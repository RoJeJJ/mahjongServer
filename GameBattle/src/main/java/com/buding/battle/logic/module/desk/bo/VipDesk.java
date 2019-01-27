package com.buding.battle.logic.module.desk.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.buding.api.context.GameContext;
import com.buding.api.context.PlayFinalResult;
import com.buding.api.context.PlayHandResult;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.event.ChangeDeskEvent;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.DeskStatus;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.common.PushService;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.listener.DeskListener;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.common.network.session.SessionStatus;
import com.buding.db.model.User;
import com.buding.db.model.UserRoom;
import com.buding.db.model.UserRoomGameTrack;
import com.buding.db.model.UserRoomResult;
import com.buding.db.model.UserRoomResultDetail;
import com.buding.hall.config.DeskConfig;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.module.common.constants.RoomState;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.task.vo.GamePlayingVo;
import com.buding.hall.module.user.helper.UserHelper;
import com.buding.mj.model.GamingData;
import com.google.gson.Gson;

public class VipDesk extends MJDeskImpl {
	UserRoom userRoom = null;
	private int vipRoomType;
	private int quanNum;
	private int fee;

	public VipDesk(DeskListener listener, Room room, DeskConfig deskConf, String deskId,int wanfa) {
		super(listener, room, deskConf, deskId);
		this.wanfa = wanfa;
	}

	@Override
	public void changeDesk(ChangeDeskEvent e) throws Exception {
		// 不允许换桌
		logger.error("act=changeDesk;error=vipMatchNotAllowChangeDesk;userId={};deskId={};roomId={};", e.playerId, this.id, this.getParent().getRoomId());
	}

	@Override
	public void destroy(DeskDestoryReason reason) {
		super.destroy(reason);

		// 记录数据到数据库
		try {
			logger.info("act=destory;deskId={};gameCount={};", this.getDeskID(), gameCount);
			UserRoom room = ServiceRepo.userRoomDao.getByCode(this.id);
			room.setLastActiveTime(new Date());
			room.setRoomState(RoomState.CLOSE);
			if (gameCount == 0) {
				//退房卡
				ServiceRepo.hallPortalService.changeFangka(ownerId, fee, false, ItemChangeReason.DESTORY_RET);
			}
			ServiceRepo.userRoomDao.updateUserRoom(room);
		} catch (Exception e) {
			logger.error("act=destroy;error=exception", e);
		}
	}
		
	public double getFee() {
		return 0; //不扣除服务费
	}

	@Override
	public void finalSettle(GameContext context) {
		List<PlayFinalResult> list = new ArrayList<PlayFinalResult>();
		for(PlayFinalResult res : context.playerFinalResult.playDetail) {
			if(res.playerId <= 0) {
				continue;
			}
			list.add(res);
		}
		
		//房间总战绩
		UserRoomResult ur = new UserRoomResult();
		ur.setRoomId(userRoom.getId());
		ur.setRoomName(userRoom.getRoomName());
		ur.setStartTime(new Date(context.playerFinalResult.startTime));
		ur.setEndTime(new Date());
		ur.setDetail(new Gson().toJson(list));
		ServiceRepo.userRoomDao.insertUserRoomResult(ur);
		
		//每个人在哪个房间游戏过
		for(PlayFinalResult res : list) {			
			UserRoomGameTrack track = new UserRoomGameTrack();
			track.setGameTime(new Date());
			track.setRoomId(userRoom.getId());
			track.setUserId(Long.valueOf(res.playerId));
			ServiceRepo.userRoomDao.insertUserRoomGameTrack(track);
		}
	}

	@Override
	public void requestKickout(int playerId, int targetPlayerId) {
		if (playerId != ownerId) {
			logger.error("act=requestKickout;error=nopriviledge;playerId={};targetPlayerId={};deskId={};", playerId, targetPlayerId, this.id);
			PushService.instance.pushDeskPlayerKickoutRsp(playerId, false, "你不是房主,没有权限踢走其他玩家");
			return;
		}

		PlayerInfo p = this.guard.getPlayerById(targetPlayerId);
		if (p == null) {
			logger.error("act=requestKickout;result=userleaved;playerId={};targetPlayerId={};deskId={};", playerId, targetPlayerId, this.id);
			PushService.instance.pushDeskPlayerKickoutRsp(playerId, true, null);
			return;
		}

		BattleSession session = ServiceRepo.sessionManager.getIoSession(targetPlayerId);
		if (session != null) {
			if (session.getStatus() == PlayerStatus.GAMING) {
				logger.error("act=requestKickout;error=usergaming;playerId={};targetPlayerId={};deskId={};", playerId, targetPlayerId, this.id);
				PushService.instance.pushDeskPlayerKickoutRsp(playerId, false, "玩家正在游戏中,无法踢出");
				return;
			}
			// if (session.getStatus() == PlayerStatus.READY) {
			// logger.error("act=requestKickout;error=userready;playerId={};targetPlayerId={};deskId={}",
			// playerId, targetPlayerId, this.id);
			// PushService.instance.pushDeskPlayerKickoutRsp(playerId, false,
			// "玩家已准备,无法踢出");
			// return;
			// }
		}

		if (this.status == DeskStatus.GAMING) {
			logger.error("act=requestKickout;error=deskgaming;playerId={};targetPlayerId={};deskId={};", playerId, targetPlayerId, this.id);
			PushService.instance.pushDeskPlayerKickoutRsp(playerId, false, "桌子已开赛,无法踢人");
			return;
		}

		kickout(targetPlayerId, "房主已将你踢出桌子");
		PushService.instance.pushDeskPlayerKickoutRsp(playerId, true, null);
		// PushService.instance.pushJumpBack2HallSyn(targetPlayerId,
		// "桌主已将你踢出桌子");
		logger.info("act=requestKickout;result=ok;playerId={};targetPlayerId={};deskId={};", playerId, targetPlayerId, this.id);
	}

