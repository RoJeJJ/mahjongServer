package com.buding.mj.states;

import com.buding.api.player.PlayerInfo;
import com.buding.api.player.vo.PlayerGameInfo;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.mj.constants.MJConstants;

/**
 * @author tiny qq_381360993
 * @Description: 准备状态
 * 
 */
public class MJStateReady extends MJStateCommon {

	@Override
	public void handlePlayerStatusChange(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleReconnectFor(int position) {
		logger.info("act=handleReconnectFor;state=ready;position={}", position);
	}

	@Override
	public void onEnter() {
		
	}

	@Override
	public void onPlatform(PlatformEvent event) {
		switch (event.eventID) {

		case GameLogicEvent.Game_Begin: {
			this.mGameData.Reset();
			
			this.mGameData.mActor.gameState = MJConstants.MJStateReady;
			
			if(this.mGameData.mPublic.mBankerUserId <= 0) {
				//第一把 游戏开始 初始房主为庄家
				PlayerInfo bankPlayer = this.mDesk.getDeskPlayer(0); //庄家
				this.mGameData.mPublic.mbankerPos = bankPlayer.position;
				this.mGameData.mPublic.mBankerUserId = bankPlayer.playerId;	
			}
			
			// //1秒后状态跳转
			this.mGameTimer.KillDeskTimer();
			this.mGameTimer.SetDeskTimer(500);

		}
			break;
			
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
		}
			break;
		}
	}

	@Override
	public void onDeskTimer() {
		this.logger.info("ready , onDeskTimer is called; deskId={}", mDesk.getDeskID());
		
		this.mGameTimer.KillDeskTimer();

		////跳转到发牌
		DispatchEvent event = new DispatchEvent();
		event.eventID = MJConstants.MJStateDeal;
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
