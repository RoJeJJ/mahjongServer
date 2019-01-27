package com.buding.game;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class GamePacket {

	static public class MyGame_Status {

		public int position = -1;
		public int status = -1;
	}

	static public class MyGame_Public {

		public int gameRule = -1;
		public int times = -1; // 倍数，叫地主或抢地主、炸弹等会翻一定倍数
		public int basePoint = -1;
		public int cardLeft = -1;

	}

	static public class MyGame_Actor {
		// 当前游戏状态
		public int gameState = -1;
		public int sequence = -1;
		public int lastActor = -1; // 最近一次操作玩家
		public int lastOutCardType = -1;
		public int currentActor = -1; // 当前出牌玩家
		public int timerInterval = -1; // 操作超时时间
	}

	static public class MyGame_PublicInfo {
		// public int cardLeft = GameConstants.MyGame_Invalid_Value;; //剩余牌数
		public byte mBaoCard = GameConstants.MyGame_Invalid_Value;; // 宝牌,
																	// 如果不能查看，则返回-1
		public int mbankerPos = GameConstants.MyGame_Invalid_Value; // 庄家位置
		public int mBankerUserId = GameConstants.MyGame_Invalid_Value;// 庄家id
		public boolean isContinueBanker = false; // 是否连庄
	}

	static public class MyGame_DeskCard {
		// 未摸的牌
		public List<Byte> cards = new ArrayList<Byte>();

		// 打下来的牌
		public List<Integer> down_cards = new ArrayList<Integer>();

		public void reset() {
			cards.clear();
			down_cards.clear();
		}
	}

	static public class MyGame_Player_Cards {
		public int position = -1;
		public List<Byte> cardsInHand = new ArrayList<Byte>();// 手牌
		public List<Byte> cardsBefore = new ArrayList<Byte>();// 打出的牌
		public transient List<Integer> cardsDown = new ArrayList<Integer>();// 吃碰明杠的牌
		public transient List<Integer> aGangCards = new ArrayList<Integer>();// 暗杠的牌
	}

	static public class MyGame_Player_Gang_Cards {
		public Map<Integer,Integer> mGangCount = Maps.newHashMap(); //明杠次数
		public Map<Integer,Integer> aGangCount = Maps.newHashMap(); //暗杠次数
	}

	static public class MyGame_Player_Ting_Cards {
		public boolean tingCard = false; // 是否已经上听
		public boolean showBao = false; // 是否已经暴露了宝牌
		public Set<Byte> cards = new HashSet<Byte>();// 听的牌
		public byte zhiduiTing = 0; // 选择支对听的牌, 如果不是支对听则为0
		//如果一手牌可以上听时，程序检测需要打出什么牌，然后听什么牌，将检测结果存入到这里
		public Map<Byte, Set<Byte>> chuAndTingMap = new HashMap<Byte, Set<Byte>>();
	}
	
	static public class MyGame_Player_Action {
		public byte cardGrab = 0;
		public int autoOperation = 0;
		public long opStartTime = 0;
		
		public void reset() {
			this.cardGrab = 0;
			this.opStartTime = 0;
		}
	}

	static public class MyGame_Player_Cancel {
		public int cancelOp = 0; // 取消的操作
	}

	static public class MyGame_Player_Hu {
		// 胡牌玩家位置
		public int position = -1;
		public byte huCard = -1;

		public void reset() {
			position = -1;
			huCard = -1;
		}
	}

	static public class MyGame_Out_Cards {

		public int position = -1;
		public List<Integer> cards = new LinkedList<Integer>();
		public int type = -1;

	}

	static public class MyGame_Finish {

		public int position = -1;
		public int result = -1;
		public int getPoint = -1;
	}
}
