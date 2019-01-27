package com.buding.hall.module.award.dao;

import java.util.List;
import java.util.Map;

import com.buding.db.model.Award;
import com.buding.db.model.UserAward;
import org.springframework.stereotype.Component;


public interface AwardDao {
	long insert(Award award);
	Award getAward(long id);
	void insertUserAward(UserAward ua);
	void updateUserAward(UserAward ua);
	List<UserAward> getUserAward(int userid);
	Map<Long, UserAward> getUserAwardMap(int userid);
	UserAward get(long awardId, int userid);
}
