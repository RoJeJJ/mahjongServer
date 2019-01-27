package com.buding.common.network.command;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public abstract class BaseCmd<KEY, DATA> implements Cmd<KEY, DATA>, InitializingBean {	
	@Override
	public void afterPropertiesSet() throws Exception {
		getCmdMapper().register(this);
	}
	
	public abstract CmdMapper<KEY,DATA> getCmdMapper();
	
}
