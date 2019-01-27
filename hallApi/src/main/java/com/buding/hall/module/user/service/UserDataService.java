package com.buding.hall.module.user.service;

import com.buding.db.model.UserData;

/**
 * 存放用户数据
 * @author Administrator
 *
 */
public interface UserDataService {
	
	int getIntVal(String key, int userId);

	void incrIntVal(String key, int userId, int val);
	
	UserData getUserDataItem(String key, int userId);
}