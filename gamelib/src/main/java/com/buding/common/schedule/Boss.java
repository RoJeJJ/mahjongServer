package com.buding.common.schedule;

public interface Boss {
	String getBossId();
	Worker<? extends Job> getWorker();
	Worker<? extends Job> setWorker(Worker<? extends Job> worker);
}
