package com.buding.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.buding.api.player.vo.PlayerGameInfo;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.api.context.PlayFinalResult;
import com.buding.api.context.PlayHandResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.mj.constants.MJConstants;
import com.buding.mj.helper.MJHelper;
import com.buding.mj.model.ActionWaitingModel;
import com.google.gson.GsonBuilder;

public class GameData extends GameDataBase {
	private Logger log = LoggerFactory.getLogger(getClass());

	public GameData() {
		super();

		this.Reset();
	}

	/*
	 * 除了玩家信息的其它所有数据重置
	 */
	public void Reset() {
		gangCards.mGangCount.clear();
		gangCards.aGangCount.clear();
		handStartTime = 0;
		handEndTime = 0;
		baoChangeNum = 0;
		currentOpertaionPlayerIndex = 0;
		currentCard = 0;
		cardOpPlayerIndex = 0;
		waitingStartTime = 0;
		waitingPlayerOperate = null;
		tingpls.clear();
		state = MJConstants.TABLE_STATE_INVALID;
		playSubstate = 0;
		dice1 = 0;
		dice2 = 0;
		gameSeq = 0;
		sleepTo = 0;
		seq = new AtomicInteger(1);
		super.Reset();
		
		
	}

	// 已玩局数
	public int handNum = 0;

	// 当前第几圈了
	public int quanNum = 1;//
	
	// 总共多少圈的房间
//	public int quanTotal = 0;

	// 本局开始时间-结束时间
	public long handStartTime = 0L;
	public long handEndTime = 0L;
	private int baoChangeNum = 0;// 宝牌换了多少次了；

	// 当前操作玩家位置
	private int currentOpertaionPlayerIndex;

	public byte currentCard = 0; // 当前打出来的牌
	private int cardOpPlayerIndex = 0; // 当前打牌的玩家
	private long waitingStartTime = 0L;
	public long startChangeBaoTime = 0;

	// public CurrentWaitingOperationPlayer;

	// 等待玩家的操作
	private ActionWaitingModel waitingPlayerOperate;

	// 听牌的玩家列表按听牌顺序存放
	private List<PlayerInfo> tingpls = new ArrayList<PlayerInfo>();
	public Map<String,PlayerGameInfo> playerGameInfoMap = Maps.newHashMap();

	private int state = MJConstants.TABLE_STATE_INVALID;
	private int playSubstate = 0;// 玩牌的时候的一些子状态，比如等待客户播动画，或者无操作
	public boolean replaying = false; //是否是回放局
	public int dice1 = 0; //骰子1
	public int dice2 = 0; //骰子2
	public int gameSeq = 0;
	public long showInitCardTime = 0;

	public long sleepTo = 0;
	public AtomicInteger seq = new AtomicInteger(1);
	
	public int genSeq() {
		return seq.getAndIncrement(); 
	}

	public void add_Down_cards(byte card) {
		for (int i = 0; i < this.mDeskCard.down_cards.size(); i++) {
			int cardvalueandnum = this.mDeskCard.down_cards.get(i).intValue();
			int cardvalue = (cardvalueandnum & 0xff);
			if (((byte) (cardvalue & 0xff)) == card) {
				int num = (cardvalueandnum >> 8) + 1;
				cardvalueandnum = (num << 8) | cardvalue;
				this.mDeskCard.down_cards.set(i, cardvalueandnum);
				break;
			}
		}
	}

	public void setWaitingStartTime(long waitingStartTime) {
		this.waitingStartTime = waitingStartTime;
	}

	public long getWaitingStartTime() {
		return waitingStartTime;
	}

	public ActionWaitingModel getWaitingPlayerOperate() {
		return waitingPlayerOperate;
	}

	public int getPlaySubstate() {
		return playSubstate;
	}

	public void setPlaySubstate(int playSubstate) {
		this.playSubstate = playSubstate;
	}

	public Byte getCurrentCard() {
		return currentCard;
	}

	public int getCardOpPlayerIndex() {
		return cardOpPlayerIndex;
	}

	public void move2NextPlayer(MJDesk desk) {
		List<PlayerInfo> loopGetPlayer = desk.loopGetPlayer(currentOpertaionPlayerIndex, 1, 0);
		currentOpertaionPlayerIndex = loopGetPlayer.get(0).position;
	}
	
	public int getBaoChangeNum() {
		return baoChangeNum;
	}

	public void setBaoChangeNum(int baoChangeNum) {
		this.baoChangeNum = baoChangeNum;
	}

