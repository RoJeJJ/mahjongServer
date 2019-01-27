package com.buding.mj.states;

import com.buding.api.context.GameContext;
import com.buding.game.events.PlatformEvent;

/**
 * @author tiny qq_381360993
 * @Description: 结算状态
 * 
 */
public class MJStateOver extends MJStateCommon {
	GameContext ctx = null;
	boolean skipHuSettle = false;

	@Override
	public void handlePlayerStatusChange(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleReconnectFor(int position) {
		logger.info("act=handleReconnectFor;state=finish;position={}", position);
	}

	@Override
	public void onEnter() {
		
	}

	@Override
	public void onPlatform(PlatformEvent event) {

	}


	@Override
	public void onDeskTimer() {
		
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
