package com.buding.common.server;
public interface ServerComponent {
	void start() throws Exception;
	void stop() throws Exception;
	void restart() throws Exception;
	String getName();
	NodeState getState();
	
	String getStatusDesc();
}