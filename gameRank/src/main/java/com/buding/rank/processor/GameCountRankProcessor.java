package com.buding.rank.processor;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.buding.common.event.EventBus;
import com.buding.common.event.Receiver;
import com.buding.db.model.User;
import com.buding.db.model.UserGameOutline;
import com.buding.db.model.UserRank;
import com.buding.hall.config.RankConfig;
import com.buding.hall.module.common.constants.RankPointType;
import com.buding.hall.module.task.event.GamePlayedEvent;
import com.buding.hall.module.task.type.EventType;
import com.buding.hall.module.task.vo.GamePlayingVo;
import com.buding.hall.module.user.dao.UserDao;


/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class GameCountRankProcessor extends BaseRankProcessor implements Receiver<GamePlayedEvent> {
	@Autowired
	@Qualifier("hallEventBus")
	EventBus eventBus;
	
	@Autowired
	UserDao userDao;	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		eventBus.register(EventType.PLAYED_GAME, this);
	}

	@Override
	public void onEvent(GamePlayedEvent event) throws Exception {
		GamePlayingVo model = event.getBody();
		addRankPoint(model.userId, model.loseCount + model.winCount + model.evenCount, getRankGroupTime());
	}

	@Override
	public String getEventName() {
		return EventType.PLAYED_GAME;
	}


	@Override
	public int getRankType() {
		return RankPointType.gameCount;
	}

	@Override
	protected long getRankGroupTime() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int week = c.get(Calendar.WEEK_OF_YEAR);
		return year * 1000 + week;
	}

	@Override
	protected RankConfig getRankConfig() {
		return configManager.rankConfMap.get("GAME_COUNT_RANK");
	}

}
