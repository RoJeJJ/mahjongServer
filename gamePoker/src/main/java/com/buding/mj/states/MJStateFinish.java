package com.buding.mj.states;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import packet.mj.MJ.GameOperFinalSettleSyn;
import packet.mj.MJ.GameOperPlayerHuSyn;
import packet.mj.MJ.GameOperPlayerSettle;
import packet.mj.MJ.PlayerFinalResult;

import com.buding.api.context.GameContext;
import com.buding.api.context.PlayFinalResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.game.events.PlatformEvent;
import com.buding.mj.constants.MJConstants;
import com.buding.mj.helper.MJHelper;
import com.buding.mj.helper.PokerPushHelper;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @author tiny qq_381360993
 * @Description: 结算状态
 * 
 */
public class MJStateFinish extends MJStateCommon {
	GameContext ctx = null;
	boolean skipHuSettle = false;

	@Override
	public void handlePlayerStatusChange(int position) {
		
	}

	@Override
	public void handleReconnectFor(int position) {
		logger.info("act=handleReconnectFor;state=finish;position={}", position);
	}

	@Override
	public void onEnter() {
		this.logger.info("finish , onEnter is called");
		this.mGameTimer.KillDeskTimer();
		
		skipHuSettle = false;
		
		ctx = new GameContext();
		
		ctx.handNum = this.mGameData.handNum;
		ctx.quanNum = this.mGameData.quanNum;
		ctx.nextHandNum = this.mGameData.handNum + 1;
		ctx.nextQuanNum = this.mGameData.quanNum;
		ctx.bankerPos = this.mGameData.mPublic.mbankerPos;
		ctx.winerPos = this.mGameData.mGameHu.position;
		if (this.mCardLogic.isFinishQuan(this.mGameData, this.mDesk)) {
			ctx.nextQuanNum++;
		}
		ctx.playerHandResults = this.mGameData.mPlayerHandResult;
		
		//是否解散
		if(this.mGameData.dismissing) {			
			if(this.mDesk.isVipTable()) {
				// 推送总结算画面
				pushFinalSettleMsg(this.mGameData, this.mDesk);				
			}
			this.mGameTimer.KillDeskTimer();
			ctx.playerFinalResult = this.mGameData.mPlayerFinalResult;
			this.mDesk.finalSettle(ctx);
			this.mDesk.onGameOver();
			return;
		}
		
		int waitSeconds = 7;
		// 有没有下一局
		if (this.mDesk.hasNextGame(ctx) == false) {
			if(this.mDesk.isVipTable()) {
//				// 推送总结算画面
				skipHuSettle = true;
				pushPlayerHuMsg(this.mGameData, this.mDesk);
				this.mDesk.handSettle(ctx);
				waitSeconds = 3; //3秒后到总结算页面
			} else {
				pushPlayerHuMsg(this.mGameData, this.mDesk);
				this.mDesk.handSettle(ctx);
				ctx.playerFinalResult = this.mGameData.mPlayerFinalResult;
				this.mDesk.finalSettle(ctx);
				this.mDesk.onGameOver();
				waitSeconds = 5;
			}
		} else {
			pushPlayerHuMsg(this.mGameData, this.mDesk);
			this.mDesk.handSettle(ctx);
			this.mDesk.ready4NextGame(ctx);
		}

		this.mGameData.mActor.gameState = MJConstants.MJStateFinish;
		this.mGameTimer.SetDeskTimer(waitSeconds * 1000);
	}

	@Override
	public void onPlatform(PlatformEvent event) {

	}

	private void pushFinalSettleMsg(GameData gameData, MJDesk desk) {
		GameOperFinalSettleSyn.Builder gb = GameOperFinalSettleSyn.newBuilder();
		gb.setSettleDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		for(PlayFinalResult r : gameData.mPlayerFinalResult.playDetail) {
			if(r.playerId <= 0) {
				continue;
			}
			PlayerFinalResult.Builder pb = PlayerFinalResult.newBuilder();
			pb.setBankerCount(r.bankerCount);
			pb.setBaoZhongBaoCount(r.baoZhongBaoCount);
			pb.setHeadImage(gameData.mPlayers[r.pos].headImg);
			pb.setHuCount(r.huCount);
			pb.setKaiPaiZhaCount(r.kaiPaiZhaCount);
			pb.setMoBaoCount(r.mobaoCount);
			pb.setPaoCount(r.paoCount);
			pb.setPlayerId(r.playerId);
			pb.setPlayerName(r.playerName);
			pb.setPosition(r.pos);
			pb.setRoomOwner(desk.getDeskOwner() == r.playerId);
			pb.setScore(r.score);
			pb.setHeadImage(r.headImg);
			gb.addDetail(pb);
		}
		PokerPushHelper.pushFinalSettleInfo(desk, 0, gb, MJConstants.SEND_TYPE_ALL);
	}

