package com.buding.hall.network.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Msg.ReceiveMailAttachRequest;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.result.Result;
import com.buding.db.model.UserAward;
import com.buding.db.model.UserMsg;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.award.dao.AwardDao;
import com.buding.hall.module.award.service.AwardService;
import com.buding.hall.module.msg.dao.MsgDao;

@Component
public class ReceiveMailAttachCmd extends HallCmd {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	protected HallPushHelper pushHelper;
		
	@Autowired
	ConfigManager configManager;
	
	@Autowired
	MsgDao msgDao;
	
	@Autowired
	AwardService awardService;
	
	@Override
	public void execute(CmdData data) throws Exception {
		ReceiveMailAttachRequest req = ReceiveMailAttachRequest.parseFrom(data.packet.getData());
		int userId = data.session.userId;
		UserMsg userMsg = msgDao.getUserMsg(userId, req.getMailId());
		if(userMsg == null) {
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "邮件不存在");
			return;
		}
		
		long awardId = userMsg.getAwardId();
		if(awardId  == 0) {
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "邮件没有附件");
			return;
		}
		
		Result ret = awardService.receiveAward(userId, awardId);
		if(ret.isFail()) {
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, ret.msg);
		}
	}

	@Override
	public PacketType getKey() {
		return PacketType.ReceiveMailAttachRequest;
	}	
}
