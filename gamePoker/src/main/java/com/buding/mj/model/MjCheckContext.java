package com.buding.mj.model;

import com.buding.api.desk.MJDesk;
import com.buding.game.GameData;

/**
 * @author tiny qq_381360993
 * @Description:
 * 检测听牌、胡牌的上下文
 * 
 */
public class MjCheckContext {
	public GameData gameData;
	public MJDesk desk;
	public byte card;
	public int position;
	public boolean debug; //是否调试模式,调试模式会打印详细日志. //TODO 这个参数应该放入gameData里面
	public boolean isQiangTing = false;
	
	public byte card2Remove = 0;
	public byte card2Ting = 0;
	
	public static MjCheckContext create(GameData gameData, MJDesk desk, byte card, int position, boolean debug, boolean isQiangTing) {
		MjCheckContext c = new MjCheckContext();
		c.gameData = gameData;
		c.desk = desk;
		c.card = card;
		c.position = position;
		c.debug = debug;
		c.isQiangTing = isQiangTing;
		return c;
	}

	public void setCard2Remove(byte card2Remove) {
		this.card2Remove = card2Remove;
	}

	public byte getCard2Ting() {
		return card2Ting;
	}
}
