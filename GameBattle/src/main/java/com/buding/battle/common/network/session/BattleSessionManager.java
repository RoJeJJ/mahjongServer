package com.buding.battle.common.network.session;

import org.springframework.stereotype.Component;

import com.buding.battle.logic.module.common.OnlineStatus;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.PlayerExitType;
import com.buding.common.network.session.SessionManager;

@Component
public class BattleSessionManager extends SessionManager<BattleSession> {
	@Override
	public void schedule2Remove(BattleSession session) {
		log.info("mark session as offline:{}", session.userId);
		session.onlineStatus = OnlineStatus.OFFLINE;
		if(cleanSession(session)) { //尝试立刻清除
			return;
		}
		//放入定时计划里面清除
		super.schedule2Remove(session);
	}

	@Override
	public boolean cleanSession(BattleSession session) {
		if(session.channel.isOpen() == false && session.getStatus() != PlayerStatus.GAMING) {
			if(session.getMatch() != null) {
				session.getMatch().playerExit(session.userId, PlayerExitType.SESSION_CLOSE_KICK);
			}
			
			return super.cleanSession(session);
		} else if(session.channel.isOpen() == false && session.getStatus() == PlayerStatus.GAMING) {			
			CommonDesk desk = session.getDesk();
			if(desk != null) {
				desk.onPlayerOfflinePacketReceived(session.userId); //告知游戏相关模块，玩家已掉线
			}
			return false;
		} else {
			return false;
		}
	}
}
