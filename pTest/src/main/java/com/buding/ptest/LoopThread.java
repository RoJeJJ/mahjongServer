package com.buding.ptest;

import java.util.concurrent.BlockingQueue;

public class LoopThread extends Thread {
	private BlockingQueue<PLooper> queue = null;
	
	public LoopThread(BlockingQueue<PLooper> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		while(true) {
			try {
				PLooper l = queue.take();
				l.loop();
				Thread.sleep(1000);
				queue.put(l);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
