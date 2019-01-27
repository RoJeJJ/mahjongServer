package com.buding.api.context;



/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class GameContext {
	public long gameStartTime;
	public long handNum; //已进行游戏局数
	public long nextHandNum; //已进行游戏局数
	public long quanNum; //已进行圈数
	public long nextQuanNum; //已进行圈数
	
	public int bankerPos;
	public int winerPos;
	public FinalResult playerFinalResult = null;
	public HandResult playerHandResults = null;
}
