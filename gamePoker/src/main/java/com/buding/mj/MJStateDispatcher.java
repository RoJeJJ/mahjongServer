package com.buding.mj;

import com.buding.api.desk.MJDesk;
import com.buding.card.ICardLogic;
import com.buding.game.GameStateDispatcher;
import com.buding.game.events.DispatchEvent;
import com.buding.mj.common.MJCardDealer;
import com.buding.mj.constants.MJConstants;
import com.buding.mj.dqmj.DQMJCardLogic;
import com.buding.mj.states.MJStateDeal;
import com.buding.mj.states.MJStateFinish;
import com.buding.mj.states.MJStateReady;
import com.buding.mj.states.MJStateRun;

@SuppressWarnings("all")
public class MJStateDispatcher extends GameStateDispatcher<MJDesk> {
	//准备阶段
	private MJStateReady mStateReady = new MJStateReady();
	//发牌阶段
	private MJStateDeal mStateDeal = new MJStateDeal();
	//游戏阶段
	private MJStateRun mStateRun = new MJStateRun();
	//结算阶段
	private MJStateFinish mStateFinish = new MJStateFinish();
	
	private MJCardDealer mCardDealer = new MJCardDealer();
	private ICardLogic mCardLogic = new DQMJCardLogic();
	
	
	@Override
	public void StateDispatch(DispatchEvent event) {
		switch(event.eventID){
		
		case MJConstants.MJStateReady:{
			this.goTo(this.mStateReady);
		}
		break;
		
		case MJConstants.MJStateDeal:{
			this.goTo(this.mStateDeal);	
		}
		break;
				
		case MJConstants.MJStateRun:{
			this.goTo(this.mStateRun);
		}
		break;
		
		case MJConstants.MJStateFinish:{
			this.goTo(mStateFinish);
		}
		break;
		
		default:{
			this.logger.error("no state to goto:" + event.eventID);
		}
			break;
		}
	}



	@Override
	public void SetDesk(MJDesk desk) {
		this.mTimerMgr.Init(desk);
		this.mCardLogic.init(this.mGameData, desk);
		this.mCardDealer.Init(this.mGameData,desk,this.mCardLogic);

		this.mStateReady.Init(desk, this.mTimerMgr, this, this.mGameData,this.mCardLogic,this.mCardDealer);
		this.mStateDeal.Init(desk, this.mTimerMgr, this, this.mGameData,this.mCardLogic,this.mCardDealer);
		this.mStateRun.Init(desk, this.mTimerMgr, this, this.mGameData,this.mCardLogic,this.mCardDealer);
		this.mStateFinish.Init(desk, this.mTimerMgr, this, this.mGameData,this.mCardLogic,this.mCardDealer);
		
		////初始状态 
		this.goTo(this.mStateReady);
	}

}
