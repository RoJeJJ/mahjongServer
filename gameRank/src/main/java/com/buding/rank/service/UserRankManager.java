package com.buding.rank.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.rank.Rank.RankItem;
import packet.rank.Rank.RankSyn;

import com.buding.common.loop.Looper;
import com.buding.common.loop.ServerLoop;
import com.buding.common.network.session.SessionManager;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.common.constants.RankPointType;
import com.buding.rank.model.RankModel;
import com.buding.rank.processor.RankProcessor;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class UserRankManager implements InitializingBean, Looper {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public Map<Integer, RankProcessor> rankProcessorMap = new HashMap<Integer, RankProcessor>();

	@Autowired
	ConfigManager configManager;
	
	@Autowired
	@Qualifier("ServerBgTaskLoop")
	ServerLoop serverLoop;
	
	@Autowired
	SessionManager hallSessionManager;
	
	public long refreshTime = 0;
		
	public void registerProcessor(int pointType, RankProcessor prossor) {
		rankProcessorMap.put(pointType, prossor);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		serverLoop.register(this);
	}
	
	@Override
	public void loop() throws Exception {
		checkRefresh();
	}

	private void checkRefresh() {
		if(System.currentTimeMillis() - refreshTime >= 60*1000) {
			doRefresh();
		}
	}

	private void doRefresh() {
		logger.info("doRefresh Rank");
		refreshTime = System.currentTimeMillis();
		boolean needPush = false;
		for(RankProcessor prossor : rankProcessorMap.values()) {
			try {
				boolean ret = prossor.refresh();
				if(ret) {
					needPush = true;
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		
		if(needPush) {
//			pushRank();
		}
	}
	
	public void pushRank() {
		for(Object userId : hallSessionManager.getOnlinePlayerIdList()) {
			Integer uid = (Integer)userId;
			RankSyn syn = buldRankSyn(uid);
			logger.info("pushRank2 {}, data: {} " , userId, new JsonFormat().printToString(syn));
			PacketBase.Builder pb = PacketBase.newBuilder();
			pb.setCode(0);
			pb.setPacketType(PacketType.RankSyn);
			pb.setData(syn.toByteString());
			hallSessionManager.write(uid, pb.build());
		}
	}
	
	public RankSyn buldRankSyn(int userId) {
		RankSyn.Builder rank = RankSyn.newBuilder();
		{
			RankProcessor p = rankProcessorMap.get(RankPointType.coin);
			int i = 1;
			for(RankModel tmp : getRank(userId, p.getRankType())) {
				RankItem.Builder item = RankItem.newBuilder();
				item.setRank(i++);
				item.setPlayerId(tmp.playerId);
				item.setPlayerName(tmp.name);
				item.setPlayerHeadImg(tmp.img);
				item.setPoint(tmp.rankPoint);
				rank.addCoinList(item);
			}
		}
		{
			RankProcessor p = rankProcessorMap.get(RankPointType.gameCount);
			int i = 1;
			for(RankModel tmp : getRank(userId, p.getRankType())) {
				RankItem.Builder item = RankItem.newBuilder();
				item.setRank(i++);
				item.setPlayerId(tmp.playerId);
				item.setPlayerName(tmp.name);
				item.setPlayerHeadImg(tmp.img);
				item.setPoint(tmp.rankPoint);
				rank.addGameCountList(item);
			}
		}
		return rank.build();
	}
	
	public List<RankModel> getRank(int userId, int pointType) {
		checkRefresh();
		
		RankProcessor processor = rankProcessorMap.get(pointType);
		if(processor != null) {
			return processor.getRank(userId);
		}
		return new ArrayList<RankModel>();
	}
}
