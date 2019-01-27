package com.buding.hall.module.award.service;

import com.buding.common.result.Result;
import com.buding.db.model.Award;

public interface AwardService {
	long addAward2User(int userId, Award award);
	void addAward2User(int userId, long awardId);
	long addAward(Award award);
	Result receiveAward(int userId, long awardId);
}