	//推送胡牌数据
	private void pushPlayerHuMsg(GameData gameData, MJDesk desk) {
		GameOperPlayerHuSyn.Builder gb = GameOperPlayerHuSyn.newBuilder();
		//开牌炸和流局不要显示宝
		if(gameData.mGameHu.position != -1) {
			int fanType = gameData.mPlayerHandResult.playDetail[gameData.mGameHu.position].fanType;
			if((fanType & MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) != MJConstants.MAHJONG_HU_CODE_KAIPAIZHA) {
				gb.setBao(gameData.mPublic.mBaoCard);
			}
		}
		
		gb.setCard(gameData.mGameHu.huCard);
		gb.setPosition(gameData.mGameHu.position);
		gb.setPaoPosition(-1);
		for (PlayerInfo p : (List<PlayerInfo>) desk.getPlayers()) {
			GameOperPlayerSettle.Builder bb = GameOperPlayerSettle.newBuilder();
			bb.setFanNum(gameData.mPlayerHandResult.playDetail[p.position].fanNum);
			bb.setFanType(gameData.mPlayerHandResult.playDetail[p.position].fanType);
			for(String fan : MJHelper.getFanDescList(gameData.mPlayerHandResult.playDetail[p.position].fanType,p.getPlayerID(),gameData)) {
				bb.addFanDetail(fan);
			}
			bb.setPosition(p.position);
			for (byte card : gameData.mPlayerCards[p.position].cardsInHand) {
				bb.addHandcard(card);
			}
			if((bb.getFanType() & MJConstants.MAHJONG_HU_CODE_DIAN_PAO) == MJConstants.MAHJONG_HU_CODE_DIAN_PAO) {
				gb.setPaoPosition(p.position);
			}
			if((bb.getFanType() & MJConstants.MAHJONG_HU_CODE_WIN) == MJConstants.MAHJONG_HU_CODE_WIN) {
				gb.setWinType(MJHelper.getHuType(bb.getFanType()));
			}
			bb.setScore(gameData.mPlayerFinalResult.playDetail[p.position].score);
			bb.setCoin(p.coin);
			gb.addDetail(bb);
		}
		gb.setSkipHuSettle(skipHuSettle);
				
		for (PlayerInfo p : (List<PlayerInfo>) desk.getPlayers()) {
			int resultType = MJHelper.getResultType(gameData.mPlayerHandResult.playDetail[p.position].fanType);
			gb.setResultType(resultType);
			
			logger.info("huMsg:"+JsonFormat.printToString(gb.build()));

			PokerPushHelper.pushPlayerHuMsg(desk, p.position, gb, MJConstants.SEND_TYPE_SINGLE);	
		}
		//记录胡牌结算等信息
		gameData.recorder.playerResultDetail = gb;
	}

	@Override
	public void onDeskTimer() {
		this.logger.info("finish , onDeskTimer is called");
		this.mGameTimer.KillDeskTimer();
		
		if(skipHuSettle) {
			skipHuSettle = false;
			ctx.playerFinalResult = this.mGameData.mPlayerFinalResult;
			pushFinalSettleMsg(this.mGameData, this.mDesk);
			this.mDesk.finalSettle(ctx);
			this.mDesk.onGameOver();
			this.mGameData.mActor.gameState = MJConstants.MJStateFinish;
			this.mGameTimer.SetDeskTimer(1 * 1000);//一秒后进入下一局
			return;
		}
		
		if (this.mDesk.hasNextGame(ctx)) {
			this.mDesk.startNextGame(ctx);
		}
	}

	@Override
	public void onPlayerTimerEvent(int position) {
		
	}

	@Override
	public void onExit() {

	}

	@Override
	public void handlePlayerHangup(int position) {
		
	}

}
