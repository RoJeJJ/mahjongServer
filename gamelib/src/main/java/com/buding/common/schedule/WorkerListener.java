package com.buding.common.schedule;

public interface WorkerListener<T extends Job> {
	void workerStop(Worker<T> worker);
	void workerBusy(Worker<T> worker);
	void workerFree(Worker<T> worker);
}
