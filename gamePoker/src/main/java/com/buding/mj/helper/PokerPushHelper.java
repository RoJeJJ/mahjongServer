package com.buding.mj.helper;

import java.util.List;

import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import packet.mj.MJ;
import packet.mj.MJ.GameOperBaoChangeSyn;
import packet.mj.MJ.GameOperFinalSettleSyn;
import packet.mj.MJ.GameOperHandCardSyn;
import packet.mj.MJ.GameOperPlayerActionNotify;
import packet.mj.MJ.GameOperPlayerActionSyn;
import packet.mj.MJ.GameOperPlayerHuSyn;

import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.Action;
import com.buding.game.GameData;
import com.buding.mj.constants.MJConstants;
import com.google.gson.Gson;
import com.google.protobuf.MessageLite;
import com.googlecode.protobuf.format.JsonFormat;
import packet.msgbase.MsgBase;

/**
 * @author tiny qq_381360993
 * @Description:
 */
public class PokerPushHelper {
    private static Logger logger = LoggerFactory.getLogger(PokerPushHelper.class);
    private static boolean debugPacket = true;

    public static void pushActionNofity(GameData gameData, MJDesk desk, int receiver, GameOperPlayerActionNotify.Builder gb, int sendType) {
        gb.setSeq(gameData.genSeq());
        String desc = "服务器推送:摸牌(" + MJHelper.getSingleCardName(gb.getNewCard()) + "),可杠:(" + MJHelper.getSingleCardName(gb.getPengArg()) + "),可碰:(" + MJHelper.getSingleCardName(gb.getPengArg()) + "),可吃(" + MJHelper.getChiComboStr(gb.getChiArgList()) + "),出牌表:(" + MJHelper.getCompositeCardListName(gb.getTingListList()) + "),支对表:(" + MJHelper.getCompositeCardListName(gb.getTingDzsList()) + ")";
        Action a = gameData.recorder.recordPlayerAction(gb.getSeq(), gb.getPosition(), gb.getActions(), 0, 0, desc, 1);
        if (debugPacket) {
            logger.info("act=pokerMsg;deskId={};type=ActionNotify[{}]", desk.getDeskID(), new Gson().toJson(a));
        }
        pushMsg(desk, receiver, MsgBuilder.getPacketForActionNofity(gb), sendType);
    }

    public static void pushBaoChange(MJDesk desk, int receiver, GameOperBaoChangeSyn.Builder gb, int sendType) {
        if (debugPacket) {
            logger.info("act=pokerMsg;deskId={};type=baoChange[{}]", desk.getDeskID(), JsonFormat.printToString(gb.build()));
        }
        pushMsg(desk, receiver, MsgBuilder.getPacketForBaoChangeSyn(gb), sendType);
    }

    public static void pushActionSyn(MJDesk desk, int receiver, GameOperPlayerActionSyn.Builder gb, int sendType) {
        if (debugPacket) {
            logger.info("act=pokerMsg;deskId={};type=actionSyn[{}]", desk.getDeskID(), JsonFormat.printToString(gb.build()));
        }
        pushMsg(desk, receiver, MsgBuilder.getPacketForActionSyn(gb), sendType);
    }

    public static void pushActorSyn(MJDesk desk, int receiver, int position, int timeLeft, int cardLeft, int sendType) {
        if (debugPacket) {
            logger.info("act=pushActorSyn;deskId={};receiver={};position={};timeLeft={};cardLeft={}", desk.getDeskID(), receiver, position, timeLeft, cardLeft);
        }
        pushMsg(desk, receiver, MsgBuilder.getPacketForActorSyn(position, timeLeft, cardLeft), sendType);
    }

    public static void pushPlayerTingMsg(MJDesk desk, int receiver, GameOperPlayerActionSyn.Builder gb, int sendType) {
        if (debugPacket) {
            logger.info("act=pokerMsg;deskId={};type=playerTing[{}]", desk.getDeskID(), JsonFormat.printToString(gb.build()));
        }
        pushMsg(desk, receiver, MsgBuilder.getPacketForActionSyn(gb), sendType);
    }

