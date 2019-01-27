package com.buding.common.event;

public interface Receiver<T extends Event<?>> {
	void onEvent(T paramEvent) throws Exception;
	String getEventName();
}