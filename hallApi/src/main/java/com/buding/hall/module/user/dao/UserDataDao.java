package com.buding.hall.module.user.dao;

import com.buding.db.model.UserData;

public interface UserDataDao {
	long insert(UserData data);
	void update(UserData data);
	UserData get(int userId, String key);
}
