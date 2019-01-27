package com.buding.mj.states;

import packet.mj.MJ.GameOperPlayerActionSyn;
import packet.mj.MJBase.GameOperation;

import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.NetEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.mj.constants.MJConstants;
import com.buding.mj.helper.PokerPushHelper;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @author tiny qq_381360993
 * @Description: 游戏状态
 * 
 */
public class MJStateRun extends MJStateCommon {
	private boolean mTimerForStateChange = false;

	@Override
	public void handlePlayerStatusChange(int position) {

	}

	@Override
	public void handleReconnectFor(int position) {
		logger.info("act=handleReconnectFor;state=run;position={};seq={};", position, mGameData.recorder.seq);
		
		this.mCardLogic.repushGameData(this.mGameData, this.mDesk, position);
		
		if(this.mGameData.mPlayerAction[position].autoOperation == 1) {
			this.mDesk.onPlayerHangup(position);
		}
	}

	@Override
	public void onNet(NetEvent event) {
		try {
			byte[] data = (byte[]) event.msg;
			GameOperation p = GameOperation.parseFrom(data);
			switch (p.getOperType()) {
			case GameOperPlayerActionSyn: {
				GameOperPlayerActionSyn.Builder gb = GameOperPlayerActionSyn.newBuilder();
				gb.mergeFrom(p.getContent());
				gb.setPosition(event.position);
				mCardLogic.playerOperation(mGameData, mDesk, gb, mDesk.getDeskPlayer(gb.getPosition()));
			}
				break;
			case GameOperReNofity: {
				this.mCardLogic.re_notify_current_operation_player(this.mGameData, this.mDesk, event.position);
			}
				break;
			default: {
				throw new RuntimeException("不支持的类型:" + p.getOperType());
			}
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("", e);
		}
	}

	@Override
	public void onEnter() {
		// TODO
		// 收取台费

		// 发送公共信息
		PokerPushHelper.pushPublicInfoMsg2All(this.mDesk, this.mGameData);

		// 开牌炸胡牌
		// 炸胡：玩家开牌手里有四张一样的牌直接算宝中宝胡另外3家门清
		if (this.mCardLogic.tryKaipaiZha(this.mGameData, this.mDesk)) {
			// 跳转到结算界面
			this.mGameTimer.KillDeskTimer();
			this.mGameTimer.SetDeskTimer(2000);
			this.mTimerForStateChange = true;
			return;
		}
		// 桌子进入开打状态
		this.mGameData.mActor.gameState = MJConstants.MJStateRun;
		this.mGameData.setState(MJConstants.GAME_TABLE_STATE_PLAYING);

		//首先从庄家开始出牌
//		this.mGameData.setCurrentOpertaionPlayerIndex(this.mGameData.mPublic.mbankerPos);
		
		// 提示玩家出牌
//		this.mCardLogic.player_chu_notify(mGameData, mDesk);

		this.checkFinish();
	}

	public void checkFinish() {
		if (this.mGameData.mActor.gameState == MJConstants.MJStateFinish) {
			this.mTimerForStateChange = true;
			this.mGameTimer.KillDeskTimer();
			this.mGameTimer.SetDeskTimer(50); // 立刻跳到游戏结束状态
			return;
		}
		long sleep = 100;
		if (mGameData.sleepTo > System.currentTimeMillis()) {
			sleep = mGameData.sleepTo - System.currentTimeMillis();
		}
		this.mTimerForStateChange = false;
		this.mGameTimer.KillDeskTimer();
		this.mGameTimer.SetDeskTimer((int) sleep); // 立刻跳到游戏结束状态
		return;
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
		}
			break;
		}
	}

	private void game_over() {
		DispatchEvent event = new DispatchEvent();
		event.eventID = MJConstants.MJStateFinish;
		this.mDispatcher.StateDispatch(event);
	}

	@Override
	public void onDeskTimer() {
		// this.logger.info("deal , onDeskTimer is called");
		this.mGameTimer.KillDeskTimer();
		if(mGameData.pause) {//暂停中
			this.checkFinish();
			return;
		}

		if (!this.mTimerForStateChange) {
			int state = this.mGameData.getState();// 当前桌子的状态

			if (state == MJConstants.GAME_TABLE_STATE_PLAYING) // 玩家玩牌中,状态为4
			{
				this.mCardLogic.gameTick(this.mGameData, this.mDesk);
			} else if (state == MJConstants.GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN) // 游戏结束，等待客户端显示game
																					// over界面
			{
				long delta = System.currentTimeMillis() - mGameData.handEndTime;
				// 游戏结束后5秒，给客户端播放排名
//				if (delta >= 4000) {
//					game_over();
//					return;
//				}
				game_over();
				return;
			}
			checkFinish();
			return;
		}

		if (this.mTimerForStateChange) {
			DispatchEvent event = new DispatchEvent();
			event.eventID = MJConstants.MJStateFinish;
			this.mDispatcher.StateDispatch(event);
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
//		this.mCardLogic.playerAutoOper(this.mGameData, this.mDesk, position);
	}
}