	public void setUserRoom(UserRoom userRoom) {
		this.userRoom = userRoom;
		JSONObject obj = JSONObject.fromObject(userRoom.getParams());
		vipRoomType = obj.getInt("vipRoomType");
		quanNum = obj.getInt("quanNum");
		fee = obj.optInt("fee");
		wanfa = Integer.valueOf(userRoom.getWanfa());
	}
	
	@Override
	public void onPlayerAfterExit(PlayerInfo player) {
		super.onPlayerAfterExit(player);
		ServiceRepo.vipService.pushVipRoomList(ownerId);
	}

	@Override
	protected void onPlayerAfterAway(PlayerInfo player) {
		//vip 离开时不能托管游戏
	}

	public synchronized void playerTryExit(int playerId, PlayerExitType reason) {
		if(gameCount > 0) { //游戏已开始，不允许退出
			playerTryAway(playerId);
			return;
		}
		if(ownerId == playerId) {
			boolean hasOtherPlayer = false;
			for(PlayerInfo p : guard.getPlayerList()) {
				if(p.playerId != playerId) {
					hasOtherPlayer = true;
					break;
				}
			}
			if(hasOtherPlayer) {
				//只离开，占住坑
				playerTryAway(playerId);
				return;
			}
		}
		super.playerTryExit(playerId, reason);
	}
	
	@Override
	public synchronized int playerSit(BattleContext ctx) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);
		PlayerInfo player = session.player;
		player.score = 0; //积分清零
		return super.playerSit(ctx);
	}

	@Override
	protected void onPlayerAfterSit(PlayerInfo player) {
		super.onPlayerAfterSit(player);
		ServiceRepo.vipService.pushVipRoomList(ownerId);
	}

	@Override
	public void handSettle(GameContext context) {
		//保存战斗数据
		String playBackPath = dumpGameData();

		//更新房间战绩
		List<PlayHandResult> list = new ArrayList<PlayHandResult>();
		for(PlayHandResult item : context.playerHandResults.playDetail) {
			if(item.playerId > 0) {
				list.add(item);
			}
		}
		UserRoomResultDetail detail = new UserRoomResultDetail();
		detail.setRoomId(userRoom.getId());
		detail.setRoomName(userRoom.getRoomName());
		detail.setStartTime(new Date(context.playerHandResults.startTime));
		detail.setEndTime(new Date());
		detail.setBankerPos(context.bankerPos);
		detail.setWinerPos(context.winerPos);
		detail.setDetail(new Gson().toJson(list));
		detail.setPlayBackPath(playBackPath);
		ServiceRepo.userRoomDao.insertUserRoomResultDetail(detail);
				
		//更新胜败记录和排行榜信息
		for(PlayHandResult res : list) {
			GamePlayingVo ret = new GamePlayingVo();
			ret.coin = 0; //vip场不扣除金币
			ret.gameId = this.getParent().getParent().getParent().getId();
			ret.matchId =this.getParent().getParent().getId();
			ret.enemyBankrupt = false; //不扣金币，不存在破产的可能性
			ret.bankrupt = false; //不扣金币，不存在破产的可能性
			ret.rankPoint = res.score;
			ret.tax = 0; //不扣服务费，房主用房卡一次性支付
			ret.userId = res.playerId;
			ret.winCount = res.result == PlayHandResult.GAME_RESULT_WIN ? 1 : 0;
			ret.loseCount = res.result == PlayHandResult.GAME_RESULT_LOSE ? 1 : 0;
			ret.evenCount = res.result == PlayHandResult.GAME_RESULT_EVEN ? 1 : 0;
			ret.continueWin = ret.winCount;
			ret.gameTime = new Date();
			ret.maxFanDesc = res.fanDesc;
			ret.maxFanType = res.fanType;
			ret.maxFanNum = res.fanNum;
			ret.maxDownCards = res.downcards;
			ret.maxFanHandCards = res.handcards;

			User user = ServiceRepo.hallPortalService.addGameResult(ret);
			
			//更新用户属性
			BattleSession session = ServiceRepo.sessionManager.getIoSession(ret.userId);
			session.player.score += res.score; //改变累计积分
			if(user != null && session != null) {
				UserHelper.copyUser2Player(user, session.player);
			}
			
			MatchConfig conf = this.getParent().getParent().getMatchConfig();
			if (conf.game.isRank || conf.isRank) { // 是排位赛
				//ServiceRepo.userRankServiceStub.addUserRankPoint(res.playerId, res.playerName, conf.gameID, res.score, new Date());
			}
		}
		
		addGameLog(context);
	}

	@Override
	public void ready4NextGame(GameContext context) {
		//自动准备
		for(PlayerInfo p : this.guard.getPlayerList()) {
			BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
			session.setStatus(PlayerStatus.READY, StatusChangeReason.NEXT_GAME_READY);
		}
	}

	@Override
	public void startNextGame(GameContext context) {
		this.game.setDesk(this, deskConf.gameParam);
		markDeskAsWaitingGame();
	}

	protected void markDeskAsWaitingGame() {
		this.status = DeskStatus.WATING;
		this.waitingGameStartTime = System.currentTimeMillis();
	}
	
	@Override
	public boolean hasNextGame(GameContext context) {
		if (vipRoomType == 2) { // 2人麻将
			return context.nextHandNum <= 24; //这里的quanNum存的是局数
		}
		if (vipRoomType == 4) { //4人麻将
			return context.nextQuanNum <= quanNum;
		}
		return false;
	}

	@Override
	public boolean isVipTable() {
		return true;
	}

	@Override
	public int getTotalQuan() {
		return quanNum;
	}
	
}
