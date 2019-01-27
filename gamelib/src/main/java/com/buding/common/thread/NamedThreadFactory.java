package com.buding.common.thread;

import org.springframework.lang.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;

	public NamedThreadFactory(ThreadGroup group, String name) {
		this.group = group;
		this.namePrefix = (group.getName() + ":" + name);
	}

	public Thread newThread(@NonNull Runnable r) {
		return new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
	}
}