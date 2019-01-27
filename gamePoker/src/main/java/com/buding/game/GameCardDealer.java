package com.buding.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.api.desk.Desk;
import com.buding.card.ICardLogic;

public abstract class GameCardDealer<T extends Desk> {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected GameData  mGameData = null;
	protected T mDesk = null;
	protected ICardLogic mCardLogic = null;
	public void Init(GameData  data, T desk, ICardLogic logic){
		this.mGameData = data;
		this.mDesk = desk;
		this.mCardLogic = logic;
	}
	
	//洗牌
	public abstract void dealCard();
	
	//处理宝牌
	public abstract void dealPublicCard();
}
