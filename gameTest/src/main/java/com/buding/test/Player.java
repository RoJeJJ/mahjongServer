package com.buding.test;


/**
 * @author vinceruan qq_404086388
 * @Description:
 * 
 */
public interface Player {	
	boolean isInit();
	boolean isMsgInit();
	boolean isGameInit();
	boolean isLogin();
	boolean isAuth();
	boolean isGameAuth();
	boolean isMsgAuth();
	boolean isEnroll();
	boolean isReady();
	boolean hasAccount();
	boolean isInitInteractCmd();
			
	boolean init(String serverIp, int serverPort);
	boolean initMsg();
	boolean initGame();
	void login();
	void setAccount(String username, String passwd);
	void auth();
	void authMsg();
	void authGame();
	void gameHeatbeat();
	void hallHeatbeat();
	void msgHeatbeat();
	void enroll(String match, String roomCode);
	void ready();
	void loadAccount();
	void initInteractCmd();
	
	GameServerProxy getGameServerProxy();

	HallServerProxy getHallServerProxy() ;

	MsgServerProxy getMsgServerProxy();
	
	
}
