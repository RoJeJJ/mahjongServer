package com.buding.game.events;

public class GameLogicEvent {

	/*
	 * 玩家事件
	 */
	public static final int Player_Sit = 1;
	public static final int Player_Agree = 2;
	public static final int Player_Exit = 3;
	public static final int Player_Offline = 4;
	public static final int Player_Reconnect = 5;
	public static final int Player_Away = 6;
	public static final int Player_ComeBack = 7;
	public static final int Player_HangUp = 8;
	public static final int Player_Cancel_Hangup = 9;
	public static final int Player_MaiPiao = 10;
	public static final int Player_SeeBestCard = 11;
	public static final int Player_ChangeBestCard = 12;

	
	/*
	 * 平台事件
	 */
	public static final int Game_Begin = 20;
	
	public static final int Game_Pause = 21;
	
	public static final int Game_Resume = 22;
	
	public static final int Game_Dismiss = 23;
	
}
