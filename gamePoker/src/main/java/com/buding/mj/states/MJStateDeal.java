package com.buding.mj.states;

import org.apache.commons.lang.StringUtils;

import com.buding.api.player.PlayerInfo;
import com.buding.game.GameRecorder;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.mj.constants.MJConstants;
import com.google.gson.Gson;

/**
 * @author tiny qq_381360993
 * @Description: 洗牌、发牌状态
 * 
 */
public class MJStateDeal extends MJStateCommon {

	@Override
	public void handlePlayerStatusChange(int position) {
		
	}

	@Override
	public void handleReconnectFor(int position) {
		logger.info("act=handleReconnectFor;state=deal;position={}", position);
	}

	@Override
	public void onEnter() {
		this.mGameData.mActor.gameState = MJConstants.MJStateDeal;
		
		String replayData = this.mDesk.getReplyData();
		if(StringUtils.isNotBlank(replayData)) {
			GameRecorder recorder = new Gson().fromJson(replayData, GameRecorder.class);
			this.mGameData.replaying = true;
			this.mGameData.mPublic.isContinueBanker = recorder.isContinueBanker;
			if(recorder.bankerPos > 0) {
				this.mGameData.mPublic.mbankerPos = recorder.bankerPos;
				this.mGameData.mPublic.mBankerUserId = recorder.bankerUserId;	
			}			
			this.mGameData.mGameHu.position = recorder.currentHuPlayerIndex;
		} else {
			// 处理庄家
			// 判断是否连庄
			this.mGameData.mPublic.isContinueBanker = false;
			if ((this.mGameData.mGameHu.position != -1)
					&& (this.mGameData.mPublic.mbankerPos == this.mGameData.mGameHu.position)) {
				this.mGameData.mPublic.isContinueBanker = true;
			}
			this.mCardDealer.dealCard();
		}
		
		this.mCardLogic.selectBanker(this.mGameData, this.mDesk);// 庄家移动到下个位置
		
		this.mGameData.mGameHu.reset();
		
//		logger.info("游戏正式开始, 是否继续当庄isDealerAgain: " + this.mGameData.mPublic.isContinueBanker + "\n"
//				+ ",4人dealerPos的位置: " + this.mGameData.mPublic.mbankerPos);
		
		this.mGameData.handStartTime = System.currentTimeMillis();
		this.mGameData.handEndTime = 0;
		this.mGameData.handNum++;// 局数加一
		
		this.mGameData.recorder.deskId = mDesk.getDeskID();
		
		// 发牌
		this.mCardLogic.sendCards(this.mGameData, this.mDesk);
		
		logger.info("act=gameDeal;isDealerAgain={};bankerPos={};bankerPlayerId={};handStartTime={};handNum={};seq={}",
				this.mGameData.mPublic.isContinueBanker, this.mGameData.mPublic.mbankerPos,this.mGameData.mPublic.mBankerUserId,
				this.mGameData.handStartTime, this.mGameData.handNum, this.mGameData.gameSeq);
		
		
		//2秒后开始游戏
		this.mGameTimer.KillDeskTimer();
		this.mGameTimer.SetDeskTimer(10);
	}

	@Override
	public void onPlatform(PlatformEvent event) {
		switch (event.eventID) {

		case GameLogicEvent.Game_Dismiss: {
						
			this.mGameData.dismissing = true;
			// //1秒后状态跳转
			this.mGameTimer.KillDeskTimer();
			
			DispatchEvent e = new DispatchEvent();
			e.eventID = MJConstants.MJStateFinish;
			this.mDispatcher.StateDispatch(e);			
		}
			break;

		default: {
			super.onPlatform(event);
			break;
		}			
		}
	}

	@Override
	public void onDeskTimer() {
		this.logger.info("deal , onDeskTimer is called; deskId={}", mDesk.getDeskID());
		this.mGameTimer.KillDeskTimer();

		DispatchEvent event = new DispatchEvent();
		event.eventID = MJConstants.MJStateRun;
		this.mDispatcher.StateDispatch(event);
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
