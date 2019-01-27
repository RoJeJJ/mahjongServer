package com.buding.mj.helper;

import packet.mj.MJ.GameOperActorSyn;
import packet.mj.MJ.GameOperBaoChangeSyn;
import packet.mj.MJ.GameOperFinalSettleSyn;
import packet.mj.MJ.GameOperHandCardSyn;
import packet.mj.MJ.GameOperPlayerActionNotify;
import packet.mj.MJ.GameOperPlayerActionSyn;
import packet.mj.MJ.GameOperPlayerHuSyn;
import packet.mj.MJ.GameOperPublicInfoSyn;
import packet.mj.MJBase.GameOperType;
import packet.mj.MJBase.GameOperation;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class MsgBuilder {
	public static GameOperation.Builder getPacketForPublicInfo(boolean showBaoCard, int baoCard, int cardLeft, int bankerPos) {
		GameOperPublicInfoSyn.Builder gb = GameOperPublicInfoSyn.newBuilder();
//		gb.setBaoCard(showBaoCard ? baoCard : -1);
		gb.setCardLeft(cardLeft);
//		gb.setBankerPos(bankerPos);

		GameOperation.Builder bb = GameOperation.newBuilder();
		bb.setContent(gb.build().toByteString());
		bb.setOperType(GameOperType.GameOperPublicInfoSyn);
		return bb;
	}
	
	public static GameOperation.Builder getPacketForHandcardSyn(GameOperHandCardSyn.Builder gb) {
		GameOperation.Builder bb = GameOperation.newBuilder();
		bb.setContent(gb.build().toByteString());
		bb.setOperType(GameOperType.GameOperHandCardSyn);
		return bb;
	}

	public static GameOperation.Builder getPacketForPlayerHu(GameOperPlayerHuSyn.Builder gb) {
		GameOperation.Builder bb = GameOperation.newBuilder();
		bb.setContent(gb.build().toByteString());
		bb.setOperType(GameOperType.GameOperPlayerHuSyn);
		return bb;
	}
	
	public static GameOperation.Builder getPacketForFinalSettle(GameOperFinalSettleSyn.Builder gb) {
		GameOperation.Builder bb = GameOperation.newBuilder();
		bb.setContent(gb.build().toByteString());
		bb.setOperType(GameOperType.GameOperFinalSettleSyn);
		return bb;
	}

	public static GameOperation.Builder getPacketForActionNofity(
			GameOperPlayerActionNotify.Builder gb) {
		GameOperation.Builder bb = GameOperation.newBuilder();
		bb.setContent(gb.build().toByteString());
		bb.setOperType(GameOperType.GameOperPlayerActionNotify);
		return bb;
	}

	public static GameOperation.Builder getPacketForActorSyn(int position,
			int timeout, int cardLeft) {
		GameOperActorSyn.Builder acb = GameOperActorSyn.newBuilder();
		acb.setPosition(position);
		acb.setTimeLeft(timeout);

		GameOperation.Builder bb = GameOperation.newBuilder();
		bb.setContent(acb.build().toByteString());
		bb.setOperType(GameOperType.GameOperActorSyn);
		return bb;
	}

	public static GameOperation.Builder getPacketForActionSyn(
			GameOperPlayerActionSyn.Builder gb) {
		GameOperation.Builder bb = GameOperation.newBuilder();
		bb.setContent(gb.build().toByteString());
		bb.setOperType(GameOperType.GameOperPlayerActionSyn);
		return bb;
	}
	
	public static GameOperation.Builder getPacketForBaoChangeSyn(
			GameOperBaoChangeSyn.Builder gb) {
		GameOperation.Builder bb = GameOperation.newBuilder();
		bb.setContent(gb.build().toByteString());
		bb.setOperType(GameOperType.GameOperBaoChangeSyn);
		return bb;
	}
}