    public static void pushPlayerHuMsg(MJDesk desk, int receiver, GameOperPlayerHuSyn.Builder gb, int sendType) {
        if (debugPacket) {
            logger.info("act=pokerMsg;deskId={};receiver={};type=playerTing[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(gb.build()));
        }
        pushMsg(desk, receiver, MsgBuilder.getPacketForPlayerHu(gb), sendType);
    }

    public static void pushFinalSettleInfo(MJDesk desk, int receiver, GameOperFinalSettleSyn.Builder gb, int sendType) {
        if (debugPacket) {
            logger.info("act=pokerMsg;deskId={};receiver={};type=finalSettle[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(gb.build()));
        }
        pushMsg(desk, receiver, MsgBuilder.getPacketForFinalSettle(gb), sendType);
    }

    public static void pushPublicInfoMsg2All(MJDesk desk, GameData data) {
        for (PlayerInfo p : data.mPlayers) {
            if (p == null) {
                continue;
            }
            int receiver = p.position;
            boolean showBaoCard = data.mTingCards[p.position].showBao;
            if (debugPacket) {
                logger.info("act=pushPublicInfo;deskId={};receiver={};showBaoCard={};baocard={};cardLeft={};bankerPos={}", desk.getDeskID(), receiver, showBaoCard, data.mPublic.mBaoCard, data.getCardLeftNum(), data.mPublic.mbankerPos);
            }
            pushMsg(desk, receiver, MsgBuilder.getPacketForPublicInfo(showBaoCard, data.mPublic.mBaoCard, data.getCardLeftNum(), data.mPublic.mbankerPos), MJConstants.SEND_TYPE_SINGLE);
        }
    }

    public static void pushPublicInfoMsg2Single(MJDesk desk, int position, GameData data) {
        PlayerInfo p = data.mPlayers[position];
        int receiver = p.position;
        boolean showBaoCard = data.mTingCards[p.position].showBao;
        if (debugPacket) {
            logger.info("act=pushPublicInfo;deskId={};receiver={};showBaoCard={};baocard={};cardLeft={};bankerPos={}", desk.getDeskID(), receiver, showBaoCard, data.mPublic.mBaoCard, data.getCardLeftNum(), data.mPublic.mbankerPos);
        }
        pushMsg(desk, receiver, MsgBuilder.getPacketForPublicInfo(showBaoCard, data.mPublic.mBaoCard, data.getCardLeftNum(), data.mPublic.mbankerPos), MJConstants.SEND_TYPE_SINGLE);
    }

    public static void pushHandCardSyn(MJDesk desk, int receiver, GameOperHandCardSyn.Builder gb, int sendType) {
        if (debugPacket) {
            logger.info("act=pokerMsg;deskId={};receiver={};type=handcardSyn[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(gb.build()));
        }
        pushMsg(desk, receiver, MsgBuilder.getPacketForHandcardSyn(gb), sendType);
    }

    public static void pushHandCardSyn(GameData gameData, MJDesk desk, PlayerInfo pl) {
        GameOperHandCardSyn.Builder handCardBuilder = GameOperHandCardSyn.newBuilder();
        // 发给玩家的牌
        List<Byte> cardsInHand = gameData.getCardsInHand(pl.position);
        List<Byte> cardsBefore = gameData.getCardsBefore(pl.position);
        List<Integer> cardsDown = gameData.getCardsDown(pl.position);

        for (int card : cardsInHand) {
            handCardBuilder.addHandCards(card);
        }

        handCardBuilder.setPosition(pl.position);// 玩家的桌子位置
        for (byte b : cardsBefore) {
            handCardBuilder.addCardsBefore(b);
        }
        handCardBuilder.addAllDownCards(cardsDown);
        pushHandCardSyn(desk, pl.position, handCardBuilder, MJConstants.SEND_TYPE_SINGLE);
    }


    public static void pushSeeCard(MJDesk desk, int playerId, MJ.ShowBestCards.Builder data) {
        desk.sendMsgToGoodPlayer(playerId, data.build().toByteArray());
    }

    public static void pushChangeCardSyn(MJDesk desk, int playerId, MJ.ChangeBestCards.Builder data) {
        desk.sendMsgChangeCardSyn(playerId, data.build().toByteArray());
    }

    public static void pushMsg(MJDesk desk, int receiver, MessageLite.Builder b, int sendType) {
        if (sendType == MJConstants.SEND_TYPE_ALL) {
            desk.sendMsg2Desk(b.build().toByteArray());
            return;
        }
        if (sendType == MJConstants.SEND_TYPE_SINGLE) {
            desk.sendMsg2Player(receiver, b.build().toByteArray());
            return;
        }
        desk.sendMsg2DeskExceptPosition(b.build().toByteArray(), Math.abs(receiver));
    }
}