	// 最后一个牌能胡
	public boolean isNextInFinalStage() {
		int iNum = 12;

		// 如果是换过宝，就多一只
		if (this.getBaoChangeNum() % 2 == 1) {
			iNum = 13;
		}

		if (getCardLeftNum() <= (iNum - 1))
			return true;

		return false;
	}

	// 是否已经进入最后阶段，最后要剩8，9张牌
	public boolean isInFinalStage() {
		int iNum = 12;

		// 如果是换过宝，就多一只
		if (this.getBaoChangeNum() % 2 == 1) {
			iNum = 13;
		}

		if (getCardLeftNum() <= iNum)
			return true;

		return false;
	}

	// 还剩多少张牌
	public int getCardLeftNum() {
		int num = this.mDeskCard.cards.size();
		return num;
	}

	public void setCurrentCard(Byte currentCard) {
		this.currentCard = currentCard;
	}

	public void setCardOpPlayerIndex(int cardOpPlayerIndex) {
		this.cardOpPlayerIndex = cardOpPlayerIndex;
	}

	public void setWaitingPlayerOperate(ActionWaitingModel waitingPlayerOperate) {
		this.waitingPlayerOperate = waitingPlayerOperate;
		
	}

	// 摸一张牌给玩家
	public byte popCard() {
		Byte b = 0;
		if (this.mDeskCard.cards.size() > 0)
			b = this.mDeskCard.cards.remove(0);

		return b;
	}

	public int getCurrentOpertaionPlayerIndex() {
		return currentOpertaionPlayerIndex;
	}

