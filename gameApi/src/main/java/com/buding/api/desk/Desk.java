package com.buding.api.desk;

import java.util.List;

import com.buding.api.context.GameContext;
import com.buding.api.player.PlayerInfo;

public interface Desk<MsgType> {
	
   PlayerInfo getDeskPlayer(int nDeskPos); 

   List<PlayerInfo> getPlayers();
   
   int getPlayerCount();
	
  void sendMsg2Player(int position, MsgType content);
   
   void sendMsg2Desk(MsgType content);
   void sendMsg2DeskExceptPosition(MsgType content, int excludePosition);
   
   int setTimer(long mills);
   
   void killTimer(int timerID);
   
   void setDeskInValid();
   
   String getDeskID();
   
   int getBasePoint();//低分，底注
		
   void onGameOver(); //游戏结束
   
   void finalSettle(GameContext context); //总结算
   
   void handSettle(GameContext context); //一局结算
   
   boolean hasNextGame(GameContext context); //是否还有下一场
   
   void startNextGame(GameContext context); //重置，准备下一场
   
   void ready4NextGame(GameContext context); //开始准备好开始下一场
   
   double getFee();
      
   String getReplyData();
   
   List<PlayerInfo> loopGetPlayer(int pos, int count, int includePos); //includePos: 0不包含pos玩家, 1:包含放在末尾 2包含放在开头
   
   List<Integer> getDebugData(int pos);
   
   void sendErrorMsg(int position, String msg);
   
   void log(LogLevel level, String msg, int position);
   
   int getPlayerActionTimeOut(int act);//获取玩家超时操作时间配置
   
   int getDeskOwner();
   
   void onPlayerHangup(int position);
   
   void onPlayerCancelHangup(int position);

   byte getGunBaoCard(byte bao);

 void sendMsgToGoodPlayer(int playerId, byte[] bytes);

 void sendMsgChangeCardSyn(int playerId, byte[] bytes);
}
