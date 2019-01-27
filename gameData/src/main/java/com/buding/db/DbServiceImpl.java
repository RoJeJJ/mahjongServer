package com.buding.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import com.buding.common.db.dao.CommonDao;
import com.buding.common.db.executor.DbCallback;
import com.buding.common.db.executor.DbService;
import com.buding.common.db.model.BaseModel;
import com.buding.common.server.BaseServerComponent;
import com.buding.common.thread.NamedThreadFactory;
import com.buding.common.util.CollectionUtils;
import com.buding.common.util.ConcurrentHashSet;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class DbServiceImpl extends BaseServerComponent implements DbService, ApplicationListener<ContextClosedEvent> {
    private static ExecutorService DB_POOL_SERVICE;
    private static final Logger LOGGER = LoggerFactory.getLogger(DbServiceImpl.class);

    //定时从map里面取数据放入queue, 再定时从queue中取出放入DB_POOL_SERVICE
    private LinkedBlockingQueue<Runnable> DB_SAVER_QUEUE = new LinkedBlockingQueue<>();

    //一开始提交的数据放入map里面去重
    private volatile ConcurrentMap<Class<?>, ConcurrentHashSet<Object>> DB_OBJECT_MAP = new ConcurrentHashMap<>();

    private int dbPoolSize;

    private int dbPoolMaxSize;


    private int keepAliveTime;

    private int entityBlockTime;


    private CommonDao commonDao;
    private final ReentrantLock takeLock;
    private final Condition notEmpty;
    private final Runnable CUSTOMER_TASK;
    private final Runnable HANDLER_CACHED_OBJ_TASK;
    private static final int MAX_RETRY_COUNT = 5;
    private boolean customerTaskFlag;
    private boolean cacheObjTaskFlag;

    public DbServiceImpl(int dbPoolSize, int dbPoolMaxSize, int keepAliveTime, int entityBlockTime) {
        this.dbPoolSize = dbPoolSize;

        this.dbPoolMaxSize = dbPoolMaxSize;

        this.keepAliveTime = keepAliveTime;

        this.entityBlockTime = entityBlockTime;

        this.takeLock = new ReentrantLock();
        this.notEmpty = this.takeLock.newCondition();
        customerTaskFlag = true;
        cacheObjTaskFlag = true;
        this.CUSTOMER_TASK = () -> {
            while (customerTaskFlag) {
                Runnable task = null;
                try {
                    task = DB_SAVER_QUEUE.take();
                } catch (Exception ex) {
                    LOGGER.error("Error: {}", ex);
                }
                if (task != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("submit task.. QUEUE_SIZE:[{}] ", DB_SAVER_QUEUE.size());
                    }
                    DB_POOL_SERVICE.submit(task);
                }
            }
        };
        this.HANDLER_CACHED_OBJ_TASK = () -> {
            while (cacheObjTaskFlag)
                try {
                    if (entityBlockTime > 0) {
                        takeLock.lockInterruptibly();
                        try {
                            notEmpty.await(entityBlockTime, TimeUnit.MILLISECONDS);
                        } finally {
                            takeLock.unlock();
                        }
                    }
                    submitCachedDbObject();
                } catch (Exception ex) {
                    LOGGER.error("Error: " + ex.getMessage());
                }
        };
    }

    @PostConstruct
    void initialize() {
        ThreadGroup threadGroup = new ThreadGroup("缓存模块");
        NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "入库线程池");
        DB_POOL_SERVICE = new ThreadPoolExecutor(this.dbPoolSize, this.dbPoolMaxSize, this.keepAliveTime,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), threadFactory);

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookRunnable()));

        LOGGER.debug("Initialize DB Daemon Thread...");
        String threadName = "数据库入库Daemon线程";
        ThreadGroup group = new ThreadGroup(threadName);
        NamedThreadFactory factory = new NamedThreadFactory(group, threadName);
        Thread thread = factory.newThread(this.CUSTOMER_TASK);
        thread.setDaemon(true);
        thread.start();

        Thread pollCachedObjThread = factory.newThread(this.HANDLER_CACHED_OBJ_TASK);
        pollCachedObjThread.setDaemon(true);
        pollCachedObjThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookRunnable()));
    }

    private void submitCachedDbObject() {
        if (DB_OBJECT_MAP.isEmpty()) {
            return;
        }

        ConcurrentMap<Class<?>, ConcurrentHashSet<Object>> objSet = DB_OBJECT_MAP;
        DB_OBJECT_MAP = new ConcurrentHashMap<>(100);

        // Iterator iterator = objSet.keySet().iterator();
        // while (iterator.hasNext()) {
        // Class key = (Class) iterator.next();
        //
        // ConcurrentHashSet set = (ConcurrentHashSet) objSet.get(key);
        // if ((set != null) && (set.size() > 0))
        // DB_SAVER_QUEUE.add(createTask(null, new EntityCache(set)));
        // }

        for (ConcurrentHashSet<?> c : objSet.values()) {
            if (c != null && c.size() > 0) {
                DB_SAVER_QUEUE.add(createTask(null, new EntityCache<>(c)));
            }
        }

    }

    private void updateIntimeCachedDbObject() {
        if (DB_OBJECT_MAP.isEmpty()) {
            return;
        }

        // ConcurrentMap objSet = new ConcurrentHashMap();
        // synchronized (DB_OBJECT_MAP) {
        // objSet.putAll(DB_OBJECT_MAP);
        // DB_OBJECT_MAP.clear();
        // }
        //
        // int subCount = 100;
        // for (ConcurrentHashSet concurrentHashSet : objSet.values())
        // if ((concurrentHashSet != null) && (!concurrentHashSet.isEmpty())) {
        // int maxSize = concurrentHashSet.size();
        // List list = new ArrayList(concurrentHashSet);
        // int maxCount = maxSize % subCount == 0 ? maxSize / subCount : maxSize
        // / subCount + 1;
        // for (int index = 0; index < maxCount; index++)
        // updateEntityIntime(new List[] { CollectionUtils.subListCopy(list,
        // subCount * index, subCount) });
        // }

        ConcurrentMap<Class<?>, ConcurrentHashSet<Object>> objSet = DB_OBJECT_MAP;
        DB_OBJECT_MAP = new ConcurrentHashMap<>(100);

        int subCount = 100;
        int maxSize;
        int maxCount;
        for (ConcurrentHashSet<?> c : objSet.values()) {
            if (c != null && !c.isEmpty()) {
                maxSize = c.size();
                maxCount = maxSize % subCount == 0 ? maxSize / subCount : maxSize / subCount + 1;
                for (int i = 0; i < maxCount; i++) {
                    updateEntityIntime(CollectionUtils.subListCopy((List<?>) c, subCount * i, subCount));
                }
            }
        }

    }

    @SuppressWarnings("SameParameterValue")
    private <T>Runnable createTask(@Nullable final DbCallback callback, final EntityCache<T> entityCache) {
        return () -> handleTask(callback, entityCache);
    }

    private <T>void handleTask(DbCallback callback, EntityCache<T> entityCache) {
        Collection<T> entities = entityCache.getEntities();
        if ((entities != null) && (!entities.isEmpty())) {
            try {
                this.commonDao.update(entities);
            } catch (Exception ex) {
                LOGGER.error("执行入库时产生异常", ex);

                // if (EntityCache.access$904(entityCache) < MAX_RETRY_COUNT) {
                // add2Queue(callback, entityCache);
                // }
                if (entityCache.retryCount < MAX_RETRY_COUNT) {
                    add2Queue(callback, entityCache);
                }

                return;
            }
        }

        if (callback != null)
            try {
                callback.doAfter();
            } catch (Exception ex) {
                LOGGER.error("执行入库后回调时产生异常", ex);
            }
    }

    public void submitUpdate2Queue(Object... entities) {
        if (entities.length <= 0) {
            return;
        }

        if (this.entityBlockTime <= 0) {
            add2Queue(null, new EntityCache<>(entities));
            return;
        }

        for (Object entity : entities)
            if ((entity instanceof BaseModel))
                put2ObjectMap(entity);
            else if ((entity instanceof Collection))
                put2ObjectMap((Collection<?>) entity);
    }

    private <T> void put2ObjectMap(T[] entities) {
        for (Object e : entities)
            put2ObjectMap(e);
    }

    private <T> void put2ObjectMap(Collection<T> entities) {
//		for (Iterator i$ = entities.iterator(); i$.hasNext();) {
//			Object e = i$.next();
//			put2ObjectMap(e);
//		}

        for (T e : entities) {
            put2ObjectMap(e);
        }
    }

    private <T> void put2ObjectMap(T entity) {
        Class clazz = entity.getClass();
        ConcurrentHashSet<Object> objs = DB_OBJECT_MAP.get(clazz);
        if (objs == null) {
            DB_OBJECT_MAP.putIfAbsent(clazz, new ConcurrentHashSet<>());
            objs = DB_OBJECT_MAP.get(clazz);
        }
        objs.add(entity);
    }

    @Override
    public void flush() {
        while (DB_POOL_SERVICE != null) {
            if (DB_POOL_SERVICE.isShutdown()) {
                break;
            }
            if (!DB_OBJECT_MAP.isEmpty()) {//将缓存数据冲入DB
                updateIntimeCachedDbObject();
            }
            if (DB_SAVER_QUEUE.isEmpty())
                try {
                    DB_POOL_SERVICE.shutdown();
                    DB_POOL_SERVICE.awaitTermination(3000L, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    LOGGER.error("{}", e);
                }
        }
    }

    private <T>void add2Queue(DbCallback callback, EntityCache<T> entityCache) {
        DB_SAVER_QUEUE.add(createTask(null, entityCache));
    }

    public void updateEntityIntime(Object... entities) {
        if (entities.length > 0)
            handleTask(null, new EntityCache<>(entities));
    }

    public void updateEntityIntime(DbCallback callback, Object... entities) {
        handleTask(callback, new EntityCache<>(entities));
    }

    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent event) {
        submitCachedDbObject();
    }

    public void setCommonDao(CommonDao commonDao) {
        this.commonDao = commonDao;
    }

    private class ShutdownHookRunnable implements Runnable {
        private ShutdownHookRunnable() {
        }

        public void run() {
            flush();
        }
    }

    public static class EntityCache<T> {
        private Collection<T> entities;
        private int retryCount;

        Collection<T> getEntities() {
            return this.entities;
        }

        public int getRetryCount() {
            return this.retryCount;
        }

        @SuppressWarnings("unchecked")
        EntityCache(T... entities) {
            this.entities = new ArrayList<>(entities.length);
            for (T entity : entities)
                if ((entity instanceof BaseModel))
                    this.entities.add(entity);
                else if ((entity instanceof Collection))
                    this.entities.addAll((Collection<T>) entity);
                else
                    Collections.addAll(this.entities, (T[]) entity);
        }

        EntityCache(Collection<T> entities) {
            this.entities = new ArrayList<>(entities);
        }
    }

    @Override
    public String getName() {
        return "dbservice";
    }

    @Override
    public void stop() throws Exception {
        this.flush();
        super.stop();
    }

    @Override
    public String getStatusDesc() {
        return ("DB_OBJECT_MAP(cache):" + DB_OBJECT_MAP.size() + "\r\n") +
                "DB_SAVER_QUEUE(updating):" + DB_SAVER_QUEUE.size();
    }

}