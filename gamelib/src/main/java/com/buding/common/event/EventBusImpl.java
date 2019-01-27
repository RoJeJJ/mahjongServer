package com.buding.common.event;

import com.buding.common.thread.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

public class EventBusImpl implements EventBus, ApplicationListener<ContextClosedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(EventBusImpl.class);

	private ConcurrentHashMap<String, CopyOnWriteArraySet<Receiver<?>>> receivers = new ConcurrentHashMap<String, CopyOnWriteArraySet<Receiver<?>>>();

//	@Autowired
//	private Scheduler scheduler;

//	@Autowired(required = false)
//	@Qualifier("event_queue_size")
	private Integer queueSize;
	
	private BlockingQueue<Event<?>> eventQueue;

//	@Autowired(required = false)
//	@Qualifier("event_pool_size")
	private Integer poolSize;

//	@Autowired(required = false)
//	@Qualifier("event_pool_max_size")
	private Integer poolMaxSize;

//	@Autowired(required = false)
//	@Qualifier("event_pool_alive_time")
	private Integer poolKeepAlive;
	
	private ExecutorService pool;

	public void setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
	}

	public void setPoolMaxSize(Integer poolMaxSize) {
		this.poolMaxSize = poolMaxSize;
	}

	public void setQueueSize(Integer queueSize) {
		this.queueSize = queueSize;
	}

	public void setPoolKeepAlive(Integer poolKeepAlive) {
		this.poolKeepAlive = poolKeepAlive;
	}

	private Runnable consumerRunner = new Runnable() {
		public void run() {
			while (true)
				try {
					Event<?> event = (Event<?>) eventQueue.take();
					String name = event.getName();
					if (!receivers.containsKey(name)) {
						logger.warn("事件'{}'没有对应的接收器", name);
					} else {
						// Iterator localIterator = ((CopyOnWriteArraySet)
						// EventBusImpl.this.receivers.get(name)).iterator();
						// continue;
						// Receiver receiver = (Receiver) localIterator.next();
						// Runnable runner = createRunner(receiver, event);
						// try {
						// EventBusImpl.this.pool.submit(runner);
						// } catch (RejectedExecutionException e) {
						// EventBusImpl.logger.error("事件线程池已满，请尽快调整配置参数");
						// onRejected(receiver, event);
						// }
						// if (!localIterator.hasNext())
						// ;

						CopyOnWriteArraySet<Receiver<?>> arraySet = receivers.get(name);
						Runnable runner = null;
						for (Receiver<?> r : arraySet) {
							try {
								runner = createRunner(r, event);
								pool.submit(runner);
							} catch (RejectedExecutionException e) {
								logger.error("事件线程池已满，请尽快调整配置参数");
								onRejected(r, event);
							}
						}

					}
				} catch (InterruptedException e) {
					logger.error("获取事件对象时出现异常", e);
				}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void onRejected(Receiver receiver, Event event) {
			try {
				receiver.onEvent(event);
			} catch (ClassCastException e) {
				logger.error("事件[{}]对象类型不符合接收器声明,{}", event.getName(), e);
			} catch (Throwable t) {
				logger.error("执行处理事件时发生异常", t);
			}
		}

		@SuppressWarnings("rawtypes")
		private Runnable createRunner(final Receiver receiver, final Event event) {
			return new Runnable() {
				@SuppressWarnings("unchecked")
				public void run() {
					try {
						receiver.onEvent(event);
					} catch (ClassCastException e) {
						logger.error("事件[{}]对象类型不符合接收器声明,{}", event.getName(), e);
					} catch (Throwable t) {
						logger.error("事件'" + event.getName() + "'处理时发生异常", t);
					}
				}
			};
		}
	};
	private volatile boolean stop;

	@PostConstruct
	protected void initialize() {
		NamedThreadFactory threadFactory = new NamedThreadFactory(new ThreadGroup("事件模块"), "事件处理");
		this.eventQueue = new LinkedBlockingQueue<>(queueSize);
		this.pool = new ThreadPoolExecutor(this.poolSize, this.poolMaxSize, this.poolKeepAlive, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(this.queueSize), threadFactory);

		Thread consumer = new Thread(this.consumerRunner, "消费事件后台线程");
		consumer.setDaemon(true);
		consumer.start();
	}

	public void onApplicationEvent(ContextClosedEvent event) {
		shutdown();
	}

	public void shutdown() {
		if (isStop())
			return;
		this.stop = true;

		while (!this.eventQueue.isEmpty()) {
			Thread.yield();
		}
		this.pool.shutdown();

		while (!this.pool.isTerminated()) {
			Thread.yield();
		}
	}

	public boolean isStop() {
		return this.stop;
	}

	public void post(Event<?> event) {
		if (event == null) {
			throw new IllegalArgumentException("事件对象不能为空");
		}
		if (isStop())
			throw new IllegalStateException("事件总线已经停止，不能再接收事件");
		try {
			this.eventQueue.put(event);
		} catch (InterruptedException e) {
			logger.error("在添加事件对象时产生异常", e);
		}
	}

//	public ScheduledFuture<?> post(Event<?> event, long delay) {
//		if (event == null) {
//			logger.error("事件对象不能为空");
//			return null;
//		}
//		if (isStop()) {
//			logger.error("事件总线已经停止，不能再接收事件[{}]", event.getName());
//		}
//		logger.debug("提交延迟[{}毫秒]的定时事件[{}]", event.getName());
//		return this.scheduler.scheduleWithDelay(getScheduleTask(event), delay);
//	}

//	private ScheduledTask getScheduleTask(final Event<?> event) {
//		return new ScheduledTask() {
//			public void run() {
//				EventBusImpl.this.post(event);
//			}
//
//			public String getName() {
//				return "延迟推送时间线程任务";
//			}
//		};
//	}

	public void register(String name, Receiver<?> receiver) {
		if ((name == null) || (receiver == null)) {
			throw new IllegalArgumentException("事件名和接收者均不能为空");
		}

		CopyOnWriteArraySet<Receiver<?>> set = this.receivers.get(name);
		if (set == null) {
			set = new CopyOnWriteArraySet<>();
			CopyOnWriteArraySet<Receiver<?>> prev = this.receivers.putIfAbsent(name, set);
			set = prev != null ? prev : set;
		}

		set.add(receiver);
	}

	public void unregister(String name, Receiver<?> receiver) {
		if ((name == null) || (receiver == null)) {
			throw new IllegalArgumentException("事件名和接收者均不能为空");
		}

		CopyOnWriteArraySet<Receiver<?>> set = this.receivers.get(name);
		if (set != null)
			set.remove(receiver);
	}
}