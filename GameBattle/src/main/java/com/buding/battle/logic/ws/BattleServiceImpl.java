package com.buding.battle.logic.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.common.DeskStatus;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.game.service.GameService;
import com.buding.common.cluster.service.ICluster;
import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.common.server.ServerConfig;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.game.model.DeskModel;
import com.buding.hall.module.ws.BattlePortalBroadcastService;
import com.google.gson.Gson;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class BattleServiceImpl implements BattlePortalBroadcastService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	BattleSessionManager sessionManager;
	
	@Autowired
	GameService gameService;
	
	@Autowired
	ConfigManager configManager;
	
	@Autowired
	ServerConfig serverConfig;
	
	@Autowired
	ICluster cluster;
		
	@Override
	public Result setDeskRelayData(String instanceId, int playerId, String data) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			BattleSession session = sessionManager.getIoSession(playerId);
			if(session == null) {
				return Result.fail("会话不存在");
			}
			CommonDesk desk = session.getDesk();
			if(desk == null) {
				return Result.fail("玩家不在桌子上");
			}
			if(desk.getStatus() != DeskStatus.GAMING) {
				return Result.fail("桌子未开始游戏");
			}
			desk.onSetGamingDataReq(data);
			return Result.success();
		}
		return Result.success();
	}
	
	@Override
	public void stopService(String instanceId) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			cluster.stop(instanceId);	
		}		
	}
	
	@Override
	public void startService(String instanceId) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			cluster.start(instanceId);
		}
	}

	@Override
	public void kickPlayer(String instanceId, int playerId) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			BattleSession session = sessionManager.getIoSession(playerId);
			if(session != null) {
				return;
			}
			CommonDesk desk = session.getDesk();
			if(desk != null) {
				desk.kickout(playerId, "系统管理员将你踢出桌子");
			}
		}		
	}

	@Override
	public String getDeskList(String instanceId) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			return new Gson().toJson(gameService.getDeskList());
		}
		return "";
	}

	@Override
	public void reloadMatchConf(String serverPattern) {
		try {
			if("all".equals(serverPattern) || serverConfig.instanceId.equals(serverPattern)) {
				configManager.initGameConfig();
				configManager.initMatchConfigFromDB();
				gameService.reload();
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopServer(String instanceId) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			
		}
	}

	@Override
	public Result clearDesk(String instanceId, int userId) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			BattleSession session = sessionManager.getIoSession(userId);
			if(session == null) {
				return Result.fail("会话不存在");
			}
			CommonDesk desk = session.getDesk();
			if(desk == null) {
				return Result.fail("玩家不在桌子上");
			}
			desk.onDismissPacketRequest();
			return Result.success();
		}
		return Result.success();
	}

	@Override
	public Result dismissDesk(String instanceId, String gameId, String matchId, String deskId) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			CommonDesk desk = gameService.findDesk(gameId, matchId, deskId);
			if(desk != null) {
				desk.onDismissPacketRequest();
				return Result.success();
			}
			return Result.fail("桌子不存在");
		}
		return Result.success();
	}

	@Override
	public TResult<String> dump(String instanceId, String gameId, String matchId, String deskId) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			CommonDesk desk = gameService.findDesk(gameId, matchId, deskId);
			if(desk != null) {
				String data = desk.dumpGameData();
				return TResult.sucess1(data);
			}
			return TResult.fail1("桌子不存在");
		}
		return TResult.fail1("不是本实例");
	}

	@Override
	public String getStatus() {
		return cluster.getStatus();
	}

	@Override
	public String searchDesk(int playerId) {
		DeskModel desk = gameService.searchDesk(playerId);
		return new Gson().toJson(desk);
	}
}
