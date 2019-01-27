package com.buding.common.network.session;

public interface SessionListener<T extends BaseSession> {
	void sessionInvalided(T session);
}
