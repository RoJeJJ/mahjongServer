package com.buding.battle.logic.module.user.service;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.common.AwayStatus;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.common.PushService;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.bo.PlayerExitType;
import com.buding.battle.logic.util.Util;
import com.buding.common.logger.LogLevel;
import com.buding.common.randomName.RandomNameService;
import com.buding.common.token.TokenClient;
import com.buding.db.model.User;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.helper.UserHelper;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.buding.hall.module.ws.HallPortalService;

@Component
public class LoginService {
	Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	BattleSessionManager sessionManager;

	@Autowired
	PushService pushService;

	@Autowired
	HallPortalService hallService;

	@Autowired
	UserDao userDao;

	@Autowired
	RandomNameService randomNameService;

	@Autowired
	TokenClient tokenClient;

	AtomicInteger idGen = new AtomicInteger(10001);

	boolean testMode = true;
	
	@Autowired
	UserSecurityHelper userSecurityHelper;

	//登录游戏服
	public void auth(BattleSession session, int userId, String token) {
		try {
//			token = userSecurityHelper.decrypt(token);
			
			// 会话验证
			if (!tokenClient.verifyToken(userId, token)) {								
				pushService.pushLoginRsp(session, false, null, "会话无效");
				return;
			}
			
			User user = hallService.getUser(userId);
			init(session, user);
		} catch (Exception e) {
			logger.error("loginError", e);
			pushService.pushLoginRsp(session, false, null, "无法登入比赛服务器");			
		}
	}

	private void init(BattleSession session, User user) {
		PlayerInfo p = new PlayerInfo();
		// 检查是否需要踢出同帐号的用户
		BattleSession oldSession = sessionManager.getIoSession(user.getId());
		
		//该代码待验证
		if(oldSession == session && oldSession.player != null) {
			pushService.pushLoginRsp(session, true, session.player, null);
			return;
		}
		
		if (oldSession != null && oldSession.channel != null && oldSession.channel.isOpen()) {
			// 通知旧连接下线
			String msg = "你的帐号已在其它地方登录";
			logger.info("user {} be kickout as other deviced login ", user.getId());
			pushService.pushLogoutSyn(user.getId(), msg);
		}

		UserHelper.copyUser2Player(user, p);
		Util.initSession(session, p, user);
		session.currentModule = ServiceRepo.matchModule;// 进入大厅
		pushService.pushLoginRsp(session, true, session.player, null);

		// 检查是否需要直接进入游戏
		if (oldSession != null && oldSession.getDesk() != null) {
			session.setStatus(PlayerStatus.GAMING, StatusChangeReason.PLAYER_RECONNECT_GAMING);
			session.enterMatch(oldSession.getMatch());
			session.enterRoom(oldSession.getRoom());
			session.enterDesk(oldSession.getDesk(), oldSession.getSeatIndex());
			session.recentDeskId = oldSession.recentDeskId;
			session.currentModule = oldSession.currentModule;
			session.player = oldSession.player;
			session.user = oldSession.user;
			if(oldSession.awayStatus == AwayStatus.AWAY) {
				session.getDesk().onPlayerComeBackPacketReceived(session.userId);	
			} else {
				session.getDesk().onPlayerReconnectPacketReceived(session.userId);
			}
		}
//		else if(oldSession != null && ) {
//			logger.info("player {} leave preview desk {} for session kick ", oldSession.userId, oldSession.getDesk().getDeskID()+"");
//			oldSession.getDesk().playerExit(oldSession.userId, PlayerExitType.DULICATE_LOGIN_KICK);
//		}
		logger.info("session status:{}", session.onlineStatus);
		//TODO oldSession需要摘机销毁掉
		Util.userHasLogin(session);
	}

	private User registerUser() {
		int playerId = idGen.getAndIncrement();
		User user = hallService.initUser();
		user.setId(playerId);
		user.setCtime(new Date());
		user.setUserType(0);
		user.setGender((int) (System.currentTimeMillis() % 2));
		user.setNickname(randomNameService.randomName(user.getGender()));
		return user;
	}
}
