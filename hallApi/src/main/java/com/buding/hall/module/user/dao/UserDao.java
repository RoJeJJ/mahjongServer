package com.buding.hall.module.user.dao;

import java.util.List;

import com.buding.db.model.GameLog;
import com.buding.db.model.User;
import com.buding.db.model.UserGameOutline;
import com.buding.hall.module.task.vo.GamePlayingVo;

public interface UserDao {
	User insert(User user);
	User getUser(int userId);
	void updateUser(User user);
	User getUserByUserName(String userName);
	List<User> getRobotListByMatchId(String matchId);
	List<User> getRobotListByIdRange(int start, int end);
	User getUserByOpenId(String openId);
	void addGameResult(GamePlayingVo gameResult) throws Exception;
	UserGameOutline getUserGameOutline(int userId);
	
	void addGameLog(GameLog log);
	void addUserOnlineData(int userId, int day, int minute);
}
