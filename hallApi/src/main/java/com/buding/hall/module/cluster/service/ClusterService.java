package com.buding.hall.module.cluster.service;

import com.buding.hall.module.cluster.model.ServerState;
import com.buding.hall.module.cluster.model.UserGaming;


public interface ClusterService {
	//更新用户正在游戏消息
	void updateUserGaming(int userId, String gameId, String serverInstanceId);

	void removeUserGaming(int userId, String gameId, String serverInstanceId) ;
	
	//获取用户正在哪个游戏服游戏
	UserGaming getUserGaming(int userId);
		
	//更新游戏服状态
	void updateServerStatus(ServerState server);
}