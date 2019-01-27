package com.buding.hall.module.exchange.dao;

import java.util.List;

import com.buding.db.model.DayExchange;
import com.buding.db.model.UserExchange;

public interface ExchangeDao {
	void insertExchange(UserExchange ue);
	void insertDayExchange(DayExchange de);
	void updateExchange(UserExchange ue);
	void updateDayExchange(DayExchange de);
	UserExchange get(long id);
	List<UserExchange> getByUserId(int userId);
	List<DayExchange> getDayChangeList();
	DayExchange getByConfAndDay(String confId, int day);
}
