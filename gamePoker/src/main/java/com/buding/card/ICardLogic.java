package com.buding.card;

import com.buding.api.player.vo.PlayerGameInfo;
import packet.mj.MJ.GameOperPlayerActionSyn;

import com.buding.api.desk.Desk;
import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameCardDealer;
import com.buding.game.GameData;


public interface ICardLogic<T extends Desk> {
	
	//组件初始化
	void init(GameData gameData, T desk);
	
	//查找开牌炸玩家(不良设计，需要改造)
	boolean tryKaipaiZha(GameData gameData, T desk);
		
	//调试接口
	void handleSetGamingData(GameCardDealer mCardDealer, GameData gameData, T desk, String json);
	
	//主循环
	void gameTick(GameData data, MJDesk desk);
	
	//发牌
	void sendCards(GameData data, MJDesk desk);
	
	//设置下一个玩家做庄家
	void selectBanker(GameData data, MJDesk desk);
	
	//提示玩家出牌	
	void player_chu_notify(GameData gameData, T desk);
	
	//重新通知玩家操作
	void re_notify_current_operation_player(GameData gameData, T desk, int position);
	
	//玩家操作(出、碰、杠、听等)
	void playerOperation(GameData gameData, MJDesk gt, GameOperPlayerActionSyn.Builder msg, PlayerInfo pl);
	
	//是否打完一个圈
	boolean isFinishQuan(GameData gameData, MJDesk gt);
	
	//服务器托管自动操作
	void playerAutoOper(GameData gameData, MJDesk gt, int position);
	
	//重新推送玩家数据,用于断线重连
	void repushGameData(GameData gameData, MJDesk desk, int position);

	void GoodPlayerSeeCard(int playerId);

    void GoodPlayerChangeCard(int card,PlayerInfo Info,MJDesk desk);
}
