package com.buding.hall.module.order.dao;

import com.buding.db.model.UserOrder;

public interface UserOrderDao {
	void insert(UserOrder order);
	void update(UserOrder order);
	UserOrder getByOrderId(String id);
}
