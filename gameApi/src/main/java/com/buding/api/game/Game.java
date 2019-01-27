package com.buding.api.game;

import com.buding.api.player.vo.PlayerGameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.api.desk.Desk;
import com.buding.api.player.PlayerInfo;

public abstract class Game {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());

	public abstract void setDesk(Desk desk, String params);
	
	public abstract void setGameParam(String params);

    public abstract void playerSit(PlayerInfo player);
    
    public abstract void playerAgree(PlayerInfo player);
    
    public abstract void playerExit(PlayerInfo player);
    
    public abstract void playerAway(PlayerInfo player);
    
    public abstract void playerComeBack(PlayerInfo player);
    
    public abstract void hangeUp(PlayerInfo player);
    
    public abstract void cancelHangeUp(PlayerInfo player);
        
    public abstract void handleGameMsg(int position, Object content);
    
    public abstract void gameBegin();
    
    public abstract void onTimer(int timerID);
    
    public abstract void playerOffline(PlayerInfo player);
    
    public abstract void playerReconnect(PlayerInfo player);
    
//    public abstract void dismiss();
    
    public abstract String dumpGameData();

	public abstract void setGamingDate(String json);
	
	public abstract void gameDismiss();
	
	public abstract void gamePause();
	
	public abstract void gameResume();

    public abstract void  playerMaiPiao(PlayerGameInfo playerGameInfo);

    public abstract void playerSeeBestCard(PlayerInfo info);

    public abstract void playerChangeBestCard(PlayerInfo info,int card);
}

