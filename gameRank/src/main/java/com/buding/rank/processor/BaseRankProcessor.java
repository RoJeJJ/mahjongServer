package com.buding.rank.processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.cache.RedisClient;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.User;
import com.buding.db.model.UserRank;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.RankConfig;
import com.buding.hall.module.rank.dao.UserRankDao;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.rank.model.RankModel;
import com.buding.rank.service.UserRankManager;
import com.google.gson.Gson;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public abstract class BaseRankProcessor implements RankProcessor, InitializingBean {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected UserRankManager rankManager;
 
	@Autowired
	protected ConfigManager configManager;

	@Autowired
	protected UserRankDao userRankDao;

	@Autowired
	protected UserDao userDao;

	private long version = 0;
	
	@Autowired
	private RedisClient redisClient;

	@Override
	public void afterPropertiesSet() throws Exception {
		rankManager.registerProcessor(getRankType(), this);
	}

	public void loadFromDB() {
//		logger.info("////// " + getClass() + " load rank from db //////");
		RankConfig rankConfig = getRankConfig();
		List<UserRank> list = userRankDao.getRankList(rankConfig.id, getRankGroupTime(), rankConfig.rankLimit);
		
		String key = rankConfig.pointType + "_rank";
		redisClient.zremrangeByRank(key, 0, rankConfig.rankLimit*2);		
		int i = 1;
		for (UserRank rank : list) {
			RankModel rankModel = dbModel2CacheModel(rank);
			rankModel.rank = i++;
			int score = rankModel.rank;
			String member = new Gson().toJson(rankModel);
			redisClient.zadd(key, score, member);
		}
		redisClient.hset("ddz_meta", "rank_version", System.currentTimeMillis()+"");
	}

	@Override
	public boolean refresh() {
		//主服务器负责定时刷新排行榜
		if(ServerConfig.mainServer) {
			loadFromDB();
		}
		
		String ver = redisClient.hget("ddz_meta", "rank_version");
		if(StringUtils.isBlank(ver)) {
			return false;
		}
		if(this.version != Long.valueOf(ver)) {
			this.version = Long.valueOf(ver);
			return true;
		}
		return false;
	}

	public void addRankPoint(int userId, int rankPoint, long rankGroupTime) {
		UserRank rank = userRankDao.getUserRank(userId, getRankConfig().id, rankGroupTime);
		if (rank == null) {
			rank = initUserRank(userId, rankPoint);
		} else {
			rank.setMtime(new Date());
			rank.setRankPoint(rank.getRankPoint() + rankPoint);
			userRankDao.update(rank);
		}
	}

	protected UserRank initUserRank(int userId, int rankPoint) {
		UserRank rank;
		rank = new UserRank();
		rank.setAuditId(0L);
		rank.setCtime(new Date());
		rank.setMtime(new Date());
		rank.setRank(0);
		rank.setRankPoint(rankPoint);
		rank.setUserId(userId);
		rank.setGroupId(getRankConfig().id);
		rank.setRankGrpTime(getRankGroupTime());
		userRankDao.insert(rank);
		return rank;
	}

	private RankModel dbModel2CacheModel(UserRank rank) {
		User user = userDao.getUser(rank.getUserId());
		RankModel rankModel = new RankModel();
		rankModel.playerId = rank.getUserId();
		rankModel.rankPoint = rank.getRankPoint();
		rankModel.name = user.getNickname();
		rankModel.mtime = new Date();
		rankModel.img = user.getHeadImg();
		return rankModel;
	}

	@Override
	public List<RankModel> getRank(int userId) {
		RankConfig rankConfig = getRankConfig();
		String key = rankConfig.pointType + "_rank";
		Set<String> items = redisClient.zrange(key, 0, rankConfig.rankLimit);
		TreeSet<RankModel> set = new TreeSet<RankModel>(new Comparator<RankModel>() {
			@Override
			public int compare(RankModel o1, RankModel o2) {
				return o1.rank - o2.rank;
			}			
		});
		for(String item : items) {
			RankModel model = new Gson().fromJson(item, RankModel.class);
			set.add(model);
		}
		return new ArrayList<RankModel>(set);
	}

	protected abstract long getRankGroupTime();

	protected abstract RankConfig getRankConfig();
}
