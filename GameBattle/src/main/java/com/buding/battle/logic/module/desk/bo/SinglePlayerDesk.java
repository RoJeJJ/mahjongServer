package com.buding.battle.logic.module.desk.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.buding.api.context.GameContext;
import com.buding.api.context.PlayHandResult;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.DeskStatus;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.listener.DeskListener;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.db.model.User;
import com.buding.db.model.UserRoomResultDetail;
import com.buding.hall.config.DeskConfig;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.module.task.vo.GamePlayingVo;
import com.buding.hall.module.user.helper.UserHelper;
import com.google.gson.Gson;

//单机场
public class SinglePlayerDesk extends MJDeskImpl {
	public SinglePlayerDesk(DeskListener listener, Room room, DeskConfig deskConf, String deskId) {
		super(listener, room, deskConf, deskId);
	}

	@Override
	public boolean hasNextGame(GameContext context) {
		return true;
	}

	@Override
	public synchronized int playerSit(BattleContext ctx) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);
		PlayerInfo player = session.player;
		player.score = 0; //积分清零
		return super.playerSit(ctx);
	}
	
	@Override
	public void handSettle(GameContext context) {
		//保存战斗数据
		dumpGameData();

		List<PlayHandResult> list = new ArrayList<PlayHandResult>();
		for(PlayHandResult item : context.playerHandResults.playDetail) {
			if(item.playerId > 0) {
				list.add(item);
			}
		}
						
		//更新胜败记录和排行榜信息
		for(PlayHandResult res : list) {			
			//更新用户属性
			BattleSession session = ServiceRepo.sessionManager.getIoSession(res.playerId);
			session.player.score += res.score; //改变累计积分
			
		}
		
		addGameLog(context);
	}
	
	
}
