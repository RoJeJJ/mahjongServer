package com.buding.game;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.api.desk.Desk;

public class GameTimerMgr {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Desk mDesk = null;

	public void Init(Desk desk){
		this.mDesk = desk;
	}


	private int mDeskTimerID = -1;

	/*
	 * 启动桌子定时器, 以毫秒为单位
	 */
	public void SetDeskTimer(int elapse){

		if( -1 != this.mDeskTimerID ){
			this.logger.error("please call KillDeskTimer");
			return;
		}

		this.mDeskTimerID = this.mDesk.setTimer(elapse);
	}

	public void KillDeskTimer(){

		if( ! this.IsDeskTimerAlive() ){
			return;
		}

		this.mDesk.killTimer(mDeskTimerID);
		this.mDeskTimerID = -1;
	}

	/*
	 * 返回-1表示定时器已经被杀掉
	 */
	public int GetDeskTimerID(){
		return this.mDeskTimerID;
	}

	/*
	 * 桌子定时器是否还活着
	 */
	public boolean IsDeskTimerAlive(){

		return ( -1 != this.mDeskTimerID );
	}






	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 一个玩家最多一个定时器
	 */
	private class PlayerTimer{
		//int mPlayerTimerID = -1; ///上面开发者定义的timerID;
		int mDeskTimerID = -1; ////desk那边返回的对应的timerID;
	}

	private Map<Integer,PlayerTimer> mPlayerTimersMap = new HashMap<Integer,PlayerTimer>();

	
	public int getPlayerTimerID(int position){
		if(position >= GameConstants.MyGame_Max_Players_Count){
			this.logger.error("position is too big " + position);
			return -1;
		}
		
		PlayerTimer timer = this.mPlayerTimersMap.get(position);
		if( null != timer ){ 
			return	timer.mDeskTimerID;
		}
		
		return -1;
	}

	public void setPlayerTimer(int position, int elapse){

		if(position >= GameConstants.MyGame_Max_Players_Count){
			this.logger.error("position is too big " + position);
			return;
		}

		PlayerTimer timer = this.mPlayerTimersMap.get(position);
		if( null != timer ){

			if( -1 != timer.mDeskTimerID){
				this.mDesk.killTimer(timer.mDeskTimerID);
				timer.mDeskTimerID = -1;
			}

		}else{

			timer = new PlayerTimer();
			this.mPlayerTimersMap.put(position, timer);
		}


		timer.mDeskTimerID = this.mDesk.setTimer(elapse);

		this.logger.info("set one timer for player " + position + " elapse " + elapse);

	}


	public void killPlayerTimer(int position){

		if(position >= GameConstants.MyGame_Max_Players_Count){
			this.logger.error("position is too big " + position);
			return;
		}

		PlayerTimer timer = this.mPlayerTimersMap.get(position);
		if( null != timer ){

			if( -1 != timer.mDeskTimerID){
				this.mDesk.killTimer(timer.mDeskTimerID);
				timer.mDeskTimerID = -1;
			}

		}else{
			this.logger.error("no timer to kill for " + position);
		}
	}


	public void killAllPlayerTimers(){

		for(PlayerTimer value : this.mPlayerTimersMap.values()){
			if(-1 != value.mDeskTimerID){
				this.mDesk.killTimer(value.mDeskTimerID);
				value.mDeskTimerID = -1;
			}
		}

		this.mPlayerTimersMap.clear();
	}
}
