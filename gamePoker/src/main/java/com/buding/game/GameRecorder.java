package com.buding.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buding.api.player.PlayerInfo;
import com.buding.mj.helper.MJHelper;
import com.buding.mj.model.Card;
import packet.mj.MJ;

public class GameRecorder {
	public List<Card> initCards = new ArrayList<>();
	public Map<Integer, List<Card>> playerInitCards = new HashMap<Integer, List<Card>>();
	public List<Action> actionList = new ArrayList<Action>();
	public byte baoCard = 0;
	public boolean isContinueBanker = false;
	public int currentHuPlayerIndex = -1;
	public int bankerPos = -1;
	public int bankerUserId = -1;
	public int dice1 = -1;
	public int dice2 = -1;
	public int seq = -1;
	public String deskId;
	public PlayerInfo[] players;
	public MJ.GameOperPlayerHuSyn.Builder playerResultDetail ;

	public void reset() {
		this.initCards.clear();
		this.playerInitCards.clear();
		this.actionList.clear();
	}
		
	public void recordBasicInfo(GameData gt)
	{
		isContinueBanker = gt.mPublic.isContinueBanker;
		bankerPos = gt.mPublic.mbankerPos;
		bankerUserId = gt.mPublic.mBankerUserId;
		dice1 = gt.dice1;
		dice2 = gt.dice2;
	}

	public void recordHuInfo(int position)
	{
		currentHuPlayerIndex = position;
	}

	public void recordHuInfo(int position,int test)
	{
		currentHuPlayerIndex = position;
	}
	
	public List<Byte> getInitCardCodeList() {
		List<Byte> list = new ArrayList<Byte>();
		for(Card c : initCards) {
			list.add((byte)(c.code & 0XFF));
		}
		return list;
	}
	
	public void recordGameStart(PlayerInfo[] players, byte baoCard, List<Byte> initCards) {
		this.baoCard = baoCard;
		for(byte b : initCards) {
			this.initCards.add(new Card(b, MJHelper.getSingleCardName(b)));
		}
		this.players = players;
	}
	
	public void recordPlayerCard(int position, List<Byte> cards) {
		List<Card> initCards = new ArrayList<Card>();
		for(byte b : cards) {
			initCards.add(new Card(b, MJHelper.getSingleCardName(b)));
		}
		playerInitCards.put(position, initCards);
	}
	
	public Action recordPlayerAction(int seq, int position, int code, int card, int card2, String desc, int direct) {
		Action a = new Action(seq, position, code, card, card2, desc, direct);
		actionList.add(a);
		return a;
	}
	
	public Action getReplyAct(int seq, int position) {
		for(Action a : actionList) {
//			System.out.println("seq1="+seq+";position1="+position+";seq2="+a.seq+";position2="+a.position);;
			if(a.seq == seq && a.position == position && a.direct == 0) {
				return a;
			}
		}
		return null;
	}
}