package com.buding.hall.network.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.RoomConfigModel;
import packet.game.Hall.RoomConfigResponse;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.cache.RedisClient;
import com.buding.db.model.RoomConf;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.conf.ConfDao;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class RoomConfCmd extends HallCmd implements InitializingBean {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	HallPushHelper pushHelper;
	
	@Autowired
	ConfDao confDao;
	
	@Autowired
	RedisClient redisClient;
	
	List<RoomConf> roomList = null;
	

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		roomList = confDao.getRoomConfList();
	}

	@Override
	public void execute(CmdData data) throws Exception {
		Map<String, Integer> onlineMap = new HashMap<String, Integer>();
		String json = redisClient.get("RoomOnlineList");
		if(StringUtils.isNotBlank(json)) {
			onlineMap = new Gson().fromJson(json, new TypeToken<Map<String, Integer>>(){}.getType());
		}
		
		RoomConfigResponse.Builder rp = RoomConfigResponse.newBuilder();
		for(RoomConf conf : roomList) {
			RoomConfigModel.Builder r = RoomConfigModel.newBuilder();
			r.setBaseScore(conf.getBaseScore());
			r.setIcon(conf.getIcon());
			r.setMatchType(conf.getMatchId());
			r.setMaxCoinLimit(conf.getMaxCoinLimit());
			r.setMinCoinLimit(conf.getMinCoinLimit());
			r.setRoomId(conf.getRoomId());
			r.setRoomName(conf.getRoomName());
			r.setRoomType(conf.getRoomType());
			r.setBaseScore(conf.getBaseScore());
			if(conf.getSrvFee() != null) {
				r.setFee(Integer.valueOf(conf.getSrvFee()));	
			}
			
			if(onlineMap.containsKey(conf.getRoomId())) {
				r.setOnlineNum(onlineMap.get(conf.getRoomId()));
			}
			
			rp.addRoomList(r);			
		}
		pushHelper.pushPBMsg(data.session, PacketType.RoomConfigResponse, rp.build().toByteString());
	}

	@Override
	public PacketType getKey() {
		return PacketType.RoomConfigRequest;
	}

}
