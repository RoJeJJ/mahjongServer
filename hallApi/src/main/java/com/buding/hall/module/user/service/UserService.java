package com.buding.hall.module.user.service;

import com.buding.common.result.Result;
import com.buding.db.model.User;
import com.buding.hall.module.common.constants.CurrencyType;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.task.vo.GamePlayingVo;

public interface UserService {

	//初始化新用户
	User initUser();

	//记录游戏结果
	User addGameResult(GamePlayingVo gameResult);

	//获取用户
	User getUser(int userId);
	
	//更新用户
	void updateUser(User user);
	
	//是否可以领取破产救济
	boolean isCanReceiveBankAssist(int userId);
	
	//注册
	Result register(User user);
	
	//绑定手机
	Result bindMobile(User user, String phone);
	
	//登录
	User login(String username, String password);
	
	//根据用户名获取
	User getByUserName(String username);
	
	//登录成功回调
	void onUserLogin(User user);
	
	void onUserLogout(int userId);
	
	/**
	 * 是否有足够的货币
	 * @param userId
	 * @param currenceType @see {@link CurrencyType}
	 * @param count
	 * @return
	 */
	Result hasEnoughCurrency(int userId, int currenceType, int count);
	
	//是否有足够的道具
	Result hasEnoughItem(int userId, String itemId, int count);
	
	//改变金币
	Result changeCoin(int userId, int coin, boolean check, ItemChangeReason reason);
	
	//改变房卡
	Result changeFangka(int userId, int coin, boolean check, ItemChangeReason reason);
	
	//用户是否在线
	boolean isUserOnline(int userId);
	
	//授权
	Result auth(int userId);
	
	//取消授权
	Result cancelAuth(int userId);
	
	//重置密码
	Result resetPasswd(int userId, String passwd);
	
	//改变用户类型
	Result changeUserType(int userId, int type) ;
}
