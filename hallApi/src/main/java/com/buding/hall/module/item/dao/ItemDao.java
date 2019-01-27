package com.buding.hall.module.item.dao;

import com.buding.db.model.UserItem;

public interface ItemDao {
	UserItem getUserItem(int userId, int itemType);
	
	void updateUserItem(UserItem item);
	
	UserItem insert(UserItem item);
	
	UserItem getById(long id);
}
