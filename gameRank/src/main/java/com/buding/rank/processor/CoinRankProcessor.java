package com.buding.rank.processor;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.buding.common.event.EventBus;
import com.buding.common.event.Receiver;
import com.buding.db.model.User;
import com.buding.db.model.UserRank;
import com.buding.hall.config.RankConfig;
import com.buding.hall.module.common.constants.RankPointType;
import com.buding.hall.module.task.event.CoinChangeEvent;
import com.buding.hall.module.task.type.EventType;
import com.buding.hall.module.user.dao.UserDao;


/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class CoinRankProcessor extends BaseRankProcessor implements Receiver<CoinChangeEvent> {
	@Autowired
	@Qualifier("hallEventBus")
	EventBus eventBus;
	
	@Autowired
	UserDao userDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		eventBus.register(EventType.COIN_CHANGE, this);
		//eventBus.register(EventType.LOGIN,this);
	}
	
	@Override
	public int getRankType() {
		return RankPointType.coin;
	}

	@Override
	protected long getRankGroupTime() {
		return 1;
	}

	@Override
	protected RankConfig getRankConfig() {
		return configManager.rankConfMap.get("GAME_COIN_RANK");
	}

	@Override
	public void onEvent(CoinChangeEvent paramEvent) throws Exception {
		int userId = paramEvent.getBody().getUserId();
		int coin = paramEvent.getBody().getCoin();
		addRankPoint(userId, coin, getRankGroupTime());
	}
	
	protected UserRank initUserRank(int userId, int rankPoint) {
		User user = userDao.getUser(userId);
		UserRank rank = new UserRank();
		rank.setAuditId(0L);
		rank.setCtime(new Date());
		rank.setMtime(new Date());
		rank.setRank(0);
		rank.setRankPoint(user.getCoin());
		rank.setUserId(userId);
		rank.setGroupId(getRankConfig().id);
		rank.setRankGrpTime(getRankGroupTime());
		userRankDao.insert(rank);
		return rank;
	}

	@Override
	public String getEventName() {
		return EventType.COIN_CHANGE;
	}
	
}
