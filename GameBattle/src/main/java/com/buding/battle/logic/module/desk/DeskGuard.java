package com.buding.battle.logic.module.desk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.hall.config.DeskConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 负责维护玩家的map信息
 * @author Administrator
 *
 */
public class DeskGuard {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private DeskConfig conf;
	
	//玩家id->座位号
	private Map<Integer, Integer> playerIdMap = new HashMap<Integer, Integer>();
	//座位号->玩家信息
	private Map<Integer, PlayerInfo> seatIndexMap = new TreeMap<Integer, PlayerInfo>();
	//玩家id->时间
	private Map<Integer, Long> sitTimeMap = new HashMap<Integer, Long>();
	
	public DeskGuard(DeskConfig conf) {
		this.conf = conf;
	}
	
	public List<Integer> getplayerIdList() {
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(playerIdMap.keySet());
		return list;
	}
	
	public List<PlayerInfo> getPlayerList() {
		List<PlayerInfo> set = new ArrayList<PlayerInfo>(seatIndexMap.values());
		return set;
	}
	
	//清除玩家信息
	public synchronized PlayerInfo playerExit(int playerId, String leaveType) {
		logger.info("act=playerLeaveSeat;type={};userId={}", leaveType, playerId);
		Integer playerPos = this.playerIdMap.remove((Object)playerId);
		this.sitTimeMap.remove((Object)playerId);
		if(playerPos != null) {
			return this.seatIndexMap.remove((Object)playerPos);
		}
		return null;
	}
	
	public PlayerInfo getPlayerById(int playerId) {
		Integer pos = playerIdMap.get((Object)playerId);
		if(pos != null) {
			return seatIndexMap.get((Object)pos);
		}
		return null;
	}
	
	public long getSitdownTime(int playerId) {
		Long t = sitTimeMap.get((Object)playerId);
		return t == null ? -1 : t;
	}
	
	public PlayerInfo getPlayerByPos(int nPos) {
		return seatIndexMap.get(nPos);
	}
	
	public boolean isEmpty() {
		return seatIndexMap == null || seatIndexMap.isEmpty();
	}
	
	public boolean isFull() {
		return seatIndexMap.size() >= conf.seatSizeUpper;
	}
	
	public boolean isCanStartGame() {
		return seatIndexMap.size() >= conf.seatSizeLower;
	}
	
	public boolean isSatByPlayer(int npos) {
		return seatIndexMap.containsKey(npos);
	}
	
	public synchronized boolean playerSit(PlayerInfo player, int npos) {
		Assert.isTrue(playerIdMap.get(player.playerId) == null);
		seatIndexMap.put(npos, player);
		playerIdMap.put(player.playerId, npos);
		sitTimeMap.put(player.playerId, System.currentTimeMillis());
		return true;
	}
	
	public synchronized void ready4NextGame() {
		for(int playerId : sitTimeMap.keySet()) {
			sitTimeMap.put(playerId, System.currentTimeMillis());
		}
	}
	
	public int getPlayerCount() {
		return playerIdMap.size();
	}

	public Map<Integer, PlayerInfo> getSeatIndexMap() {
		return seatIndexMap;
	}
	
	//随机获取空位
	public synchronized int getEmptySeat(CommonDesk<?> desk) {
		if(isFull()) {
			return -1;
		}
		//管理员顺序分配
		if(desk.isAdminUse()) {
			for(int i = 0; i < conf.seatSizeUpper; i++) {
				if(isSatByPlayer(i) == false) {
					return i;
				}
			}
			return -1;
		}
		//非管理员随机分配
//		return randomGetSeat();
		return seqGetSeat();
	}

	private int seqGetSeat() {
		for(int i = 0; i < conf.seatSizeUpper; i++) {
			if(isSatByPlayer(i) == false) {
				return i;
			}
		}
		return -1;
	}

	private int randomGetSeat() {
		int pos[] = new int[conf.seatSizeUpper];
		for(int i = 0; i < conf.seatSizeUpper; i++) {
			pos[i] = i;
		}
		logger.info(new Gson().toJson(pos));
		int max = conf.seatSizeUpper;
		while(max > 0) {
			int random = (int)(System.currentTimeMillis()%max);
			int position = pos[random];
			if(isSatByPlayer(position) == false) {
				return position;
			}
			int i = pos[max - 1];
			pos[max - 1] = pos[random];
			pos[random] = i;
			
			logger.info(position + " is sat by " + seatIndexMap.get(position).playerId);
			logger.info(position + ":" + new Gson().toJson(pos));
			
			max --;
		}
		throw new RuntimeException("程序出错");
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}
	
}