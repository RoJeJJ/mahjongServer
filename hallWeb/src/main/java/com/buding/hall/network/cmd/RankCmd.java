package com.buding.hall.network.cmd;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.rank.Rank.RankItem;
import packet.rank.Rank.RankRequest;
import packet.rank.Rank.RankSyn;

import com.buding.common.token.TokenServer;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.service.UserService;
import com.buding.rank.model.RankModel;
import com.buding.rank.service.UserRankManager;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class RankCmd extends HallCmd {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserDao userDao;

	@Autowired
	TokenServer tokenServer;

	@Autowired
	UserRankManager userRankManager;
	
	@Autowired
	HallPushHelper pushHelper;
	

	@Override
	public void execute(CmdData data) throws Exception {
		PacketBase packet = data.packet;		
		RankSyn rankSynModel = userRankManager.buldRankSyn(data.session.userId);
		pushHelper.pushPBMsg(data.session, PacketType.RankSyn, rankSynModel.toByteString());
	}

	@Override
	public PacketType getKey() {
		return PacketType.RankRequest;
	}

}
