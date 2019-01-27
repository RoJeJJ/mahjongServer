package com.buding.battle.common.network.session;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.logic.module.common.AwayStatus;
import com.buding.battle.logic.module.common.OnlineStatus;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.battle.logic.network.module.Module;
import com.buding.common.network.session.BaseSession;
import com.buding.common.network.session.SessionStatus;
import com.buding.db.model.User;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class BattleSession extends BaseSession { 
	private Logger logger = LoggerFactory.getLogger(getClass());

	public transient Module<PacketType, PacketBase> currentModule;
	public PlayerInfo player;
	public User user;	
	private transient Game game;//当前所在的游戏
	private transient Match match;//当前所在的赛场
	private transient Room room;//当前所在的房间
	private transient CommonDesk desk;//当前所在的桌子
	private int seatIndex;//当前所在桌子的座位号
	public OnlineStatus onlineStatus = OnlineStatus.ONLINE;
	public AwayStatus awayStatus = AwayStatus.BACK;
	private PlayerStatus status = PlayerStatus.IN_HALL; //当前玩家的状态
	public ConcurrentLinkedHashMap<String, String> recentDeskId = new ConcurrentLinkedHashMap.Builder<String, String>().maximumWeightedCapacity(3).build();
	public List<Integer> debugData;//调试数据
	
	@Override
	public boolean isCanRemove() {
		//已经计划移除&&不在游戏中&&等待时间3分钟已到
		return sessionStatus == SessionStatus.INVALID && status != PlayerStatus.GAMING && System.currentTimeMillis() - planRemoveTime >= 3*60*1000;
	}
	
	public void enterRoom(Room room) {
		if(room != null) {
			this.enterMatch(room.getParent());
		}
		
		this.room = room;		
	}
	
	public void enterMatch(Match match) {
		if(match != null) {
			this.game = match.getParent();
		}
		this.match = match;		
	}
	
	public void enterDesk(CommonDesk desk, int seatIndex) {
		if(desk != null) {
			this.enterRoom((Room)desk.getParent());
		}
		this.seatIndex = seatIndex;
		this.player.position = seatIndex;
		this.desk = desk;
	}
	
	public void leaveDesk() {
		this.desk = null;
		this.seatIndex = -1;
		this.status = PlayerStatus.UNREADY;
		this.player.position = -1;
	}
	
	public void leaveRoom() {
		this.leaveDesk();
		this.room = null;
	}
	
	public void leaveMatch() {
		this.leaveRoom();
		this.match = null;
		this.game = null;
	}

	public Game getGame() {
		return game;
	}

	public Match getMatch() {
		return match;
	}

	public Room getRoom() {
		return room;
	}

	public CommonDesk getDesk() {
		return desk;
	}

	public int getSeatIndex() {
		return seatIndex;
	}
	
	public PlayerStatus getStatus() {
		return status;
	}
	
	public void setStatus(PlayerStatus status, StatusChangeReason reason) {
		logger.info("act=setStatus;userid={};status={};reason={};sessionid={};gameid={};matchid={};roomid={};deskid={};pos={}", userId, status, reason, sessionId, getGameId(), getMatchId(), getRoomId(), getDeskId(), seatIndex);
		this.status = status;
	}
	
//	public void kickout() {
//		if(this.desk != null) {
//			this.desk.kickout(this.userId);
//		}
//		this.leaveDesk();
//		if(this.currentModule == ServiceRepo.gameModule) {
//			this.currentModule = ServiceRepo.matchModule;
//		}
//	}
	
	public String getGameId() {
		return this.game == null ? null : this.game.getId();
	}

	public String getMatchId() {
		return this.match == null ? null : this.match.getId();
	}

	public String getRoomId() {
		return this.room == null ? null : this.room.getRoomId();
	}

	public String getDeskId() {
		return this.desk == null ? null : this.desk.getDeskID() + "";
	}
	
	public boolean isAdmin() {
		return user.getRole() != null && (user.getRole() & 1) == 1;
	}
}
