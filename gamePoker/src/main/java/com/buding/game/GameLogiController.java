package com.buding.game;

import com.buding.api.desk.Desk;
import com.buding.api.game.Game;
import com.buding.api.player.PlayerInfo;
import com.buding.api.player.vo.PlayerGameInfo;
import com.buding.game.events.*;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class GameLogiController extends Game {
protected GameStateDispatcher m_dispatcher = null; 
   
	
	@Override
	public void setDesk(Desk desk, String params) {
		
		if(null == desk){
			this.logger.error("desk is null");
			return;
		}
		
		
		if( null == this.m_dispatcher){
			
			this.logger.error("m_dispatcher is null");
			return;
		}
		
		this.m_dispatcher.SetDesk(desk);
		this.m_dispatcher.setGameParam(params);
	}
	
	@Override
	public void setGameParam(String params) {
		this.m_dispatcher.setGameParam(params);
	}

	@Override
	public void playerSit(PlayerInfo player) {
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_Sit;
		event.info = player;
		
		this.updatePlayerEvent(event);
	}

	@Override
	public void playerAgree(PlayerInfo player) {
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_Agree;
		event.info = player;
		
		this.updatePlayerEvent(event);
	}

	@Override
	public void hangeUp(PlayerInfo player) {
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_HangUp;
		event.info = player;
		
		this.updatePlayerEvent(event);
	}

	@Override
	public void cancelHangeUp(PlayerInfo player) {
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_Cancel_Hangup;
		event.info = player;
		
		this.updatePlayerEvent(event);
	}

	@Override
	public void playerExit(PlayerInfo player) {
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_Exit;
		event.info = player;
		
		this.updatePlayerEvent(event);
	}

	@Override
	public void playerAway(PlayerInfo player) {
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_Away;
		event.info = player;
		
		this.updatePlayerEvent(event);
	}

	@Override
	public void handleGameMsg(int position, Object content) {
		NetEvent event = new NetEvent();
		event.position = position;
		event.msg = content;
		this.updateNetEvent(event);
	}

	@Override
	public void gameBegin() {
		PlatformEvent event = new PlatformEvent();
		event.eventID = GameLogicEvent.Game_Begin;
		
		this.updatePlatformEvent(event);
		
	}

	@Override
	public void gameDismiss() {
		PlatformEvent event = new PlatformEvent();
		event.eventID = GameLogicEvent.Game_Dismiss;
		
		this.updatePlatformEvent(event);
	}

	@Override
	public void gamePause() {
		PlatformEvent event = new PlatformEvent();
		event.eventID = GameLogicEvent.Game_Pause;
		
		this.updatePlatformEvent(event);
	}

	@Override
	public void gameResume() {
		PlatformEvent event = new PlatformEvent();
		event.eventID = GameLogicEvent.Game_Resume;
		
		this.updatePlatformEvent(event);
	}


	@Override
	public  void  playerMaiPiao(PlayerGameInfo playerGameInfo){
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_MaiPiao;
		event.gameInfo = playerGameInfo;
		this.updatePlayerEvent(event);
	}

	@Override
	public  void  playerSeeBestCard(PlayerInfo playerInfo){
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_SeeBestCard;
		event.info = playerInfo;
		this.updatePlayerEvent(event);
	}

	@Override
	public  void  playerChangeBestCard(PlayerInfo playerInfo,int card){
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_ChangeBestCard;
		event.info = playerInfo;
		event.card = card;
		this.updatePlayerEvent(event);
	}

	@Override
	public void playerOffline(PlayerInfo player) {
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_Offline;
		event.info = player;
		
		this.updatePlayerEvent(event);
	}

	@Override
	public void playerReconnect(PlayerInfo player) {
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_Reconnect;
		event.info = player;
		
		this.updatePlayerEvent(event);
	}
	

	@Override
	public void playerComeBack(PlayerInfo player) {
		PlayerEvent event = new PlayerEvent();
		event.eventID = GameLogicEvent.Player_ComeBack;
		event.info = player;
		
		this.updatePlayerEvent(event);
	}

	@Override
	public void onTimer(int timerID) {
		TimerEvent event = new TimerEvent();
		event.timerID = timerID;
		this.updateTimerEvent(event);
	}
	
	/*
	 * 事件分发处理
	 */
	private void updatePlayerEvent(PlayerEvent event){
		this.m_dispatcher.HandlePlayerEvent(event);
	}

	private void updatePlatformEvent(PlatformEvent event){
		this.m_dispatcher.HandlePlatformEvent(event);
	}
	
	private void updateNetEvent(NetEvent event){
		this.m_dispatcher.HandleNetEvent(event);
	}
	
	private void updateTimerEvent(TimerEvent event){
		this.m_dispatcher.HandleTimerEvent(event);
	}

	@Override
	public String dumpGameData() {
		return this.m_dispatcher.mGameData.dump();
	}
	
	@Override
	public void setGamingDate(String gameData) {
		this.m_dispatcher.setGamingDate(gameData);
	}
}
