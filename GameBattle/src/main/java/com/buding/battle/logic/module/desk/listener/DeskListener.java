package com.buding.battle.logic.module.desk.listener;

import com.buding.api.game.Game;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
/**
 * 桌子事件接口
 * */
public interface DeskListener {
	//玩家加入房间找位置坐下
	void onPlayerSit(CommonDesk<?> desk, PlayerInfo player);
	void onPlayerReady(CommonDesk<?> desk, PlayerInfo player);
	void onPlayerLeave(CommonDesk<?> desk, PlayerInfo player);
	void onDeskGameStart(CommonDesk<?> desk, Game game);
	void onDeskGameFinish(CommonDesk<?> desk, Game game);
	void onDeskDestroy(CommonDesk<?> desk);
	void onDeskCreate(CommonDesk<?> desk);
}
