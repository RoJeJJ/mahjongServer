package com.buding.db.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.GameLog;
import com.buding.db.model.User;
import com.buding.db.model.UserDayReport;
import com.buding.db.model.UserGameOutline;
import com.buding.hall.module.task.vo.GamePlayingVo;
import com.buding.hall.module.user.dao.UserDao;
import org.springframework.stereotype.Component;

@Component("userDao")
public class UserDaoImpl extends CachedServiceAdpter implements UserDao{
	private final DbService dbService;

	@Autowired
	public UserDaoImpl(DbService dbService) {
		this.dbService = dbService;
	}

//	@Autowired
//	UserMapper userMapper;
	
	@Override
	public User getUser(int userId) {
		return this.get(userId, User.class);
	}

	@Override
	public void updateUser(User model) {
		this.put2EntityCache(model);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(model);
		} else {
			this.dbService.submitUpdate2Queue(model);
		}
	}

	@Override
	public User getUserByUserName(String userName) {
		String sql = "select * from user where user_name = ? ";
		User user = this.commonDao.selectOne(sql, User.class, userName);
		if(user != null) {
			user = getUser(user.getId());
		}
		return user;
	}

	@Override
	public User insert(User user) {
		this.commonDao.save(user);
		return getUserByUserName(user.getUserName());
	}

	@Override
	public List<User> getRobotListByMatchId(String matchId) {
		String sql = "select * from user where binded_match = ? ";
		return this.commonDao.selectList(sql, User.class, matchId);
	}

	@Override
	public List<User> getRobotListByIdRange(int start, int end) {
		String sql = "select * from user where id >= ? and id <= ?";
		return getList(sql, User.class, start, end);
	}

	@Override
	public User getUserByOpenId(String openId) {
		return this.getOne("select * from user where wxopenid = ? ", User.class, openId);
	}

	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	@Override
	public void addGameResult(GamePlayingVo gameResult){
		UserGameOutline result;
		result = getUserGameOutline(gameResult.userId);
		
		if(result == null) {
			result = new UserGameOutline();
			result.setId(gameResult.userId);
			result.setMaxFanNum(0);
			result.setMaxFanType(0);
			result.setMaxFanDesc("");
			result.setContinueWinCount(0);
			result.setWinCount(0);
			result.setLoseCount(0);
			result.setTotalCount(0);			
			this.commonDao.save(result);
		}
		
		//加锁
		synchronized (result) {
			if(gameResult.winCount > 0) {
				result.setContinueWinCount(result.getContinueWinCount() + gameResult.continueWin);	
			} else {
				result.setContinueWinCount(0);
			}
			result.setWinCount(gameResult.winCount + result.getWinCount());
			result.setLoseCount(gameResult.loseCount + result.getLoseCount());
			result.setTotalCount(gameResult.winCount + gameResult.loseCount + gameResult.evenCount + result.getTotalCount());
			result.setLastGameTime(gameResult.gameTime);
			result.setLastGameMatch(gameResult.matchId);
			if(gameResult.maxFanNum >= result.getMaxFanNum() && gameResult.winCount > 0) {
				result.setMaxFanDesc(gameResult.maxFanDesc);
				result.setMaxFanType(gameResult.maxFanType);
				result.setMaxFanNum(gameResult.maxFanNum);
				result.setMaxFanDowncards(gameResult.maxDownCards);
				result.setMaxFanHandcards(gameResult.maxFanHandCards);
			}
		}
		
		this.put2EntityCache(result);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(result);
		} else {
			this.dbService.submitUpdate2Queue(result);
		}		
	}

	@Override
	public UserGameOutline getUserGameOutline(int userId) {
		return this.get(userId, UserGameOutline.class);
	}

	@Override
	public void addGameLog(GameLog log) {
		this.commonDao.save(log);
	}

	@Override
	public void addUserOnlineData(int userId, int day, int minute) {
		UserDayReport report = this.getOne("select * from user_day_report where user_id = ? and day = ? ", UserDayReport.class, userId, day);
		if(report == null) {
			report = new UserDayReport();
			report.setUserId(userId);
			report.setOnlineMinutes(minute);
			report.setDay(day);
			this.commonDao.save(report);
			return;
		}
		report.setOnlineMinutes(report.getOnlineMinutes() + minute);
		this.dbService.submitUpdate2Queue(report);
	}
}
