package com.buding.game;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class GameParam {
	public boolean autoOperWhenTimeout = false; //实际超时时间
	public int chiPengPlayMills = 600; //吃碰动画播放时间(毫秒)
	public int gangPlayMills = 1500; //杠动画播放时间(毫秒)
	public int chuPlayMills = 1000;  //出牌动画播放时间(毫秒)
	public int operTimeOutSeconds = 12; //界面提示用户操作时间(秒)
	public int thinkMills4AutoOper = 600;// 自动托管时，每次出牌思考时间(毫秒)
	public int sendCardPlayMills = 3000; //发牌动画播放时间
	public int changeBaoMills = 2000; //换宝动画播放时间
	public int totalQuan = 0;
}