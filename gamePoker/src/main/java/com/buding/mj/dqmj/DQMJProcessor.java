package com.buding.mj.dqmj;

import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.omg.CORBA.TRANSACTION_MODE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.mj.common.BaseMJRule;
import com.buding.mj.common.CardCombo;
import com.buding.mj.common.MJContext;
import com.buding.mj.common.MjCheckResult;
import com.buding.mj.constants.MJConstants;
import com.buding.mj.helper.MJHelper;
import com.buding.mj.model.ActionWaitingModel;
import com.buding.mj.model.ChuTingModel;
import com.buding.mj.model.MjCheckContext;
import com.buding.mj.model.ZhiduiModel;
import com.google.gson.Gson;

public class DQMJProcessor {
    private Logger logger = LoggerFactory.getLogger(getClass());
    BaseMJRule mjRule = new BaseMJRule();

    public DQMJProcessor() {

    }

    // 杠
    public ActionWaitingModel check_gang(GameData gameData, byte card, PlayerInfo pl) {
        int cd = card & 0xff;
        ActionWaitingModel result = null;
        //
        if (gameData.getCardNumInHand(pl.position) <= 4)
            return null;
        //
        int same_card_num = gameData.getXCardNumInHand(card, pl.position);

        if (same_card_num == 3) {
            result = new ActionWaitingModel();
            result.targetCard = card;
            result.peng_card_value = cd | (cd << 8);
            result.playerTableIndex = pl.position;
            result.opertaion = MJConstants.MAHJONG_OPERTAION_MING_GANG;
        }
        return result;
    }

    // 碰
    public ActionWaitingModel check_peng(GameData gameData, byte card, PlayerInfo pl) {
        int cd = card & 0xff;
        ActionWaitingModel result = null;
        //
        if (gameData.getCardNumInHand(pl.position) <= 4)
            return null;
        //
        int same_card_num = gameData.getXCardNumInHand(card, pl.position);

        if (same_card_num >= 2) {
            result = new ActionWaitingModel();
            result.targetCard = card;
            result.peng_card_value = cd | (cd << 8);
            result.playerTableIndex = pl.position;
            result.opertaion = MJConstants.MAHJONG_OPERTAION_PENG;
        }
        return result;
    }

    public boolean checkHuBaseRule(List<Byte> handcards, List<Integer> cardsDown, MjCheckContext ctx) {
        //TODO 清一色可以胡
        // 清一色不能胡
        if (has2Color(handcards, cardsDown) == false) {
            if (ctx.debug) {
                logger.info("act=checkHuBaseRule;desc=清一色不能胡;");
            }
            return false;
        }
        //
        // 红中的牌数
        int hongzhong_num = getHongZhongNum(handcards, cardsDown);

        // 如果没有红中
        if (hongzhong_num == 0) { // 、红中就可代替这个“幺九牌条件”与“刻牌条件”。(即只要有一张红中，即可忽略以上第2、3条和牌和条件。例：单调红中)
            // 是否有1或9
            if (has19(handcards, cardsDown) == false) {
                if (ctx.debug) {
                    logger.info("act=checkHuBaseRule;desc=幺九条件不能胡;");
                }
                return false;
            }
            // 胡的时候一定有刻子
            if (hasKe(handcards, cardsDown) == false) {
                if (ctx.debug) {
                    logger.info("act=checkHuBaseRule;desc=无刻子不能胡;");
                }
                return false;
            }
        }
        // 没有顺子, 有红中也要有顺子才能胡
        if (hasShun(handcards, cardsDown) == false) {
            if (ctx.debug) {
                logger.info("act=checkHuBaseRule;desc=没有顺子不能胡;");
            }
            return false;
        }
        // 门清不能胡
        if (!ctx.isQiangTing && cardsDown.size() == 0) {
            if (ctx.debug) {
                logger.info("act=checkHuBaseRule;desc=门清不能胡;");
            }
            return false;
        }
        return true;
    }

