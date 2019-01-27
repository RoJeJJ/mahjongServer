package com.buding.hall.module.rank.dao;

import java.util.List;
import java.util.Set;

import com.buding.db.model.RankAudit;
import com.buding.db.model.UserRank;
import com.buding.db.model.UserRankDetail;

public interface UserRankDao {
	UserRankDetail getByType(int userId, String gameId, int pointType, long datetime);
	void update(UserRank rank);
	void insert(UserRank rank);
	void update(UserRankDetail rank);
	void insert(UserRankDetail rank);
	
	UserRank getUserRank(int userId, String rankGrpId, long rankGroupTime);
	List<UserRank> getRankList(String rankGrpId, long rankGroupTime, int size);
	
	/**
	 * 
	 * @param gameId
	 * @param startTime
	 * @param endTime
	 * @param pointType
	 * @return
	 */
	List<UserRank> assembleUserRank(String gameId, long startTime, long endTime, int pointType);
	List<UserRank> assembleOldRank(long auditId);
	Set<Integer> getAwardUserIdSet(long auditId);
	RankAudit getBySettleTime(String groupId, long settleTime);
	
}
