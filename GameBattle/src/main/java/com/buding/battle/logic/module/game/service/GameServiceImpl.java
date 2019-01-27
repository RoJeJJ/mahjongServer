package com.buding.battle.logic.module.game.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import packet.game.MsgGame.GameMsgSyn;

import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.cluster.ClusterStubService;
import com.buding.battle.logic.module.common.AwayStatus;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.EnrollResult;
import com.buding.battle.logic.module.common.EnterRoomResult;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.common.PushService;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.PlayerExitType;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.game.GameImpl;
import com.buding.battle.logic.module.match.Match;
import com.buding.common.admin.component.BaseComponent;
import com.buding.common.cluster.model.RoomOnlineModel;
import com.buding.common.result.Result;
import com.buding.common.server.ServerConfig;
import com.buding.common.util.VelocityUtil;
import com.buding.db.model.User;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.GameConfig;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.module.cluster.model.UserGaming;
import com.buding.hall.module.game.model.DeskModel;
import com.buding.hall.module.user.helper.UserHelper;
import com.buding.hall.module.ws.HallPortalService;

@Component
public class GameServiceImpl extends BaseComponent implements GameService, InitializingBean {	
	public static GameServiceImpl instance;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	ConcurrentMap<String, Game> gameMap = new ConcurrentHashMap<String, Game>();

	@Autowired
	PushService pushService;

	@Autowired
	BattleSessionManager sessionManager;
	
	@Autowired
	ConfigManager configManager;
	
	@Autowired
	HallPortalService hallPortalService;
	
	@Value("${game.quickStartGameId}")
	private String quickStartGameId;
	
	@Value("${game.gamesRecevieBankrupt}")
	private String gamesRecevieBankrupt;
	
	@Autowired
	ClusterStubService clusterStubService;
	
