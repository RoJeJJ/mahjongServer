package com.buding.battle.logic.module.game;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.EnrollResult;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.match.MatchImpl;
import com.buding.common.cluster.model.RoomOnlineModel;
import com.buding.common.server.BaseServerComponent;
import com.buding.hall.config.GameConfig;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.module.game.model.DeskModel;
import com.google.gson.GsonBuilder;


public class GameImpl extends BaseServerComponent implements Game {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	ConcurrentMap<String, Match> matchMap = new ConcurrentHashMap<String, Match>();
	transient GameConfig gameConfig;

	public  GameImpl() {
		
	}
	
	public void init(GameConfig config) {
		this.gameConfig = config;
		
		logger.info("act=gameInit;gameId={};gameName={};", gameConfig.gameId, gameConfig.gameName);
		for(MatchConfig conf : gameConfig.matchs) {
			try {
				Match match = matchMap.get(conf.matchID);
				if(match == null) {
					if(StringUtils.isNotBlank(conf.matchClassFullName)) {
						Class<?> cls = getClass().getClassLoader().loadClass(conf.matchClassFullName);
						Constructor<?> c = cls.getConstructor(Game.class);
						match = (Match)c.newInstance(this);
					} else {
						match = new MatchImpl(this);
					}
				}
				match.init(conf);
				matchMap.put(conf.matchID, match);
			} catch(Exception e) {
				logger.error("act=init;error=exception;", e);
			}
		}
		
	}
	
	
	public Match getMatch(String matchId) {
		return matchMap.get(matchId);
	}
	
	@Override
	public int getPlayerCount() {
		int c = 0;
		for(Match m : matchMap.values()) {
			c += m.getPlayerCount();
		}
		return c;
	}
	
	@Override
	public int getMaxPlayerCount() {
		int c = 0;
		for(Match m : matchMap.values()) {
			c += m.getMatchConfig().playerCountLimit;
		}
		return c;
	}

	@Override
	public EnrollResult enroll(BattleContext ctx) {
		Match match =  matchMap.get(ctx.matchId);
		if(match == null) {
			logger.info("act=enroll;error=matchMiss;playerId={};matchId={};", ctx.playerId, ctx.matchId);
			return EnrollResult.fail("赛场不存在");
		}
		
		EnrollResult ret = match.enroll(ctx);
		return ret;
	}
	
	@Override
	public Map<String, Match> getMatchMap() {
		return matchMap;
	}

	@Override
	public String getId() {
		return gameConfig.gameId;
	}

	@Override
	public String getName() {
		return gameConfig.gameName;
	}

	@Override
	public List<DeskModel> getDeskList() {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(Match room : this.matchMap.values()) {
			list.addAll(room.getDeskList());
		}
		return list;
	}

	@Override
	public Map<String, Integer> getPlayerMap() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(String matchId : matchMap.keySet()) {
			map.put(matchId, matchMap.get(matchId).getPlayerCount());
		}
		return map;
	}

	@Override
	public Map<String, Map<String, Map<String, Double>>> getDeskDelayStatus() {
		Map<String, Map<String, Map<String, Double>>> map = new HashMap<String, Map<String, Map<String, Double>>>();
		for(String key : matchMap.keySet()) {
			Map<String, Map<String, Double>> a = matchMap.get(key).getDeskDelayStatus();
			map.put(key, a);
		}
		return map;
	}

	@Override
	public String getStatusDesc() {
		String status = new GsonBuilder().setPrettyPrinting().create().toJson(getDeskDelayStatus());
		return status;
	}

	@Override
	public DeskModel findDesk(int playerId) {
		for(Match m : this.matchMap.values()) {
			DeskModel desk = m.findDesk(playerId);
			if(desk != null) {
				return desk;
			}
		}
		return null;
	}

	@Override
	public List<RoomOnlineModel> getRoomOnlineList() {
		List<RoomOnlineModel> list = new ArrayList<RoomOnlineModel>();
		for(Match m : this.matchMap.values()) {
			list.addAll(m.getRoomOnlineList());
		}
		return list;
	}
	
}
