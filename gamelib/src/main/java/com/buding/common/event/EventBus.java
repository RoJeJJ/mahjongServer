package com.buding.common.event;


public interface EventBus {
	
	/**
	 * 提交事件 
	 * @param paramEvent
	 */
	void post(Event<?> paramEvent);

	/**
	 * 提交事件
	 * @param paramEvent
	 * @param paramLong
	 * @return
	 */
//	public abstract ScheduledFuture<?> post(Event<?> paramEvent, long paramLong);

	/**
	 * 注册事件
	 * @param paramString
	 * @param paramReceiver
	 */
	void register(String paramString, Receiver<?> paramReceiver);

	/**
	 * 注销事件
	 * @param paramString
	 * @param paramReceiver
	 */
	void unregister(String paramString, Receiver<?> paramReceiver);
}