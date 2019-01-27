package com.buding.battle.logic.module.desk.bo;

import java.util.List;

import com.buding.api.desk.Desk;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.DeskStatus;
import com.buding.battle.logic.module.common.ParentAware;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.common.monitor.Monitorable;
import com.buding.hall.config.DeskConfig;
import com.buding.hall.module.game.model.DeskModel;
import packet.mj.MJ;

public interface CommonDesk<MsgType> extends Desk<MsgType>, Monitorable, ParentAware<Room> {   
	/**
	 * 玩家就位,返回玩家的座位索引
	 * @param ctx
	 */
	int playerSit(BattleContext ctx);
	
	/**
	 * 玩家离开
	 * @param playerId
	 */
	@Deprecated
	void playerExit(int playerId, PlayerExitType type);
	
	/**
	 * 销毁
	 */
	void destroy(DeskDestoryReason type);
	
	/**
	 * 桌子是否已空
	 * @return
	 */
	boolean isEmpty();
	
	/**
	 * 桌子是否已满
	 * @return
	 */
	boolean isFull();
	
	/**
	 * 检查是否可以开赛
	 */
	void tryStartGame();
	
	/**
	 * 收到用户准备数据包
	 * @param playerId
	 */
	void onPlayerReadyPacketReceived(int playerId);
	
	/**
	 * 换桌
	 * @param playerId
	 */
	void onPlayerChangeDeskPacketReceived(int playerId);
	
	/**
	 * 重连
	 * @param playerId
	 */
	void onPlayerReconnectPacketReceived(int playerId);
	
	/**
	 * 退出游戏
	 * @param playerId
	 */
	void onPlayerExitPacketReceived(int playerId);
	
	/**
	 * 离开游戏
	 * @param playerId
	 */
	void onPlayerAwayPacketReceived(int playerId);
	
	/**
	 * 回到游戏
	 * @param playerId
	 */
	void onPlayerComeBackPacketReceived(int playerId);
	
	/**
	 * 断线
	 * @param playerId
	 */
	void onPlayerOfflinePacketReceived(int playerId);
	
	/**
	 * 收到玩家解散请求
	 * @param playerId
	 */
	void onPlayerDissVotePacketReceived(int playerId, boolean agree);
	
	/**
	 * 收到游戏数据包
	 * @param playerID
	 * @param content
	 */
	void onGameMsgPacketReceived(int playerID, MsgType content);
	
	/**
	 * 收到聊天数据包
	 * @param playerID
	 * @param content
	 */
	void onChatMsgPacketReceived(int playerID, int contentType, byte[] content);
	
	void onPlayerHangupPacketReceived(int playerID);
	
	void onPlayerCancelHangupPacketReceived(int playerID);
	
	/**
	 * 收到踢人数据包
	 * @param playerId
	 * @param targetPlayerId
	 */
	void onKickoutPacketReceived(int playerId, int targetPlayerId);


	/**
	 * 收到买漂数据包
	 * @param data
	 * @param session
	 */
	void onPlayerMaiPiaoReceived(MJ.MaiPiaoSyn data, BattleSession session);

	/**
	 * 收到请求--获取底牌
	 * @param session
	 */
	void onPlayerSeeBestCardReceived(BattleSession session);

	/**
	 * 收到请求--更换牌
	 * @param session
	 */
	void onPlayerChangeBestCardReceived(BattleSession session,MJ.ChangeBestCards data);


	/**
	 * 获取玩家数量
	 * @return
	 */
	int getPlayerCount();
	
	/**
	 * 获取桌子状态
	 * @return
	 */
	DeskStatus getStatus();
	
	/**
	 * 是否自动换桌
	 * @return
	 */
	boolean isAutoChangeDesk();
	
	/**
	 * 重置桌子状态
	 */
	void reset();
	
	List<PlayerInfo> getPlayers();
	
	DeskConfig getDeskConfig();
	
	void setDeskConfig(DeskConfig conf);
	
	void setDeskOwner(int ownerId);
	
	double getDeskDelayStatus();
	
	void kickout(int playerId, String msg);
	
	void onDismissPacketRequest();
	
	void setDeskId(String id);
	
	boolean isAutoReady();
	
	void onSetGamingDataReq(String json);
	
	void markAsAdminUse();
	
	boolean isAdminUse();
	
	String dumpGameData();
	
	int getGameCount();
	
	DeskModel getDeskInfo();
	
	String printGameDetail();
	
	boolean isHasPlayer(int playerId);
}
