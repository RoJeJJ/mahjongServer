package com.buding.api.player;

import java.io.Serializable;


public class PlayerInfo implements Serializable {
	private static final long serialVersionUID = -8011061375263995942L;
	
	public int playerId;// /玩家唯一id
	public boolean isGoodPlayer;// 好玩家

	public int position = -1; // /玩家在桌子上的位置

	public int coin = 10000; // 玩家金币

//	public int rankPoint = 1000; // /排位分

	public String name;
	public String headImg;

	public int gender; // 0 女 1男
	public int fanka;
	public int bindedMobile; // 0 没有 1已绑定
	public int userType; // 0 游客, 其它待定
//	public int winCount;
//	public int loseCount;
	public int vipType; // 0普通用户,其它待定
	public int status = 0;
	public int integral = 0; // 积分
	public int zongzi = 0; // 粽子
	public String roomId; // 当前房间id
	public String privateRoomId;// 专属房间id
	
	public transient int robot = 0; // //1表示机器人
	public transient int score = 0; //积分, 多局游戏计分用到

	public int kpzCount = 0; // 开牌炸的次数
	public int mobaoCount = 0; // 摸宝的次数
	public int baozhongbaoCount = 0; // 宝中宝次数
	public int zhuangCount = 0; // 做庄次数
	public int dianpaoCount = 0; // 点炮次数
	public boolean hangup = false;
	public boolean isbuy = false;
	public int piao = 0;

	public int getKpzCount() {
		return kpzCount;
	}

	public void setKpzCount(int kpzCount) {
		this.kpzCount = kpzCount;
	}


	public int getMobaoCount() {
		return mobaoCount;
	}

	public void setMobaoCount(int mobaoCount) {
		this.mobaoCount = mobaoCount;
	}

	public int getBaozhongbaoCount() {
		return baozhongbaoCount;
	}

	public void setBaozhongbaoCount(int baozhongbaoCount) {
		this.baozhongbaoCount = baozhongbaoCount;
	}

	public int getZhuangCount() {
		return zhuangCount;
	}

	public void setZhuangCount(int zhuangCount) {
		this.zhuangCount = zhuangCount;
	}

	public int getDianpaoCount() {
		return dianpaoCount;
	}

	public void setDianpaoCount(int dianpaoCount) {
		this.dianpaoCount = dianpaoCount;
	}

	public int getPlayerID() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getTablePos() {
		return position;
	}

	public boolean isRobot() {
		return robot ==1;
	}

	public boolean isGoodPlayer() {
		return isGoodPlayer;
	}

	public void setGoodPlayer(boolean goodPlayer) {
		isGoodPlayer = goodPlayer;
	}
}