    /**
     * 获取可以支队的牌
     *
     * @param handCards
     * @param chuTingModel
     * @return 1.遍历可以听的集合(key为可以出的牌--card2Remove, value为可以胡的牌的集合--tingList)
     * 2.如果tingList少于两个,return
     * 3,手牌去除出的牌,找到可以胡的牌的位置,位置大于2满足条件
     */
    public ZhiduiModel getZhiduiCards(List<Byte> handCards, ChuTingModel chuTingModel) {
        ZhiduiModel model = new ZhiduiModel();
        for (byte card2Remove : chuTingModel.chuAndTingMap.keySet()) { //打出一张牌
            Set<Byte> tingList = chuTingModel.chuAndTingMap.get(card2Remove);//需要听的牌
            if (tingList.size() < 2) {
                continue;
            }
            List<Byte> cards = new ArrayList<Byte>();
            cards.addAll(handCards);
            cards.remove((Object) card2Remove);
            Set<Integer> tmp = new HashSet<Integer>();
            for (byte card2Ting : tingList) {
                int count = MJHelper.getCardCount(cards, card2Ting);
                if (count >= 2) {
                    tmp.add((int) card2Ting);
                }
            }
            if (tmp.size() >= 2) {
                model.chuAndZhiduiMap.put(card2Remove, tmp);
            }

        }
        return model;
    }

    public MjCheckResult canTingThisCard(List<Byte> handCards, List<Integer> cardsDown, byte card2Remove, byte card2Ting, MjCheckContext ctx) {
        List<Byte> list = new ArrayList<Byte>();
        list.addAll(handCards);
        list.remove((Object) card2Remove);// 打出这一张牌

        //打出一张牌后，依然要满足基本上听条件
        if (checkTingBaseRule(list, cardsDown, ctx) == false) {
            if (ctx.debug) {
                logger.info("checkHu:ting={};remove={};result={};", MJHelper.getSingleCardName(card2Ting), MJHelper.getSingleCardName(card2Remove), "基本检测不通过");
            }
            return null;
        }

        MJHelper.add2SortedList(card2Ting, list);// 加一张牌

        if (ctx.debug) {
            logger.info("checkHu:ting={};remove={};hand={};down={};", MJHelper.getSingleCardName(card2Ting), MJHelper.getSingleCardName(card2Remove), MJHelper.getSingleCardListName(list), MJHelper.getCompositeCardListName(cardsDown));
        }


        if (checkHuBaseRule(list, cardsDown, ctx) == false) {
            if (ctx.debug) {
                logger.info("checkHu:ting={};remove={};result={};", MJHelper.getSingleCardName(card2Ting), MJHelper.getSingleCardName(card2Remove), "基本检测不通过");
            }
            return null;
        }

        MJContext c = new MJContext();
        c.cardsInCard.addAll(list);
        MjCheckResult ret = mjRule.canHu(c);
        MjCheckContext m = new MjCheckContext();
        m.isQiangTing = true;
        if (checkHuBaseRule(list, cardsDown, m)) {
            return ret;
        }


//		int hongzhong_num = getHongZhongNum(list, cardsDown);
//		boolean hasKezi = hasKe(new ArrayList<Byte>(), cardsDown) || hongzhong_num > 0; //2 个或3个红中都可以
//		boolean hasShunzi = hasShun(new ArrayList<Byte>(), cardsDown);
//		if(hasKezi && hasShunzi) { //刻子条件和顺子条件都满足，只需要检查是否成牌即可
//			MJContext c = new MJContext();
//			c.cardsInCard.addAll(list);
//			MjCheckResult ret = mjRule.canHu(c);
//			return ret;
//		}
//
//		if(hasKezi && !hasShunzi) {
//			//将所有顺子抽出来，看剩下的是否能胡牌
//
//			List<CardCombo> shunziList = MJHelper.getAllShunzi(list);
//			for(CardCombo c : shunziList) {
//				List<Byte> cards = new ArrayList<Byte>(list);
//				//先把顺子提出来，这是基本胡牌条件,剩下的牌再看能不能胡
//				cards.remove((Byte)c.card1);
//				cards.remove((Byte)c.card2);
//				cards.remove((Byte)c.targetCard);
//				MJContext context = new MJContext();
//				context.cardsInCard.addAll(cards);
//				MjCheckResult ret = mjRule.canHu(context);
//				if(ret != null) {
//					return ret;
//				}
//			}
//			return null;
//		}
//
//		if(hasShunzi && !hasKezi) { //有顺子没有刻子
//			List<CardCombo> keziList = MJHelper.getAllKezi(list);
//			for(CardCombo c : keziList) {
//				List<Byte> cards = new ArrayList<Byte>(list);
//				//先把刻子提出来，这是基本胡牌条件,剩下的牌再看能不能胡
//				cards.remove((Byte)c.card1);
//				cards.remove((Byte)c.card2);
//				cards.remove((Byte)c.targetCard);
//				MJContext context = new MJContext();
//				context.cardsInCard.addAll(cards);
//				MjCheckResult ret = mjRule.canHu(context);
//				if(ret != null) {
//					return ret;
//				}
//			}
//			return null;
//		}
//
//		//吃碰杠中没有刻子和顺子
//		if(!hasKezi && !hasShunzi) {
//			List<CardCombo> shunziList = MJHelper.getAllShunzi(list);
//			for(CardCombo c : shunziList) {
//				List<Byte> cards = new ArrayList<Byte>(list);
//				//先把顺子提出来，这是基本胡牌条件,剩下的牌再看能不能胡
//				cards.remove((Byte)c.card1);
//				cards.remove((Byte)c.card2);
//				cards.remove((Byte)c.targetCard);
//
//				List<CardCombo> keziList = MJHelper.getAllKezi(cards);
//				for(CardCombo kezi : keziList) {
//					List<Byte> cards2 = new ArrayList<Byte>(cards);
//					//先把刻子提出来，这是基本胡牌条件,剩下的牌再看能不能胡
//					cards2.remove((Byte)kezi.card1);
//					cards2.remove((Byte)kezi.card2);
//					cards2.remove((Byte)kezi.targetCard);
//					MJContext context = new MJContext();
//					context.cardsInCard.addAll(cards);
//					MjCheckResult ret = mjRule.canHu(context);
//					if(ret != null) {
//						return ret;
//					}
//				}
//			}
//		}

        return null;
    }