	@Autowired
	ServerConfig serverConfig;
		
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
		reload();
	}

	@Override
	public void reload() {
		for(GameConfig config : configManager.gameMap.values()) {
			Game game = gameMap.get(config.gameId);
			if(game == null) {
				game = new GameImpl();
			}
			game.init(config);
			gameMap.put(config.gameId, game);
		}
	}

	public void leavePreviewDesk(BattleSession session) {
		CommonDesk desk = session.getDesk();
		if (desk != null) {
			logger.info("player leave preview desk {} for next desk; ", desk.getDeskID());
			desk.playerExit(session.userId, PlayerExitType.DULICATE_ENROLL);
		}
	}

	public Result enroll(BattleSession session, BattleContext ctx) {
		if(session.getStatus() == PlayerStatus.GAMING || session.getStatus() == PlayerStatus.READY) {
			CommonDesk desk = session.getDesk();
			if(desk != null) {
				//离开回来、还是断线重连
				if(session.awayStatus == AwayStatus.AWAY) {
					desk.onPlayerComeBackPacketReceived(session.userId);
				} else {
					desk.onPlayerReconnectPacketReceived(session.userId);					
				}
				return new Result(2);
			}
		}
		
		if(session.userId == 0) {
			return Result.fail("未登录游戏服");
		}
		User user = hallPortalService.getUser(session.userId);
		UserHelper.copyUser2Player(user, session.player);
		
		UserGaming gaming = clusterStubService.checkPlayInOtherGame(session, ctx.getGameId());
		if(gaming != null) {
			String msg = "你已经在" + configManager.gameMap.get(gaming.gameId).gameName+"开始了游戏,不能再进入其它赛区";					
			return Result.fail(msg); 
		}
		
		leavePreviewDesk(session);
		
		Game game = gameMap.get(ctx.getGameId());
		if(game == null) {
			logger.info("player {} enroll fail, game not found:{};", session.userId, ctx.getGameId());
			return Result.fail("游戏不存在");
		}

		EnrollResult ret = game.enroll(ctx);
		if (!ret.isOk()) {
			String msg = ret.msg == null ? "报名失败" : ret.msg;			
			logger.info("player {} enroll fail, code={}; msg={};", session.userId, ret.code, msg);
			return Result.fail(msg);
		}

		Match match = game.getMatch(ctx.matchId);
		session.enterMatch(match);
		session.setStatus(PlayerStatus.ENROLL_OK, StatusChangeReason.ENROLL);

		ctx.setRoomId(ret.roomId);
		EnterRoomResult deskResult = match.enterRoom(ctx);

		if (deskResult.isFail()) {
			String msg = deskResult.msg == null ? "报名失败" : deskResult.msg;			
			logger.info("player {} enter room fail;", session.userId);
			return Result.fail(msg);
		}

		session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.ENROLL);

		session.currentModule = ServiceRepo.matchModule;
		
		return Result.success();
	}

	public void requestReady(BattleSession session) {
		CommonDesk desk = session.getDesk();
		if (desk == null) {
			String gameId = session.getGame().getId();
			String matchId = session.getMatch().getId();
			Result ret = enroll(session, BattleContext.create(session).setGameId(gameId).setMatchId(matchId));
			if(ret.isFail()) {
				pushService.pushReadyRsp(session.userId, false, ret.msg == null ? "无法开始游戏" : ret.msg);
				return;
			}
			desk = session.getDesk();
		}
		
		//刷新用户数据
		User user = hallPortalService.getUser(session.userId);
		UserHelper.copyUser2Player(user, session.player);
		
		desk.onPlayerReadyPacketReceived(session.userId);
	}
	
	public void changeDesk(BattleSession session) {
		//换桌次数是否有限制
		
		PlayerStatus status = session.getStatus();
		if(status == PlayerStatus.GAMING) {
			logger.error("游戏已经开始,忽略玩家的换桌请求，玩家ID:[{}];", session.userId);
			return;
		}
		
		CommonDesk desk = session.getDesk();
		if(desk == null) {
			logger.error("玩家不在桌子上面,忽略玩家的换桌请求，玩家ID:[{}];", session.userId);
			return;
		}
		
		//将请求包放入队列
		desk.onPlayerChangeDeskPacketReceived(session.userId);
	}
	
	@Override
	public void quickStart(BattleSession session) {
		GameConfig game = configManager.gameMap.get(quickStartGameId);
		if(game == null) {
			logger.error("user {} quick start fail, game {} not found;", session.userId, quickStartGameId);
			pushService.pushQuickStartRsp(session.userId, -1, "游戏不存在");
			return;
		}
		MatchConfig match = configManager.getMatchConfig(quickStartGameId, session.player.coin);
		if(match == null) {
			pushService.pushQuickStartRsp(session.userId, -1, "快速游戏失败,金币不足");
			return;
		}
		Result ret = enroll(session, BattleContext.create(session).setGameId(quickStartGameId).setMatchId(match.matchID));
		if(ret.isOk()) {
			pushService.pushQuickStartRsp(session.userId, 0, "");
		} else {
			pushService.pushQuickStartRsp(session.userId, -1, ret.msg == null ? "快速开始失败" : ret.msg);
		}
		session.getDesk().onPlayerReadyPacketReceived(session.userId);
	}
	
	@Override
	public void checkCoinInMath(BattleSession session, Match match) {
		if(match == null) {
			return;
		}
		MatchConfig targetMatch = configManager.getMatchConfig(match.getMatchConfig().gameID, session.player.coin);
		//可以留在本赛区
		if(targetMatch != null) {
			//还可以继续留在本场
			if(targetMatch.matchID.equals(match.getMatchConfig().matchID)) {
				return;
			}
			
			//只能前往本赛区的其它场
			GameMsgSyn.Builder syn = GameMsgSyn.newBuilder();
			Map<String, String> params = new HashMap<String, String>();
			params.put("gameName", targetMatch.matchName);
			if(session.player.coin < match.getMatchConfig().conditionInfo.enterCondition.minCoinLimit) {
				//只能去低分场
				syn.setFlag(0);
				syn.setGameId(targetMatch.gameID);
				syn.setMatchId(targetMatch.matchID);
				syn.setMsg(VelocityUtil.merge(configManager.msgTplMap.get("playLowMatchMsg"), params));
			} else {
				//只能去高分场
				syn.setFlag(0);
				syn.setGameId(targetMatch.gameID);
				syn.setMatchId(targetMatch.matchID);
				syn.setMsg(VelocityUtil.merge(configManager.msgTplMap.get("playHighMatchMsg"), params));
			}
			pushService.pushGameMsgSyn(session.userId, syn.build().toByteArray());
			return;
		}
		
		if(StringUtils.isNotBlank(gamesRecevieBankrupt)) {
			for(String gameId : gamesRecevieBankrupt.split(",")) {
				targetMatch = configManager.getMatchConfig(gameId, session.player.coin);
				if(targetMatch != null) {
					//前往其它区玩
					GameMsgSyn.Builder syn = GameMsgSyn.newBuilder();		
					Map<String, String> params = new HashMap<String, String>();
					params.put("gameName", match.getMatchConfig().matchName);
					params.put("coin", match.getMatchConfig().conditionInfo.enterCondition.minCoinLimit+"");
					syn.setFlag(0);
					syn.setGameId(targetMatch.gameID);
					syn.setMatchId(targetMatch.matchID);
					syn.setMsg(VelocityUtil.merge(configManager.msgTplMap.get("playOtherGameMsg"), params));
					pushService.pushGameMsgSyn(session.userId, syn.build().toByteArray());
					return;
				}
			}
		}
		
		//还可以领取破产救济
		if(hallPortalService.isCanReceiveBankAssist(session.userId)) {
			GameMsgSyn.Builder syn = GameMsgSyn.newBuilder();
			syn.setFlag(1); //返回大厅
			syn.setMsg(configManager.msgTplMap.get("bankruptMsg"));
			pushService.pushGameMsgSyn(session.userId, syn.build().toByteArray());
			return;
		} else {
			GameMsgSyn.Builder syn = GameMsgSyn.newBuilder();
			syn.setFlag(2); //充值
			syn.setMsg(configManager.msgTplMap.get("playEndCharge"));
			pushService.pushGameMsgSyn(session.userId, syn.build().toByteArray());
			return;
		}
	}

	@Override
	public ConcurrentMap<String, Game> gameMap() {
		return this.gameMap;
	}

	@Override
	public String getComponentName() {
		return "gameservice";
	}

	@Override
	public Map<String, Map<String, Map<String, Map<String, Double>>>> getDeskDelayStatus() {
		Map<String, Map<String, Map<String, Map<String, Double>>>> map = new HashMap<String, Map<String, Map<String, Map<String, Double>>>>();
		for(String key : gameMap.keySet()) {
			Map<String, Map<String, Map<String, Double>>> a = gameMap.get(key).getDeskDelayStatus();
			map.put(key, a);
		}
		return map;
	}

	@Override
	public Game getById(String gameId) {
		return gameMap.get(gameId);
	}

	@Override
	public List<DeskModel> getDeskList() {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(Game game : this.gameMap.values()) {
			list.addAll(game.getDeskList());
		}
		for(DeskModel desk : list) {
			desk.instanceId = serverConfig.instanceId;
		}
		return list;
	}

	@Override
	public CommonDesk findDesk(String gameId, String matchId, String deskId) {
		Game game = gameMap.get(gameId);
		if(game == null) {
			return null;
		}
		Match match = game.getMatch(matchId);
		if(match == null) {
			return null;
		}
		
		return match.findDesk(deskId);
	}

	@Override
	public DeskModel searchDesk(int playerId) {
		for(Game game : this.gameMap.values()) {
			DeskModel desk = game.findDesk(playerId);
			if(desk != null) {
				return desk;
			}
		}
		return null;
	}

	@Override
	public List<RoomOnlineModel> getRoomOnlineList() {
		List<RoomOnlineModel> list = new ArrayList<RoomOnlineModel>();
		for(Game game : this.gameMap.values()) {
			list.addAll(game.getRoomOnlineList());
		}
		return list;
	}

	
	
}
