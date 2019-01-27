package com.buding.hall.module.ws;

import java.util.Date;
import java.util.List;

import com.buding.common.cluster.model.ServerModel;
import com.buding.common.result.Result;
import com.buding.db.model.GameLog;
import com.buding.db.model.User;
import com.buding.db.model.UserRoom;
import com.buding.hall.module.cluster.model.ServerState;
import com.buding.hall.module.cluster.model.UserGaming;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.task.vo.GamePlayingVo;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public interface HallPortalService {
	//更新用户正在游戏的数据
	void updateUserGaming(int userId, String gameId, String serverInstanceId);

	//移除
	void removeUserGaming(int userId, String gameId, String serverInstanceId);

	//分布式环境下更新服务器在线状态
	void updateServerStatus(ServerState server);

	//获取用户正在游戏的数据
	UserGaming getUserGaming(int userId);

	// 获取用户
	User getUser(int userId);

	// 初始化新用户
	User initUser();

	// 是否可以领取破产救济
	boolean isCanReceiveBankAssist(int userId);

	// 改变金币
	Result changeCoin(int userId, int coin, boolean check, ItemChangeReason reason);

	// 记录游戏结果
	User addGameResult(GamePlayingVo gameResult);

	//添加游戏记录
	void addGameLog(GameLog log);

	//是否有足够金币
	Result hasEnoughCurrency(int userId, int currenceType, int count);

	// 是否有足够的道具
	Result hasEnoughItem(int userId, String itemId, int count);

	// 改变房卡
	Result changeFangka(int userId, int coin, boolean check, ItemChangeReason reason);

	// 用户是否在线
	boolean isUserOnline(int userId);

	//授权
	Result auth(int userId);

	//取消授权
	Result cancelAuth(int userId);
	
	//重新加载配置
	Result reloadMallConf(String serverPattern);

	//添加房间
	Result addRoom(UserRoom room);

	//更新插入房间
	Result upsertRoom(UserRoom room);

	//获取我的房间
	UserRoom getMyRoom(int userId, String matchId);

	//获取我的房间 
	UserRoom getMyRoom(int userId, long roomId);

	//获取房间
	UserRoom getByRoomCode(String roomCode);

	//生成房间编号
	String genRoomUniqCode();
	
	//添加用户排行榜积分
	void addUserRankPoint(int userId, String userName, String gameId, int rankPoint, int rankType, Date date) throws Exception;
	
	//重置密码
	Result resetPasswd(int userId, String passwd);
	
	//改变用户类型
	Result changeUserType(int userId, int type);
	
	//获取所有服务列表
	List<ServerModel> getAllServer(String serverType);
	
	//启动服务
	void stopService(String instanceId);
	
	//停止服务
	void startService(String instanceId);
	
	//微信支付回调
	String onWxPayCallback(String xml) throws Exception;
	
	String onAliPayCallback(String xml) throws Exception;
}