    public ChuTingModel canTingInternal(List<Byte> handCards, List<Integer> cardsDown, MjCheckContext c) {
        Map<Byte, Set<Byte>> chuAndTingMap = new HashMap<Byte, Set<Byte>>();
        Set<Byte> allCards = MJHelper.getAllUniqCard();
        Set<Byte> set = MJHelper.getUniqCardList(handCards);
        for (byte card2Remove : set) {
            if (c.card2Remove > 0 && c.card2Remove != card2Remove) {
                continue; //只检查指定情况的
            }
            for (byte card2Ting : allCards) {
                if (c.card2Ting > 0 && c.card2Ting != card2Ting) {
                    continue;//检查指定情况的
                }
//				MjCheckResult ret = canTingThisCard(handCards, cardsDown, card2Remove, card2Ting, c);
                //重写
                boolean canTingAndCheckRule = canTingAndCheckRule(handCards, cardsDown, card2Remove, card2Ting, c);
                if (!canTingAndCheckRule) {
                    continue;
                }
                // 可以胡
                if (chuAndTingMap.get(card2Remove) == null) {
                    chuAndTingMap.put(card2Remove, new HashSet<Byte>());
                }
                chuAndTingMap.get(card2Remove).add(card2Ting);
            }
        }
        ChuTingModel model = new ChuTingModel();
        model.chuAndTingMap = chuAndTingMap;
        return model;
    }

