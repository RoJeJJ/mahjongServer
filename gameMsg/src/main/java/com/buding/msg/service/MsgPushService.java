package com.buding.msg.service;

import com.buding.hall.module.msg.vo.BaseMsg;

public interface MsgPushService extends MsgContainerFacade {
	 void push2SendQueue(BaseMsg msg) throws Exception;
	 boolean isMsgSended2User(int userId, String msgType, long msgId);
}
