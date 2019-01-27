package com.buding.hall.module.ws;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public interface BattlePortalBroadcastService {
	// 设置回放数据
	Result setDeskRelayData(String instanceId, int playerId, String data);

	// 停止服务
	void stopService(String instanceId);

	// 恢复服务
	void startService(String instanceId);

	// 剔除玩家
	void kickPlayer(String instanceId, int playerId);

	// 获取桌子列表
	String getDeskList(String instanceId);

	// 重新加载配置
	void reloadMatchConf(String serverPattern);

	// 停服
	void stopServer(String instanceId);

	// 清除卡桌
	Result clearDesk(String instanceId, int userId);

	//结算桌子
	Result dismissDesk(String instanceId, String gameId, String matchId, String deskId);
	
	TResult<String> dump(String instanceId, String gameId, String matchId, String deskId);
	
	String getStatus();
	
	String searchDesk(int playerId);
}
