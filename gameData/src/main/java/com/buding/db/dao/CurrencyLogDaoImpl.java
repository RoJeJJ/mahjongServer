package com.buding.db.dao;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.db.model.UserCurrencyLog;
import com.buding.hall.module.currency.dao.CurrencyLogDao;
import org.springframework.stereotype.Component;

@Component("currencyLogDao")
public class CurrencyLogDaoImpl extends CachedServiceAdpter implements CurrencyLogDao {
	
	@Override
	public void insertLog(UserCurrencyLog log) {
		this.commonDao.save(log);
	}
}