	public void setHandEndTime(long handEndTime) {
		this.handEndTime = handEndTime;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean havePlayerTingCard() {
		return !tingpls.isEmpty();
	}

	public List<PlayerInfo> getTIngPls() {
		return tingpls;
	}

	public int getState() {
		return state;
	}

	public void addTingPl(PlayerInfo player) {
		mTingCards[player.position].tingCard = true;
		tingpls.add(player);
	}

	public void setCurrentOpertaionPlayerIndex(int currentOpertaionPlayerIndex) {
		this.currentOpertaionPlayerIndex = currentOpertaionPlayerIndex;
	}

	public List<Byte> getCardsInHand(int position) {
		return this.mPlayerCards[position].cardsInHand;
	}

	public List<Integer> getCardsDown(int position) {
		return this.mPlayerCards[position].cardsDown;
	}

	public List<Integer> getAnGangCardsDown(int position) {
		return this.mPlayerCards[position].aGangCards;
	}

	public boolean has3SameCards(byte card, int position) {
		// 判断是否手牌有3张与目标牌一样
		int sames = 0;
		for (byte tempCard : getCardsInHand(position)) {
			if (tempCard == card) {
				sames++;
			}
		}
		if (sames == 3) {
			return true;
		}

		return isPengCard(card, position);
	}

	public boolean isPengCard(byte b, int position) {
		for (int i = 0; i < getCardsDown(position).size(); i++) {
			int bb = getCardsDown(position).get(i);
			byte b1 = (byte) (bb & 0xff);
			byte b2 = (byte) ((bb >> 8) & 0xff);
			byte b3 = (byte) ((bb >> 16) & 0xff);

			if (b1 == b && b2 == b && b3 == b) {
				return true;
			}
		}
		return false;
	}

	public void addCardInHand(byte b, int position, CardChangeReason reason) {
		if(b == 0) {
			return;
		}
		log.info("act=addCardInHand;reason={};position={};card={}", reason, position, b);
		List<Byte> cardsInHand = getCardsInHand(position);
		int pre = cardsInHand.size();
		try {
			if (b == 0) {
				throw new RuntimeException();
			}
			//
			MJHelper.add2SortedList(b, cardsInHand);
		} finally {
			int after = cardsInHand.size();
//			Assert.isTrue(after == pre + 1, "AddCardInHand:"+reason);
		}
	}

	// 从玩家手里取走一张牌
	public Byte removeCardInHand(int in_card, int position, CardChangeReason reason) {
		if(in_card == 0) {
			return 0;
		}
		log.info("act=addCardInHand;reason={};position={};card={}", reason, position, in_card);
		List<Byte> cardsInHand = getCardsInHand(position);
		int pre = cardsInHand.size();
		try {
			byte value = (byte) (in_card & 0xff);
			if (value == 0) {
				throw new RuntimeException();
			}
			Byte b = 0;			
			for (int i = 0; i < cardsInHand.size(); i++) {
				byte bb = cardsInHand.get(i);
				if (bb == value) {
					b = bb;
					cardsInHand.remove(i);
					break;
				}
			}
			return b;
		} finally {
			int after = cardsInHand.size();
//			Assert.isTrue(after == pre - 1, "RemoveCardInHand:"+reason+" "+ in_card);
		}
	}

	// 玩家手里有几张牌
	public int getCardNumInHand(int position) {
		return getCardsInHand(position).size();
	}

	// 查询手里某张牌，有多少张
	public int getXCardNumInHand(int value, int position) {
		int num = 0;
		List<Byte> cardsInHand = getCardsInHand(position);
		for (int i = 0; i < cardsInHand.size(); i++) {
			byte bb = cardsInHand.get(i);
			if (bb == value) {
				num++;
			}
		}
		//
		return num;
	}

	// 查询手里的第几张牌
	public Byte findCardInHand(int value, int position) {
		Byte b = 0;
		List<Byte> cardsInHand = getCardsInHand(position);
		for (int i = 0; i < cardsInHand.size(); i++) {
			byte bb = cardsInHand.get(i);
			if (bb == value) {
				b = bb;
				break;
			}
		}
		//
		return b;
	}
	
	// 在玩家门前放一张吃碰杠牌
	public int addCardDown(int c1, int c2, int c3, boolean isChi, int position) {
		List<Integer> list = getCardsDown(position);
		
		int card = MJHelper.addCardDown(c1, c2, c3, isChi, list);
		return card;
	}

	// 在玩家门前放明杠牌
	public int addMingGangCardDown(int c1,  int position) {
		List<Integer> list = getCardsDown(position);

		int card = MJHelper.addGangCardDown(c1,  list);
		return card;
	}

	// 在玩家门前放暗杠牌
	public int addAnGangCardDown(int c1,  int position) {
		List<Integer> list = getAnGangCardsDown(position);
		list.add(c1);
		return c1;
	}

	// 是否门清
	public boolean isMenQing(int position) {
		if (getCardsDown(position).size() <= 0)
			return true;
		//
		return false;
	}

	// 在玩家门前放一张牌
	public void addCardBefore(Byte b, int position) {
		getCardsBefore(position).add(b);
	}

	// 返回所有玩家看的见的牌的，这牌的数量
	public int getVisibleCardNum(byte b, int position) {
		int num = 0;
		List<Integer> cardsDown = getCardsDown(position);
		for (int i = 0; i < cardsDown.size(); i++) {
			int bb = cardsDown.get(i);
			byte b1 = (byte) (bb & 0xff);
			byte b2 = (byte) ((bb >> 8) & 0xff);
			byte b3 = (byte) ((bb >> 16) & 0xff);

			if (b1 == b)
				num++;
			if (b2 == b)
				num++;
			if (b3 == b)
				num++;
		}
		List<Byte> cardsBefore = getCardsBefore(position);
		for (int i = 0; i < cardsBefore.size(); i++) {
			byte bb = cardsBefore.get(i);
			if (bb == b)
				num++;
		}

		return num;
	}

	public byte getCard(int idx, int position) {
		List<Byte> cardsInHand = getCardsInHand(position);
		if (idx < 0 || idx >= cardsInHand.size())
			return 0;
		//
		byte b = cardsInHand.get(idx);

		return b;
	}

	public List<Byte> getCardsBefore(int position) {
		return this.mPlayerCards[position].cardsBefore;
	}

	public String dump() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(recorder);
	}
	
	public void mergeHandResult(PlayFinalResult finalRes, PlayHandResult handRes) {
		finalRes.pos = handRes.pos;
		finalRes.score += handRes.score;
		if ((handRes.fanType & MJConstants.MAHJONG_HU_CODE_WIN) == MJConstants.MAHJONG_HU_CODE_WIN) {
			finalRes.huCount++;
			if ((handRes.fanType & MJConstants.MAHJONG_HU_CODE_MO_BAO) == MJConstants.MAHJONG_HU_CODE_MO_BAO) {
				finalRes.mobaoCount++;
			}
			
			if ((handRes.fanType & MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) == MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) {
				finalRes.baoZhongBaoCount++;
			}
			
			if ((handRes.fanType & MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) == MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) {
				finalRes.kaiPaiZhaCount++;
			}
		} else {
			if ((handRes.fanType & MJConstants.MAHJONG_HU_CODE_DIAN_PAO) == MJConstants.MAHJONG_HU_CODE_DIAN_PAO) {
				finalRes.paoCount++;
			}
		}
		
		if ((handRes.fanType & MJConstants.MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA) == MJConstants.MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA) {
			finalRes.bankerCount++;
		}
	}
}
