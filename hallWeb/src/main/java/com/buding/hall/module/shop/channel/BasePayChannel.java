package com.buding.hall.module.shop.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BasePayChannel implements PayChannel, InitializingBean {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	ChannelRepostory channelRepostory;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		channelRepostory.register(this);
	}
}
