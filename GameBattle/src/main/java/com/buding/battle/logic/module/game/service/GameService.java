package com.buding.battle.logic.module.game.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.match.Match;
import com.buding.common.cluster.model.RoomOnlineModel;
import com.buding.common.result.Result;
import com.buding.hall.module.game.model.DeskModel;

public interface GameService {

	Result enroll(BattleSession session, BattleContext ctx);

	void requestReady(BattleSession session);

	void changeDesk(BattleSession session);
	
	void quickStart(BattleSession session);
	
	void checkCoinInMath(BattleSession session, Match match);
	
	ConcurrentMap<String, Game> gameMap();
	
	Map<String, Map<String, Map<String, Map<String, Double>>>> getDeskDelayStatus();
	
	List<RoomOnlineModel> getRoomOnlineList();
	
	Game getById(String gameId);
	
	List<DeskModel> getDeskList();
	
	void reload();
	
	CommonDesk findDesk(String gameId, String matchId, String deskId);
	
	DeskModel searchDesk(int playerId);
}
