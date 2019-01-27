package com.buding.ptest;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.CmdLineParser;

import com.buding.common.thread.NamedThreadFactory;
import com.google.gson.Gson;

public class PTestStarter {
	public static void main(String[] args) throws Exception {
		System.out.println("PTestStarter start.....");
		PTestCmd a = new PTestCmd();
		CmdLineParser parser = new CmdLineParser(a);
		parser.parseArgument(args);
		
		System.out.println("arg:" + new Gson().toJson(a));
				
		ThreadGroup threadGroup = new ThreadGroup("压力测试线程组");
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "压力测试线程");
		final ScheduledExecutorService gamePool = Executors.newScheduledThreadPool(50, threadFactory);
		
		final BlockingQueue<PLooper> queue = new LinkedBlockingQueue<PLooper>();
		
		final CountDownLatch latch = new CountDownLatch(a.workerCount);
		 
		ExecutorService sumitPool = Executors.newCachedThreadPool();
		for(int i = 0; i < 5; i++) {
			sumitPool.submit(new Runnable() {				
				@Override
				public void run() {
					while(true) {
						try {
							PLooper l = queue.take();
							l.run();
							if(l.loginFinish) {
								System.out.println("Add To Schedule Pool");
								latch.countDown();
								gamePool.scheduleAtFixedRate(l, new Random().nextInt(3), 1, TimeUnit.SECONDS);
							} else {
								queue.put(l);
							}
							Thread.sleep(10);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		
		for(int i = 0; i < a.workerCount; i++) {
			System.out.println("初始化");
			PLooper l = new PLooper(a.server, 5000, null, null);
			queue.add(l);
		}
		
		latch.await();
		
		System.out.println("all task submit ok ....");
	}
}
