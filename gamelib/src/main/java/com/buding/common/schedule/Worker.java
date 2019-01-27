package com.buding.common.schedule;

public interface Worker<T extends Job> extends Runnable {
	/**
	 * 是否有空
	 * @return
	 */
	boolean isBusy();
	
	/**
	 * 提交任务
	 * @param job
	 */
	void submitJob(T job);
	
	/**
	 * 停止干活
	 */
	void stop();
	
	/**
	 * 设置监听器
	 * @param listener
	 */
	void setWorkerListener(WorkerListener<T> listener);
	
	/**
	 * 获取工作者id
	 * @return
	 */
	long getId();
	
	void followBoss(Boss boss);
	
	void freeFrom(Boss boss);
}
