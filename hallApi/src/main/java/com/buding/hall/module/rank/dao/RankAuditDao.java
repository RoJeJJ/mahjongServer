package com.buding.hall.module.rank.dao;

import com.buding.db.model.RankAudit;

public interface RankAuditDao {
	RankAudit getByParams(String gameId, long date);
	long insert(RankAudit audit);
}
