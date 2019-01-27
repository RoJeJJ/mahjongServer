package com.buding.game;

import com.buding.api.context.FinalResult;
import com.buding.api.context.HandResult;
import com.buding.api.context.PlayFinalResult;
import com.buding.api.context.PlayHandResult;
import com.buding.api.player.PlayerInfo;


public class GameDataBase {

	public GameDataBase() {

		for(int index = 0; index < GameConstants.MyGame_Max_Players_Count; index ++){
			
			this.mPlayerFinalResult.playDetail[index] = new PlayFinalResult();
			
			this.mPlayerFinalResult.playDetail[index].pos = index;
			
			this.mPlayerFinalResult.startTime = System.currentTimeMillis();
			
			if(this.mPlayers[index] != null) {
				this.mPlayerFinalResult.playDetail[index].playerId = this.mPlayers[index].playerId;
				this.mPlayerFinalResult.playDetail[index].playerName = this.mPlayers[index].name;
				this.mPlayerFinalResult.playDetail[index].headImg = this.mPlayers[index].headImg;
			}
		}
	}
	
	public void resetOpCancel() {
		for(GamePacket.MyGame_Player_Cancel op : this.mOpCancel) {
			op.cancelOp = 0;
		}
	}
	
	/*
	 * 所有数据重置
	 */
	protected void Reset(){
		
		////公共包
//		this.mPublic.mbankerPos = GameConstants.MyGame_Invalid_Value;//庄家位置
		this.mPublic.mBaoCard = GameConstants.MyGame_Invalid_Value; //宝牌
//		this.mPublic.cardLeft = GameConstants.MyGame_Invalid_Value; //牌堆剩余牌数

		this.mDeskCard = new GamePacket.MyGame_DeskCard();
		
		this.recorder = new GameRecorder();
		
		this.mActor = new GamePacket.MyGame_Actor();
		
		this.mPlayerHandResult.startTime = System.currentTimeMillis();
		
		this.playerChangeBao = -1;
						
		for(int index = 0; index < GameConstants.MyGame_Max_Players_Count; index ++){

			this.mPlayerCards[index] = new GamePacket.MyGame_Player_Cards();

//			this.mFinish[index] = new GamePacket.MyGame_Finish();
			
			this.mTingCards[index] = new GamePacket.MyGame_Player_Ting_Cards();
			
			this.mOpCancel[index] = new GamePacket.MyGame_Player_Cancel();
			
			this.mPlayerHandResult.playDetail[index] = new PlayHandResult();
			
			if(this.mPlayerAction[index] == null) {
				this.mPlayerAction[index] = new GamePacket.MyGame_Player_Action();	
			} else {
				this.mPlayerAction[index].reset();
			}
			
			
			this.mPlayerHandResult.playDetail[index].pos = index;
			this.mPlayerFinalResult.playDetail[index].pos = index;
			
			if(this.mPlayers[index] != null) {
				this.mPlayerHandResult.playDetail[index].playerId = this.mPlayers[index].playerId;
				this.mPlayerHandResult.playDetail[index].playerName = this.mPlayers[index].name;
				this.mPlayerHandResult.playDetail[index].result = PlayHandResult.GAME_RESULT_EVEN;
				this.mPlayerFinalResult.playDetail[index].playerId = this.mPlayers[index].playerId;
				this.mPlayerFinalResult.playDetail[index].playerName = this.mPlayers[index].name;
				this.mPlayerFinalResult.playDetail[index].headImg = this.mPlayers[index].headImg;
				
				if(this.mPlayers[index].isRobot()) {
					this.mPlayerAction[index].autoOperation = 1;//自动托管
				}
			}			
		}
	}


	////游戏参数,暂时没有用到
	public GameParam mGameParam = new GameParam();
	
	//负责换宝的玩家, 第一个上听的玩家负责换宝
	public int playerChangeBao = -1;
	
	//调试模式
	public boolean debugMode = false;
	
	//是否解散中状态
	public boolean dismissing = false;
	
	//是否暂停状态.
	public boolean pause = false;

	///行动者
	public GamePacket.MyGame_Actor mActor = null; //new GamePacket.MyGame_Actor();

	///玩家相关牌
	public GamePacket.MyGame_Player_Cards mPlayerCards[] = new GamePacket.MyGame_Player_Cards[GameConstants.MyGame_Max_Players_Count];
	
	///结算
//	public GamePacket.MyGame_Finish mFinish[] = new GamePacket.MyGame_Finish[GameConstants.MyGame_Max_Players_Count];

    //胡牌信息
	public GamePacket.MyGame_Player_Hu mGameHu = new GamePacket.MyGame_Player_Hu();; 
	
	//公共信息
	public GamePacket.MyGame_PublicInfo mPublic = new GamePacket.MyGame_PublicInfo();
	
	//桌上的牌
	public GamePacket.MyGame_DeskCard mDeskCard = null; //new GamePacket.MyGame_DeskCard();
	
	//玩家听的牌
	public GamePacket.MyGame_Player_Ting_Cards mTingCards[] = new GamePacket.MyGame_Player_Ting_Cards[GameConstants.MyGame_Max_Players_Count];

	//玩家杠牌次数
	public GamePacket.MyGame_Player_Gang_Cards gangCards = new GamePacket.MyGame_Player_Gang_Cards();
	
	//玩家取消操作
	public GamePacket.MyGame_Player_Cancel mOpCancel[] = new GamePacket.MyGame_Player_Cancel[GameConstants.MyGame_Max_Players_Count];
	
	//玩家行动
	public GamePacket.MyGame_Player_Action mPlayerAction[] = new GamePacket.MyGame_Player_Action[GameConstants.MyGame_Max_Players_Count];
	
	//玩家累计输赢
	public FinalResult mPlayerFinalResult = new FinalResult(GameConstants.MyGame_Max_Players_Count);
	
	//玩家当前局输赢
	public HandResult mPlayerHandResult = new HandResult(GameConstants.MyGame_Max_Players_Count);
	
	//玩家信息
	public PlayerInfo[] mPlayers = new PlayerInfo[GameConstants.MyGame_Max_Players_Count];
	
	//游戏记录
	public GameRecorder recorder = new GameRecorder();
}