    /**
     * 校验是否可以听牌
     * 1.选举出所有可以成为将牌的牌
     * 2.遍历将牌集合,删除他可以成牌.
     *
     * @param handCards   手牌(包含即将打出去的牌)
     * @param cardsDown   吃/碰/杠/粘的牌
     * @param card2Remove 即将打出去的牌
     * @param card2Ting   检测可以听的牌
     * @return
     */
    private boolean canTingAndCheckRule(List<Byte> handCards, List<Integer> cardsDown, byte card2Remove, byte card2Ting, MjCheckContext c) {
        List<Byte> jiangPaiList = new ArrayList<Byte>();
        List<Byte> shouPaiTemp = new ArrayList<Byte>();
        shouPaiTemp.addAll(handCards);
        shouPaiTemp.remove(Byte.valueOf(card2Remove + ""));
        shouPaiTemp.add(card2Ting);
        Collections.sort(shouPaiTemp);
        Map<Byte, Byte> maps = new HashMap<Byte, Byte>();
        //记录每张牌的数量到 maps
        for (Byte b : shouPaiTemp) {
            Byte t = maps.get(b);
            if (t == null) {
                maps.put(b, (byte) 1);
            } else {
                t++;
                maps.put(b, t);
            }
        }
        for (Map.Entry<Byte, Byte> e : maps.entrySet()) {
            //相同的牌数量大于等于2 添加到将牌中
            if (e.getValue() >= 2) {
                jiangPaiList.add(e.getKey());
            }
        }
        for (Byte b : jiangPaiList) {
            List shouPaiTemp2 = new ArrayList();
            shouPaiTemp2.addAll(shouPaiTemp);
            shouPaiTemp2.remove(Byte.valueOf(b + ""));
            shouPaiTemp2.remove(Byte.valueOf(b + ""));
            if (mjRule.canChengPai(shouPaiTemp2, new MjCheckResult())) {
                if (checkHuRule(shouPaiTemp, cardsDown, b)) {
                    if (c.desk.canWuJiaBuHu()) {
                        List shouPaiTemp3 = new ArrayList();
                        shouPaiTemp3.addAll(shouPaiTemp);
                        shouPaiTemp3.remove(Byte.valueOf(card2Ting + ""));
                        if (isJiaHu(shouPaiTemp3, c.desk, card2Ting, 0, c.position)) return true;
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 1.清一色,返回false
     * 2.有两个红中或以上,返回true
     * 3.有1,9且去除将牌后有顺有有碰
     *
     * @param handcards
     * @param cardsDown
     * @return
     */
    private boolean checkHuRule(List<Byte> handcards, List<Integer> cardsDown, Byte b) {
        if (!has2Color(handcards, cardsDown)) {
            logger.info("act=checkHuBaseRule;desc=清一色不能胡;");
            return false;
        }
        int hongZhongNum = getHongZhongNum(handcards, cardsDown);
        if (hongZhongNum >= 2) {
            return true;
        }
        List shouPaiTemp = new ArrayList();
        shouPaiTemp.addAll(handcards);
        shouPaiTemp.remove(Byte.valueOf(b + ""));
        shouPaiTemp.remove(Byte.valueOf(b + ""));
        return has19(handcards, cardsDown) && hasKe(shouPaiTemp, cardsDown) && hasShun(shouPaiTemp, cardsDown);
    }

    private MjCheckResult canHu(List<Byte> cards) {
        BaseMJRule rule = new BaseMJRule();
        MJContext ctx = new MJContext();
        ctx.cardsInCard = cards;
        MjCheckResult ret = rule.canHu(ctx);
        return ret;
    }

    private MjCheckResult canChengPai(List<Byte> cards) {
        BaseMJRule rule = new BaseMJRule();
        MJContext ctx = new MJContext();
        ctx.cardsInCard = cards;
        MjCheckResult ret = rule.canHu(ctx);
        return ret;
    }

    /**
     * 判断是否是夹胡
     */
    public boolean isJiaHu(List<Byte> handcards, MJDesk desk, byte card, int zhidui, int position) {
        //夹胡
        {
            List<Byte> cards = new ArrayList<Byte>();
            cards.addAll(handcards);
            cards.add(card);
            Byte card2 = (byte) (card + 1);
            Byte card3 = (byte) (card - 1);
            if (cards.remove((Byte) card) && cards.remove(card2) && cards.remove(card3)) {
                if (canHu(cards) != null)
                    return true;
            }
        }

        // 3、7 夹
        if (desk.can37Jia()) {
            {
                List<Byte> cards = new ArrayList<Byte>();
                cards.addAll(handcards);
                cards.add(card);
                int num = (card & 0X0F);
                Byte card_1 = (byte) (card - 1);
                Byte card_2 = (byte) (card - 2);
                if (num == 3 && cards.remove((Byte) card) && cards.remove(card_1) && cards.remove(card_2)) {
                    if (canHu(cards) != null)
                        return true;
                }
            }
            {
                List<Byte> cards = new ArrayList<Byte>();
                cards.addAll(handcards);
                cards.add(card);
                int num = (card & 0X0F);
                Byte card1 = (byte) (card + 1);
                Byte card2 = (byte) (card + 2);
                if (num == 7 && cards.remove((Byte) card) && cards.remove(card1) && cards.remove(card2)) {
                    if (canHu(cards) != null)
                        return true;
                }
            }
        }

        // 单调夹
        if (desk.canDanDiaoJia()) {
            List<Byte> cards = new ArrayList<Byte>();
            cards.addAll(handcards);
            cards.add(card);
            if (cards.remove((Byte) card) && cards.remove((Byte) (card))) {
                if (mjRule.canChengPai(cards, new MjCheckResult()))
                    return true;
            }
        }

        // 支对夹
        if (desk.canZhiDuiHU()) {
            if (zhidui > 0) return (zhidui & 0XFF) == card;
        }
        //对倒
        if (desk.canDuidao() || zhidui == 0) {
            for (List<Byte> temp : checkDuiDaoHu(handcards)) {
                if (temp.contains(card)) return true;
            }
        }
        return false;
    }

    /**
     * 校验对倒胡
     *
     * @param cards 不加胡的牌的手牌集合
     * @return 返回对倒的集合
     */
    public List<List<Byte>> checkDuiDaoHu(List<Byte> cards) {
        List<Byte> shouPaitemp = new ArrayList<Byte>();
        List<List<Byte>> resultList = new ArrayList<List<Byte>>();
        shouPaitemp.addAll(cards);
        Map<Byte, Byte> maps = new HashMap<Byte, Byte>();
        for (Byte b : shouPaitemp) {
            Byte t = maps.get(b);
            if (t == null) {
                maps.put(b, (byte) 1);
            } else {
                t++;
                maps.put(b, t);
            }
        }
        List<Byte> duiDaoList = new ArrayList<Byte>();
        for (Map.Entry<Byte, Byte> e : maps.entrySet()) {
            if (e.getValue() >= 2) {
                duiDaoList.add(e.getKey());
            }
        }
        if (duiDaoList.size() >= 2) {
            for (byte b : duiDaoList) {
                List<Byte> duiDaoTemp = new ArrayList<Byte>();
                duiDaoTemp.addAll(duiDaoList);
                duiDaoTemp.remove(Byte.valueOf(b + ""));
                for (Byte b1 : duiDaoTemp) {
                    shouPaitemp.clear();
                    shouPaitemp.addAll(cards);
                    shouPaitemp.remove(Byte.valueOf(b + ""));
                    shouPaitemp.remove(Byte.valueOf(b + ""));
                    shouPaitemp.remove(Byte.valueOf(b1 + ""));
                    shouPaitemp.remove(Byte.valueOf(b1 + ""));
                    if (mjRule.canChengPai(shouPaitemp, new MjCheckResult())) {
                        List<Byte> tempResult = new ArrayList();
                        tempResult.add(b);
                        tempResult.add(b1);
                        resultList.add(tempResult);
                    }
                }
            }
        }
        System.out.println("校验胡牌对倒集合为===============" + resultList);
        return resultList;
    }

    //检验是否可以听牌
    public boolean canTing(MjCheckContext c) {
        List<Byte> handCards = new ArrayList<Byte>();
        handCards.addAll(c.gameData.mPlayerCards[c.position].cardsInHand);
        if (c.card != 0) {
            //将 摸/别人打的   牌按顺序加入到手牌中
            MJHelper.add2SortedList(c.card, handCards);
        }

        List<Integer> cardsDown = c.gameData.mPlayerCards[c.position].cardsDown;
//		if(c.debug) {
        logger.info("act=canTing;handcards={};downcards={}", MJHelper.getSingleCardListName(handCards), MJHelper.getCompositeCardListName(cardsDown));
//		}
        if (checkTingBaseRule(handCards, cardsDown, c) == false) {
            return false;
        }

        ChuTingModel model = canTingInternal(handCards, cardsDown, c);

        if (model == null || model.chuAndTingMap.isEmpty()) {
            return false;
        }
//		// 是否需要检测支对
//		ZhiduiModel zhiduiModel = new ZhiduiModel();
//		if (c.desk.canZhiDuiHU()) {
//			// 把所有可听的对子提出来
//			zhiduiModel = this.getZhiduiCards(handCards, model);
//
//		}
//		if(c.desk.canWuJiaBuHu()) {
//			Set<Byte> removeSet = new HashSet<Byte>();
//			for(Byte card : model.chuAndTingMap.keySet()) {
//				Set<Byte> tingSet = model.chuAndTingMap.get(card);
//				Set<Integer> zhiduiSet = zhiduiModel.chuAndZhiduiMap.isEmpty()?new HashSet<Integer>():zhiduiModel.chuAndZhiduiMap.get(card);
//				if(zhiduiSet==null){
//					zhiduiSet = new HashSet<>();
//				}
//				Set<Byte> calSet = new HashSet<Byte>();
//				for(byte tingCard : tingSet) {//tingSet不考虑无夹不糊可以胡的牌
//					byte zhidui = zhiduiSet.contains(tingCard+0)? tingCard : 0;
//					List<Byte> temp = new ArrayList<>(handCards);
//					temp.remove((Object)card);
//					if(isJiaHu(temp, c.desk, tingCard, zhidui, c.position)) {
//						calSet.add(tingCard);
//					}
//				}
//				if(calSet.isEmpty()) {
//					removeSet.add(card);
//				} else {
//					tingSet.clear();
//					tingSet.addAll(calSet);
//				}
//			}
//
//			for(Byte card : removeSet) {
//				model.chuAndTingMap.remove(card);
//			}
//			if (model == null || model.chuAndTingMap.isEmpty()) {
//				logger.info("act=canTing;desc=无夹不胡不满足;");
//				return false;
//			}
//		}
        c.gameData.mTingCards[c.position].chuAndTingMap = model.chuAndTingMap;
        logger.info("act=checkTing;deskId={};result={}", c.desk.getDeskID(), new Gson().toJson(model.chuAndTingMap));
        return true;
    }

    private boolean checkTingBaseRule(List<Byte> handCards, List<Integer> cardsDown, MjCheckContext c) {
        // 清一色不能胡，所以也不能听(一色上听，加另一色一张牌是不可能胡牌的)
        if (has2Color(handCards, cardsDown) == false) {
            if (c.debug) {
                logger.info("act=checkTingBaseRule;error=onecolor;desc=清一色不能听;");
            }
            return false;
        }
        // 红中的牌数
        int hongzhong_num = getHongZhongNum(handCards, cardsDown);

        // 如果没有红中
        if (hongzhong_num == 0) {
            //没有红中，也没有幺九不能听
            if (has19(handCards, cardsDown) == false) {
                if (c.debug) {
                    logger.info("act=checkTingBaseRule;error=no19;desc=幺九条件不能听;");
                }
                return false;
            }
            // 没有刻子，也没有有2对不可能听，（2对可以听,听刻子）
            if (hasKe(handCards, cardsDown) == false && hasTwoPair(handCards) == false) {
                if (c.debug) {
                    logger.info("act=checkTingBaseRule;error=nokezi;desc=没有刻字同时不是听刻子;");
                }
                return false;
            }
        }

        // 门清不能听
        if (!c.isQiangTing && cardsDown.size() == 0) {
//			if(c.debug) {
            logger.info("act=checkTingBaseRule;error=mengqing;desc=门清不能听;");
//			}
            return false;
        }
        return true;
    }


    //

    // 有几个红中
    public int getHongZhongNum(List<Byte> cardsInHand, List<Integer> cardsDown) {
        int num = 0;
        for (int i = 0; i < cardsInHand.size(); i++) {
            int b = (int) cardsInHand.get(i) & 0xff;
            if (b == MJConstants.MAHJONG_CODE_HONG_ZHONG) {
                num++;
            }
        }

        if (cardsDown == null)
            return num;

        for (int i = 0; i < cardsDown.size(); i++) {
            int bb = cardsDown.get(i) & 0xff;
            if (bb == MJConstants.MAHJONG_CODE_HONG_ZHONG)
                num += 3;
        }

        //
        return num;
    }

    // 有没有“幺”或“九”。
    public boolean has19(List<Byte> cardsInHand, List<Integer> cardsDown) {
        for (int i = 0; i < cardsInHand.size(); i++) {
            int bb = cardsInHand.get(i);
            if (bb == 1 || bb == 9 || bb == 0x11 || bb == 0x19 || bb == 0x21 || bb == 0x29)
                return true;
        }

        for (int i = 0; i < cardsDown.size(); i++) {
            int bb = cardsDown.get(i);
            byte b1 = (byte) (bb & 0xff);
            byte b2 = (byte) ((bb >> 8) & 0xff);
            byte b3 = (byte) ((bb >> 16) & 0xff);
            //
            if (b1 == 1 || b1 == 9 || b1 == 0x11 || b1 == 0x19 || b1 == 0x21 || b1 == 0x29)
                return true;
            if (b2 == 1 || b2 == 9 || b2 == 0x11 || b2 == 0x19 || b2 == 0x21 || b2 == 0x29)
                return true;
            if (b3 == 1 || b3 == 9 || b3 == 0x11 || b3 == 0x19 || b3 == 0x21 || b3 == 0x29)
                return true;
        }
        return false;
        //
    }

    // 是否有3张一样的“刻”牌
    public boolean hasKe(List<Byte> cardsInHand, List<Integer> cardsDown) {
        for (int i = 0; i < cardsInHand.size() - 2; i++) {
            Byte b1 = cardsInHand.get(i);
            Byte b2 = cardsInHand.get(i + 1);
            Byte b3 = cardsInHand.get(i + 2);
            if (b1 == b2 && b2 == b3)
                return true;
        }

        for (int i = 0; i < cardsDown.size(); i++) {
            int bb = cardsDown.get(i);
            byte b1 = (byte) (bb & 0xff);
            byte b2 = (byte) ((bb >> 8) & 0xff);
            byte b3 = (byte) ((bb >> 16) & 0xff);

            if (b1 == b2 && b2 == b3)
                return true;
        }

        return false;
    }

    // 手里是否至少有两对牌
    public boolean hasTwoPair(List<Byte> cardsInHand) {
        //
        int pair_num = 0;
        for (int i = 0; i < cardsInHand.size() - 1; i++) {
            Byte b1 = cardsInHand.get(i);
            Byte b2 = cardsInHand.get(i + 1);
            if (b1 == b2)
                pair_num++;
        }
        //
        if (pair_num >= 2)
            return true;

        return false;
    }

    private boolean found_card(int c, List<Byte> cards) {
        for (int i = 0; i < cards.size(); i++) {
            int b1 = cards.get(i) & 0xff;
            if (b1 == c)
                return true;
        }
        //
        return false;
    }

    // 是否有3张连在一起的“顺”牌
    public boolean hasShun(List<Byte> cardsInHand, List<Integer> cardsDown) {
        for (int i = 0; i < cardsInHand.size() - 2; i++) {
            Byte b1 = cardsInHand.get(i);
            if (found_card(b1 + 1, cardsInHand) && found_card(b1 + 2, cardsInHand))// 123,有序
                return true;
        }

        for (int i = 0; i < cardsDown.size(); i++) {
            int bb = cardsDown.get(i);
            byte b1 = (byte) (bb & 0xff);
            byte b2 = (byte) ((bb >> 8) & 0xff);
            byte b3 = (byte) ((bb >> 16) & 0xff);

            if (b1 == b2 - 1 && b2 == b3 - 1)// 123,有序
                return true;

            if (b3 == b2 - 1 && b2 == b1 - 1)// 123,有序
                return true;
        }
        return false;
    }


    // 玩家收牌和吃碰牌，看看是否至少有2种花色
    public boolean has2Color(List<Byte> cardsInHand, List<Integer> cardsDown) {
        int color_num = 0;
        boolean found0 = false; // 万
        boolean found1 = false; // 条
        boolean found2 = false; // 筒
        //
        for (int i = 0; i < cardsInHand.size(); i++) {
            int bb = cardsInHand.get(i) & 0xff;
            // 红中不算色
            if (bb == MJConstants.MAHJONG_CODE_HONG_ZHONG)
                continue;

            int color = bb & MJConstants.MAHJONG_CODE_COLOR_MASK;
            if (color == 0 && found0 == false) {
                found0 = true;
                color_num++;
            } else if (color == 0x10 && found1 == false) {
                found1 = true;
                color_num++;
            } else if (color == 0x20 && found2 == false) {
                found2 = true;
                color_num++;
            }
            //
            if (color_num >= 2)
                return true;
        }
        //
        for (int i = 0; i < cardsDown.size(); i++) {
            int bb = cardsDown.get(i) & 0xff;
            // 红中不算色
            if (bb == MJConstants.MAHJONG_CODE_HONG_ZHONG)
                continue;
            int color = bb & MJConstants.MAHJONG_CODE_COLOR_MASK;
            //
            if (color == 0x0 && found0 == false) {
                found0 = true;
                color_num++;
            } else if (color == 0x10 && found1 == false) {
                found1 = true;
                color_num++;
            } else if (color == 0x20 && found2 == false) {
                found2 = true;
                color_num++;
            }
            //
            if (color_num >= 2)
                return true;
        }
        //

        //
        return false;
    }

    public Map<String,Boolean> checkHu( MjCheckContext c){
        Map<String,Boolean> result = Maps.newHashMap();
        List<Byte> handCards = c.gameData.getCardsInHand(c.position);
        List<Integer> cardsDown = c.gameData.getCardsDown(c.position);
        List<Integer> anGangCards = c.gameData.getAnGangCardsDown(c.position);
        List<Integer> allCard = Lists.newArrayList();
        List<Integer> allDownCard = Lists.newArrayList();

        for (Byte handCard : handCards) {
            allCard.add(handCard & 0xff);
        }

        for (Integer card : cardsDown) {
            int card1 = card & 0xff;
            int card2 = (card >> 8) & 0xff;
            int card3 = (card >> 16) & 0xff;

            allCard.add(card1);
            allCard.add(card2);
            allCard.add(card3);

            allDownCard.add(card1);
            allDownCard.add(card2);
            allDownCard.add(card3);
        }

        return result;
    }

    /**
     * 是否清一色
     */
    private boolean checkQingYiSe(List<Byte> cards) {
        boolean isQingYiSe = false;
        int colorCount = 0;
        //是否有万子
        boolean hasWang = false;
        //是否有条子
        boolean hasTiao = false;
        //是否有筒子
        boolean hasTong = false;
        //是否有东南西北中发白  字牌
        boolean hasZhi = false;
        for (Byte card : cards) {
            if (!hasWang) {
                if (isWang(card & 0xff)) {
                    colorCount++;
                    hasWang = true;
                }
            }

            if (!hasTiao) {
                if (isTiao(card & 0xff)) {
                    colorCount++;
                    hasTiao = true;
                }
            }

            if (!hasTong) {
                if (isTong(card & 0xff)) {
                    colorCount++;
                    hasTong = true;
                }
            }

            //有字肯定不是清一色
            if (isZhi(card & 0xff)) {
                return false;
            }

        }
        if (colorCount == 1) {
            isQingYiSe = true;
        }
        return isQingYiSe;
    }

    /**
     * 字一色
     */
    public boolean checkZhiYiSe(List<Byte> cards){
        for (Byte card : cards) {
            if (!isZhi(card & 0xff)){
                return false;
            }
        }
        return true;
    }

    /**
     * 小七对
     * 豪华七对
     */
    public Map<String,Boolean> checkQiDui(List<Byte> cards){
        Map<String,Boolean> result = Maps.newHashMap();
        result.put("xiaoQiDui",true);
        result.put("haoHuaQiDui",false);
        Map<Integer,Integer> countMap = Maps.newHashMap();
        for (Byte card : cards) {
            int template = card & 0xff;
            int count = 0;
            if (countMap.containsKey(template)){
                count = countMap.get(template);
            }
            count ++;
            countMap.put(template,count);
        }
        for (Integer count : countMap.values()) {
            if (count == 1 || count == 3 ){
                result.put("xiaoQiDui",false);
                result.put("haoHuaQiDui",false);
                break;
            }
            if (count == 4){
                result.put("haoHuaQiDui",true);
            }
        }
        return result;
    }


    /**
     *  十三幺
     *  七星
     * */

    public Map<String,Boolean> checkThirteenYao(List<Byte> cards){
        Map<String,Boolean> result = Maps.newHashMap();
        Map<Integer,Integer> countMap = Maps.newHashMap();
        result.put("thirteen",false);
        result.put("seven",false);
        for (int i = 0; i < cards.size() - 1; i++) {
            int card = cards.get(i) & 0xff;
            //字牌不能重复
            if (card >= MJConstants.MAHJONG_CODE_DONG_FENG){
                if (countMap.containsKey(card)){
                    break;
                }
                countMap.put(card,1);
            }
            int template = cards.get(i + 1) & 0xff;
            if (template - card <= 2){
                break;
            }
        }
        result.put("thirteen",true);
        if (countMap.size() >= 7){
            result.put("seven",true);
        }
        return result;
    }




    private boolean isZhi(int card) {
        boolean isZhi = false;
        if (card >= MJConstants.MAHJONG_CODE_DONG_FENG) {
            isZhi = true;
        }
        return isZhi;
    }

    private boolean isWang(int card) {
        boolean isWang = false;
        if (card >= MJConstants.MAHJONG_CODE_ONE_WANG && card <= MJConstants.MAHJONG_CODE_NINE_WANG) {
            isWang = true;
        }
        return isWang;
    }

    private boolean isTiao(int card) {
        boolean isWang = false;
        if (card >= MJConstants.MAHJONG_CODE_ONE_TIAO && card <= MJConstants.MAHJONG_CODE_NINE_TIAO) {
            isWang = true;
        }
        return isWang;
    }

    private boolean isTong(int card) {
        boolean isTong = false;
        if (card >= MJConstants.MAHJONG_CODE_ONE_TONG && card <= MJConstants.MAHJONG_CODE_NINE_TONG) {
            isTong = true;
        }
        return isTong;
    }

}
