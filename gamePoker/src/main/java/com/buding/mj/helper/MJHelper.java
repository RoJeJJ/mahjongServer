package com.buding.mj.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.buding.game.GameData;
import packet.mj.MJ.GameOperChiArg;
import packet.mj.MJ.GameOperPlayerActionNotify;

import com.buding.mj.common.CardCombo;
import com.buding.mj.constants.MJConstants;
import com.buding.mj.model.Card;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class MJHelper {
	public static Map<Integer, String> singleCardMap = new HashMap<Integer, String>();

	static {
		singleCardMap.put(MJConstants.MAHJONG_CODE_DONG_FENG + 0, "東风");
		singleCardMap.put(MJConstants.MAHJONG_CODE_NAN_FENG + 0, "南风");
		singleCardMap.put(MJConstants.MAHJONG_CODE_XI_FENG + 0, "西风");
		singleCardMap.put(MJConstants.MAHJONG_CODE_BEI_FENG + 0, "北风");
		singleCardMap.put(MJConstants.MAHJONG_CODE_HONG_ZHONG + 0, "红中");
		singleCardMap.put(MJConstants.MAHJONG_CODE_FA_CAI + 0, "發财");
		singleCardMap.put(MJConstants.MAHJONG_CODE_BAI_BAN + 0, "白板");

		for (int i = 1; i <= 9; i++) {
			int ib = (0 << MJConstants.MAHJONG_CODE_COLOR_SHIFTS) + i;
			byte b = (byte) (ib & 0xff);
			singleCardMap.put(b + 0, i + "万");
		}
		for (int i = 1; i <= 9; i++) {
			int ib = (1 << MJConstants.MAHJONG_CODE_COLOR_SHIFTS) + i;
			byte b = (byte) (ib & 0xff);
			singleCardMap.put(b + 0, i + "条");
		}
		for (int i = 1; i <= 9; i++) {
			int ib = (2 << MJConstants.MAHJONG_CODE_COLOR_SHIFTS) + i;
			byte b = (byte) (ib & 0xff);
			singleCardMap.put(b + 0, i + "筒");
		}
		System.out.println(new Gson().toJson(singleCardMap));
	}

	public static Set<Byte> getAllUniqCard() {
		Set<Byte> set = new HashSet<Byte>();
		for (int card : singleCardMap.keySet()) {
			byte c = (byte) (card & 0xFF);
			set.add(c);
		}
		return set;
	}

	public static List<Byte> getCardCodeList(List<Card> cards) {
		List<Byte> list = new ArrayList<Byte>();
		for (Card card : cards) {
			list.add((byte) (card.code & 0XFF));
		}
		return list;
	}

	public static String getSingleCardCodeNames(List<Byte> cards) {
		List<String> list = new ArrayList<String>();
		for (int card : cards) {
			list.add(new Card(card, singleCardMap.get(card)).toString());
		}
		return new GsonBuilder().create().toJson(list);
	}

	public static String getCompositeCardCodeNames(List<Integer> cards) {
		List<String> list = new ArrayList<String>();
		for (int card : cards) {
			int card1 = card & 0xFF;
			int card2 = (card >> 8) & 0xFF;
			int card3 = (card >> 16) & 0xFF;
			if (card1 > 0) {
				list.add(new Card(card1, singleCardMap.get(card1)).toString());
			}
			if (card2 > 0) {
				list.add(new Card(card2, singleCardMap.get(card2)).toString());
			}
			if (card3 > 0) {
				list.add(new Card(card3, singleCardMap.get(card3)).toString());
			}
		}
		return new GsonBuilder().create().toJson(list);
	}

	public static String getSingleCardListName(List<Byte> cards) {
		List<String> ret = new ArrayList<String>();
		for (int card : cards) {
			ret.add(getSingleCardName(card));
		}
		return new Gson().toJson(ret);
	}

	public static String getCompositeCardListName(List<Integer> cards) {
		List<String> ret = new ArrayList<String>();
		for (int card : cards) {
			int card1 = card & 0xFF;
			int card2 = (card >> 8) & 0xFF;
			int card3 = (card >> 16) & 0xFF;
			if (card1 > 0) {
				ret.add(getSingleCardName(card1));
			}
			if (card2 > 0) {
				ret.add(getSingleCardName(card2));
			}
			if (card3 > 0) {
				ret.add(getSingleCardName(card3));
			}
		}
		return new Gson().toJson(ret);
	}

	public static String getSingleCardName(int card) {
		if (card == -1) {
			return "背面";
		}
		if (card == 0) {
			return "";
		}
		return singleCardMap.get(card);
	}

	private static int convertAct(List<String> ret, int multiAct, int singleAct, String actName) {
		if ((multiAct & singleAct) == singleAct) {
			ret.add(actName);
			return multiAct - singleAct;
		}
		return multiAct;
	}

	public static Set<Byte> getUniqCardList(List<Byte> list) {
		Set<Byte> set = new HashSet<Byte>();
		set.addAll(list);
		return set;
	}

	public static String getActionName(int act) {
		List<String> ret = new ArrayList<String>();
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_CHI, "吃");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_PENG, "碰");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_AN_GANG, "暗杠");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_MING_GANG, "明杠");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_GANG_PENG, "杠碰");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_GANG_PENG_CHI, "杠碰吃");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_CHU, "出");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_HU, "胡");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_TING, "听");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_CANCEL, "取消");
		// act = convertAct(ret, act,
		// MJConstants.MAHJONG_OPERTAION_OVERTIME_AUTO_CHU, "自动出");
		// act = convertAct(ret, act,
		// MJConstants.MAHJONG_OPERTAION_ADD_CHU_CARD, "已出");
		// act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_QIANG_TING,
		// "抢听");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_MO, "摸牌");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_CHI_TING, "吃听");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_PENG_TING, "碰听");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_ZD_TING, "支对");
		act = convertAct(ret, act, MJConstants.MAHJONG_OPERTAION_CHNAGE_BAO, "换宝");

		if (act > 0) {
			throw new RuntimeException("0x" + Integer.toHexString(act) + "未知牌操作");
		}
		return new Gson().toJson(ret);
	}

	public static void copyChiArg(List<CardCombo> combos, GameOperPlayerActionNotify.Builder msg) {
		if (combos == null) {
			return;
		}
		for (CardCombo c : combos) {
			GameOperChiArg.Builder gb = GameOperChiArg.newBuilder();
			gb.setMyCard1(c.card1);
			gb.setMyCard2(c.card2);
			gb.setTargetCard(c.targetCard);
			msg.addChiArg(gb);
		}
	}

	// public static void copyHuArg(byte newCard,
	// GameOperPlayerActionNotify.Builder msg) {
	// if (newCard <= 0) {
	// return;
	// }
	// // GameOperHuArg.Builder ab = GameOperHuArg.newBuilder();
	// // ab.setTargetCard(newCard);
	// // msg.setHuArg(ab);
	// msg.setHuArg(newCard);
	// }
	//
	// public static void copyAutoChuArg(byte newCard,
	// GameOperPlayerActionNotify.Builder msg) {
	// if (newCard <= 0) {
	// return;
	// }
	// GameOperAutoChuArg.Builder ab = GameOperAutoChuArg.newBuilder();
	// ab.setCard(newCard);
	// msg.setAutoChuArg(ab);
	// }

	public static void copyPengArg(int newCard, GameOperPlayerActionNotify.Builder msg) {
		if (newCard <= 0) {
			return;
		}
		msg.setPengArg(newCard);
	}

	public static void add2SortedList(int b, List<Integer> cards) {
		boolean inserted = false;
		//
		for (int i = 0; i < cards.size(); i++) {
			int bb = cards.get(i);
			if (b < bb) {
				cards.add(i, b);
				inserted = true;
				break;
			}
		}
		//
		if (inserted == false) {
			cards.add(b);
		}
	}

	public static boolean findInSortList(List<Byte> list, byte card1, byte card2) {
		int ind = list.indexOf((Object) card1);
		int size = list.size();
		if (ind == -1) {
			return false;
		}
		boolean found1 = false;
		boolean found2 = false;
		for (; ind < size; ind++) {
			if (list.get(ind) == card1) {
				found1 = true;
				break;
			}
			if (list.get(ind) > card1) {
				return false;
			}
		}

		for (; ind < size; ind++) {
			if (list.get(ind) == card2) {
				found2 = true;
				break;
			}
			if (list.get(ind) > card2) {
				return false;
			}
		}
		return found1 && found2;
	}

	public static void add2SortedList(byte b, List<Byte> cards) {
		boolean inserted = false;
		//
		for (int i = 0; i < cards.size(); i++) {
			byte bb = cards.get(i);
			if (b < bb) {
				cards.add(i, b);
				inserted = true;
				break;
			}
		}
		//
		if (inserted == false) {
			cards.add(b);
		}
	}

	public static String getResultTypeDesc(int fanType) {
		if ((fanType & MJConstants.MAHJONG_HU_CODE_DAILOU) == MJConstants.MAHJONG_HU_CODE_DAILOU) {
			return "宝中宝";
		} else if ((fanType & MJConstants.MAHJONG_HU_CODE_GUADAFENG) == MJConstants.MAHJONG_HU_CODE_GUADAFENG) {
			return "刮大风";
		} else if ((fanType & MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) == MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) {
			return "开牌炸";
		} else if ((fanType & MJConstants.MAHJONG_HU_CODE_HONGZHONGMTF) == MJConstants.MAHJONG_HU_CODE_HONGZHONGMTF) {
			return "红中满天飞";
		} else if ((fanType & MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) == MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) {
			return "宝中宝";
		} else if ((fanType & MJConstants.MAHJONG_HU_CODE_MO_BAO) == MJConstants.MAHJONG_HU_CODE_MO_BAO) {
			return "摸宝";
		} else if ((fanType & MJConstants.MAHJONG_HU_CODE_WIN) == MJConstants.MAHJONG_HU_CODE_WIN) {
			return "平胡";
		} else if ((fanType & MJConstants.MAHJONG_HU_CODE_LOSE) == MJConstants.MAHJONG_HU_CODE_LOSE) {
			return "输";
		} else if ((fanType & MJConstants.MAHJONG_HU_CODE_LIUJU) == MJConstants.MAHJONG_HU_CODE_LIUJU) {
			return "流局";
		}
		return "无";
	}

	public static int getHuType(int fanType) {
		// 开牌炸
		if ((fanType & MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) == MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) {
			return MJConstants.MAHJONG_HU_CODE_KAIPAIZHA;
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_DAILOU) == MJConstants.MAHJONG_HU_CODE_DAILOU) {
			return MJConstants.MAHJONG_HU_CODE_DAILOU;
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) == MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) {
			return MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO;
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_GUADAFENG) == MJConstants.MAHJONG_HU_CODE_GUADAFENG) {
			return MJConstants.MAHJONG_HU_CODE_GUADAFENG;
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_HONGZHONGMTF) == MJConstants.MAHJONG_HU_CODE_HONGZHONGMTF) {
			return MJConstants.MAHJONG_HU_CODE_HONGZHONGMTF;
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_MO_BAO) == MJConstants.MAHJONG_HU_CODE_MO_BAO) {
			return MJConstants.MAHJONG_HU_CODE_MO_BAO;
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_ZI_MO) == MJConstants.MAHJONG_HU_CODE_ZI_MO) {
			return MJConstants.MAHJONG_HU_CODE_ZI_MO;
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_WIN) == MJConstants.MAHJONG_HU_CODE_WIN) {
			return MJConstants.MAHJONG_HU_CODE_WIN;
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_LIUJU) == MJConstants.MAHJONG_HU_CODE_LIUJU) {
			return MJConstants.MAHJONG_HU_CODE_LIUJU;
		}
		return MJConstants.MAHJONG_HU_CODE_WIN;
	}

	public static int getResultType(int fanType) {
		// if ((fanType & MJConstants.MAHJONG_HU_CODE_DAILOU) ==
		// MJConstants.MAHJONG_HU_CODE_DAILOU) {
		// return MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO;
		// } else if ((fanType & MJConstants.MAHJONG_HU_CODE_GUADAFENG) ==
		// MJConstants.MAHJONG_HU_CODE_GUADAFENG) {
		// return MJConstants.MAHJONG_HU_CODE_GUADAFENG;
		// } else if ((fanType & MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) ==
		// MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) {
		// return MJConstants.MAHJONG_HU_CODE_KAIPAIZHA;
		// } else if ((fanType & MJConstants.MAHJONG_HU_CODE_HONGZHONGMTF) ==
		// MJConstants.MAHJONG_HU_CODE_HONGZHONGMTF) {
		// return MJConstants.MAHJONG_HU_CODE_HONGZHONGMTF;
		// } else if ((fanType & MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) ==
		// MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) {
		// return MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO;
		// } else if ((fanType & MJConstants.MAHJONG_HU_CODE_MO_BAO) ==
		// MJConstants.MAHJONG_HU_CODE_MO_BAO) {
		// return MJConstants.MAHJONG_HU_CODE_MO_BAO;
		// } else

		if ((fanType & MJConstants.MAHJONG_HU_CODE_WIN) == MJConstants.MAHJONG_HU_CODE_WIN) {
			return MJConstants.MAHJONG_HU_CODE_WIN;
		} else if ((fanType & MJConstants.MAHJONG_HU_CODE_LOSE) == MJConstants.MAHJONG_HU_CODE_LOSE) {
			return MJConstants.MAHJONG_HU_CODE_LOSE;
		} else if ((fanType & MJConstants.MAHJONG_HU_CODE_LIUJU) == MJConstants.MAHJONG_HU_CODE_LIUJU) {
			return MJConstants.MAHJONG_HU_CODE_LIUJU;
		}
		return 0;
	}

	public static String getFanDescList2String(int fanType) {
		List<String> descList = getFanDescList(fanType);
		return new Gson().toJson(descList);
	}

	public static List<String> getFanDescList(int fanType) {
		List<String> list = new ArrayList<String>();

		int i = 0;
		if ((fanType & MJConstants.MAHJONG_HU_CODE_WIN) == MJConstants.MAHJONG_HU_CODE_WIN) {
			if ((fanType & MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) == MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) {
				i++;
				fanType -= MJConstants.MJ_HU_TYPE_BAO_ZHONG_BAO;
				list.add(0, "开牌炸 ");
			}

			if ((fanType & MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) == MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) {
				i++;
				fanType -= MJConstants.MJ_HU_TYPE_BAO_ZHONG_BAO;
				list.add(0, "宝中宝");
			}

			if ((fanType & MJConstants.MAHJONG_HU_CODE_GUADAFENG) == MJConstants.MAHJONG_HU_CODE_GUADAFENG) {
				i++;
				fanType -= MJConstants.MJ_HU_TYPE_MO_BAO_HU;
				list.add(0, "刮大风 ");
			}

			if ((fanType & MJConstants.MAHJONG_HU_CODE_MO_BAO) == MJConstants.MAHJONG_HU_CODE_MO_BAO) {
				fanType -= MJConstants.MAHJONG_HU_CODE_MO_BAO;
				i++;
				list.add(0, "摸宝 ");
			}

			if ((fanType & MJConstants.MAHJONG_HU_CODE_JIA_HU) == MJConstants.MAHJONG_HU_CODE_JIA_HU) {
				fanType -= MJConstants.MAHJONG_HU_CODE_JIA_HU;
				i++;
				list.add(0, "夹胡 ");
			}

			if ((fanType & MJConstants.MAHJONG_HU_CODE_ZI_MO) == MJConstants.MAHJONG_HU_CODE_ZI_MO) {
				fanType -= MJConstants.MAHJONG_HU_CODE_ZI_MO;
				i++;
				list.add(0, "自摸 ");
			}

			if (i == 0) {
				list.add("平胡 ");
			}
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_TING) != MJConstants.MAHJONG_HU_CODE_TING) {
			list.add("未上听 ");
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_MEN_QING) == MJConstants.MAHJONG_HU_CODE_MEN_QING) {
			fanType -= MJConstants.MAHJONG_HU_CODE_MEN_QING;
			list.add("门清 ");
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_DIAN_PAO) == MJConstants.MAHJONG_HU_CODE_DIAN_PAO) {
			fanType -= MJConstants.MAHJONG_HU_CODE_DIAN_PAO;
			list.add("点炮 ");
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA) == MJConstants.MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA) {
			fanType -= MJConstants.MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA;
			list.add("庄家 ");
		}

		return list;
	}

	public static List<String> getFanDescList(int fanType,int playerId,GameData gameData) {
		List<String> list = new ArrayList<String>();

		int i = 0;
		if ((fanType & MJConstants.MAHJONG_HU_CODE_WIN) == MJConstants.MAHJONG_HU_CODE_WIN) {
			if ((fanType & MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) == MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) {
				i++;
				fanType -= MJConstants.MJ_HU_TYPE_BAO_ZHONG_BAO;
				list.add(0, "开牌炸 ");
			}

			if ((fanType & MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) == MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) {
				i++;
				fanType -= MJConstants.MJ_HU_TYPE_BAO_ZHONG_BAO;
				list.add(0, "宝中宝");
			}

			if ((fanType & MJConstants.MAHJONG_HU_CODE_GUADAFENG) == MJConstants.MAHJONG_HU_CODE_GUADAFENG) {
				i++;
				fanType -= MJConstants.MJ_HU_TYPE_MO_BAO_HU;
				list.add(0, "刮大风 ");
			}

			if ((fanType & MJConstants.MAHJONG_HU_CODE_MO_BAO) == MJConstants.MAHJONG_HU_CODE_MO_BAO) {
				fanType -= MJConstants.MAHJONG_HU_CODE_MO_BAO;
				i++;
				list.add(0, "摸宝 ");
			}

			if ((fanType & MJConstants.MAHJONG_HU_CODE_JIA_HU) == MJConstants.MAHJONG_HU_CODE_JIA_HU) {
				fanType -= MJConstants.MAHJONG_HU_CODE_JIA_HU;
				i++;
				list.add(0, "夹胡 ");
			}

			if ((fanType & MJConstants.MAHJONG_HU_CODE_ZI_MO) == MJConstants.MAHJONG_HU_CODE_ZI_MO) {
				fanType -= MJConstants.MAHJONG_HU_CODE_ZI_MO;
				i++;
				list.add(0, "自摸 ");
			}

			if (i == 0) {
				list.add("平胡 ");
			}
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_TING) != MJConstants.MAHJONG_HU_CODE_TING) {
			list.add("未上听 ");
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_MEN_QING) == MJConstants.MAHJONG_HU_CODE_MEN_QING) {
			fanType -= MJConstants.MAHJONG_HU_CODE_MEN_QING;
			list.add("门清 ");
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_DIAN_PAO) == MJConstants.MAHJONG_HU_CODE_DIAN_PAO) {
			fanType -= MJConstants.MAHJONG_HU_CODE_DIAN_PAO;
			list.add("点炮 ");
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA) == MJConstants.MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA) {
			fanType -= MJConstants.MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA;
			list.add("庄家 ");
		}

		if (gameData.gangCards.mGangCount.get(playerId) != null){
			list.add(0, "明杠*" + gameData.gangCards.mGangCount.get(playerId) + " ");
		}
		if (gameData.gangCards.aGangCount.get(playerId) != null){
			list.add(0, "暗杠*" + gameData.gangCards.aGangCount.get(playerId) + " ");
		}

		String piaoStr = "不飘 ";
		if (gameData.playerGameInfoMap.get(String.valueOf(playerId)).getPiaoNum() > 0){
			piaoStr = "飘+" + gameData.playerGameInfoMap.get(String.valueOf(playerId)).getPiaoNum() + " ";
		}
		list.add(0, piaoStr);

		String buyStr = "不买 ";
		if (gameData.playerGameInfoMap.get(String.valueOf(playerId)).isBuy()){
			buyStr = "买 ";
		}
		list.add(0, buyStr);

		return list;
	}

	public static String getFanDesc(int fanType) {
		String fanDesc = "";
		if ((fanType & MJConstants.MAHJONG_HU_CODE_WIN) == MJConstants.MAHJONG_HU_CODE_WIN) {
			fanDesc += "赢 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_LOSE) == MJConstants.MAHJONG_HU_CODE_LOSE) {
			fanDesc += "输 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_TING) == MJConstants.MAHJONG_HU_CODE_TING) {
			fanDesc += "已上听 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_TING) != MJConstants.MAHJONG_HU_CODE_TING) {
			fanDesc += "未上听 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_MEN_QING) == MJConstants.MAHJONG_HU_CODE_MEN_QING) {
			fanDesc += "门清 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_DIAN_PAO) == MJConstants.MAHJONG_HU_CODE_DIAN_PAO) {
			fanDesc += "点炮 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA) == MJConstants.MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA) {
			fanDesc += "我是庄家 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_TARGET_ZHUANG_JIA) == MJConstants.MAHJONG_HU_CODE_TARGET_ZHUANG_JIA) {
			fanDesc += "对手庄家 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_ZI_MO) == MJConstants.MAHJONG_HU_CODE_ZI_MO) {
			fanDesc += "自摸一番 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_JIA_HU) == MJConstants.MAHJONG_HU_CODE_JIA_HU) {
			fanDesc += "夹胡一番 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_MO_BAO) == MJConstants.MAHJONG_HU_CODE_MO_BAO) {
			fanDesc += "宝胡一番 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) == MJConstants.MAHJONG_HU_CODE_BAO_ZHONG_BAO) {
			fanDesc += "宝中宝一番 ";
		}

		if ((fanType & MJConstants.MAHJONG_HU_CODE_DAILOU) == MJConstants.MAHJONG_HU_CODE_DAILOU) {
			fanDesc += "带漏 ";
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_GUADAFENG) == MJConstants.MAHJONG_HU_CODE_GUADAFENG) {
			fanDesc += "刮大风 ";
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) == MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) {
			fanDesc += "开牌炸 ";
		}
		if ((fanType & MJConstants.MAHJONG_HU_CODE_HONGZHONGMTF) == MJConstants.MAHJONG_HU_CODE_HONGZHONGMTF) {
			fanDesc += "红中满天飞 ";
		}

		return fanDesc;
	}

	public static String getChiComboStr(List<GameOperChiArg> list) {
		if (list == null) {
			return "";
		}
		List<String> ret = new ArrayList<String>();
		for (GameOperChiArg arg : list) {
			int card = arg.getMyCard1() & 0XFF;
			int card2 = arg.getMyCard2() & 0XFF;
			int target = arg.getTargetCard() & 0XFF;
			ret.add(new Card(card, MJHelper.getSingleCardName(card)).toString());
			ret.add(new Card(card2, MJHelper.getSingleCardName(card2)).toString());
			ret.add(new Card(target, MJHelper.getSingleCardName(target)).toString());
		}
		return new Gson().toJson(ret);
	}

	public static List<CardCombo> getAllShunzi(List<Byte> cards) {
		List<CardCombo> list = new ArrayList<CardCombo>();
		for (int i = 0; i < cards.size() - 2; i++) {
			Byte b1 = cards.get(i);
			Byte b2 = (byte)(b1+1);
			Byte b3 = (byte)(b1+2);
			int ind2 = findIndex(cards, b2, i+1);
			if(ind2 == -1) {
				continue;
			}			
			int ind3 = findIndex(cards, b3, ind2+1);
			if(ind3 == -1) {
				continue;
			}
			list.add(new CardCombo(b1, b2, b3));
		}

		return list;
	}
	
	public static int findIndex(List<Byte> cards, byte card2Find, int fromInd) {
		if(fromInd >= cards.size()) {
			return -1;
		}
		int i = fromInd;
		while(i < cards.size()) {
			if(cards.get(i) == card2Find) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public static List<CardCombo> getAllKezi(List<Byte> cards) {
		List<CardCombo> list = new ArrayList<CardCombo>();
		for (int i = 0; i < cards.size() - 2; i++) {
			Byte b1 = cards.get(i);
			Byte b2 = cards.get(i + 1);
			Byte b3 = cards.get(i + 2);
			if (b1 == b2 && b2 == b3) {
				list.add(new CardCombo(b1, b2, b3));
			}
		}

		return list;
	}

	public static int getCardCount(List<Byte> list, byte card) {
		int i = 0;
		for (byte c : list) {
			if (c == card) {
				i++;
			}
		}
		return i;
	}

	public static List<Byte> getAllDouble(List<Byte> list) {
		Map<Byte, Integer> map = new HashMap<Byte, Integer>();
		for (byte b : list) {
			if (map.containsKey(b)) {
				map.put(b, map.get(b) + 1);
			} else {
				map.put(b, 1);
			}
		}
		List<Byte> ret = new ArrayList<Byte>();
		for (Entry<Byte, Integer> e : map.entrySet()) {
			if (e.getValue() > 1) {
				ret.add(e.getKey());
			}
		}
		Collections.sort(ret);
		return ret;
	}

	public static int addCardDown(int c1, int c2, int c3, boolean isChi, List<Integer> list) {
		int card = 0;
		if (c3 >= c2 && c2 >= c1)// 321
		{
			card = (c3 << 16) | (c2 << 8) | c1;
		}
		if (c3 >= c1 && c1 >= c2)// 312
		{
			card = (c3 << 16) | (c1 << 8) | c2;
		} else if (c1 >= c2 && c2 >= c3)// 123
		{
			card = (c1 << 16) | (c2 << 8) | c3;
		} else if (c1 >= c3 && c3 >= c2)// 132
		{
			card = (c1 << 16) | (c3 << 8) | c2;
		} else if (c2 >= c3 && c3 >= c1)// 231
		{
			card = (c2 << 16) | (c3 << 8) | c1;
		} else if (c2 >= c1 && c1 >= c3)// 213
		{
			card = (c2 << 16) | (c1 << 8) | c3;
		}

		if (isChi) {
			card = card | (c1 << 24);
		}

		list.add(card);
		return card;
	}

	public static int addGangCardDown(int card,  List<Integer> list) {
		list.add(card);
		return card;
	}

	public static boolean isHas3SameV2(byte b, List<Byte> comboList) {
		int sames = 0;
		for (byte tempCard : comboList) {
			if (tempCard == b) {
				sames++;
			}
		}
		if (sames == 3) {
			return true;
		}
		return false;
	}

	public static boolean isHas3Same(byte b, List<Integer> comboList) {
		for (int i = 0; i < comboList.size(); i++) {
			int bb = comboList.get(i);
			byte b1 = (byte) (bb & 0xff);
			byte b2 = (byte) ((bb >> 8) & 0xff);
			byte b3 = (byte) ((bb >> 16) & 0xff);

			if (b1 == b && b2 == b && b3 == b) {
				return true;
			}
		}
		return false;
	}
}
