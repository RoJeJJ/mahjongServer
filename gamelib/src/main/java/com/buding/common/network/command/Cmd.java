package com.buding.common.network.command;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public interface Cmd<KEY,DATA> {
	void execute(DATA data) throws Exception;
	KEY getKey();
}
