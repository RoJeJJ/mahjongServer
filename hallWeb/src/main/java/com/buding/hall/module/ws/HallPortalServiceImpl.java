package com.buding.hall.module.ws;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.cluster.model.ServerModel;
import com.buding.common.cluster.service.ICluster;
import com.buding.common.result.Result;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.GameLog;
import com.buding.db.model.User;
import com.buding.db.model.UserRoom;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.cluster.model.ServerState;
import com.buding.hall.module.cluster.model.UserGaming;
import com.buding.hall.module.cluster.service.ClusterService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.rank.service.RankService;
import com.buding.hall.module.shop.service.ShopService;
import com.buding.hall.module.task.vo.GamePlayingVo;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.service.UserRoomService;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class HallPortalServiceImpl implements HallPortalService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserDao userDao;
	
	@Autowired
	ClusterService clusterService;
	
	@Autowired
	UserRoomService userRoomService;
	
	@Autowired
	RankService rankService;
	
	@Autowired
	ConfigManager configManager;
	
	@Autowired
	ServerConfig serverConfig;
	
	@Autowired
	ICluster cluster;
	
	@Autowired
	ShopService shopService;
	
//	@Override
//	public void stopService(String instanceId) {
//		cluster.stop(instanceId);
//	}
//	
//	@Override
//	public void startService(String instanceId) {
//		cluster.start(instanceId);
//	}
	
	@Override
	public void updateUserGaming(int userId, String gameId, String serverInstanceId) {
		clusterService.updateUserGaming(userId, gameId, serverInstanceId);	
	}

	@Override
	public void removeUserGaming(int userId, String gameId, String serverInstanceId) {
		clusterService.removeUserGaming(userId, gameId, serverInstanceId);
	}

	@Override
	public void updateServerStatus(ServerState server) {
		clusterService.updateServerStatus(server);
	}

	@Override
	public UserGaming getUserGaming(int userId) {
		return clusterService.getUserGaming(userId);
	}

	@Override
	public User getUser(int userId) {
		return userService.getUser(userId);
	}

	@Override
	public User initUser() {
		return userService.initUser();
	}

	@Override
	public Result changeCoin(int userId, int coin, boolean check, ItemChangeReason reason) {
		return userService.changeCoin(userId, coin, check, reason);
	}

	@Override
	public User addGameResult(GamePlayingVo gameResult) {
		return userService.addGameResult(gameResult);
	}

	@Override
	public void addGameLog(GameLog log) {
		userDao.addGameLog(log);
	}

	@Override
	public Result hasEnoughCurrency(int userId, int currenceType, int count) {
		return userService.hasEnoughCurrency(userId, currenceType, count);
	}

	@Override
	public Result hasEnoughItem(int userId, String itemId, int count) {
		return userService.hasEnoughItem(userId, itemId, count);
	}

	@Override
	public Result changeFangka(int userId, int coin, boolean check, ItemChangeReason reason) {
		return userService.changeFangka(userId, coin, check, reason);
	}

	@Override
	public boolean isUserOnline(int userId) {
		return userService.isUserOnline(userId);
	}

	@Override
	public Result auth(int userId) {
		return userService.auth(userId);
	}

	@Override
	public Result cancelAuth(int userId) {
		return userService.cancelAuth(userId);
	}

	@Override
	public boolean isCanReceiveBankAssist(int userId) {
		return userService.isCanReceiveBankAssist(userId);
	}

	@Override
	public Result addRoom(UserRoom room) {
		return userRoomService.addRoom(room);
	}

	@Override
	public Result upsertRoom(UserRoom room) {
		return userRoomService.upsertRoom(room);
	}

	@Override
	public UserRoom getMyRoom(int userId, String matchId) {
		return userRoomService.getMyRoom(userId, matchId);
	}

	@Override
	public UserRoom getMyRoom(int userId, long roomId) {
		return userRoomService.getMyRoom(userId, roomId);
	}

	@Override
	public UserRoom getByRoomCode(String roomCode) {
		return userRoomService.getByRoomCode(roomCode);
	}

	@Override
	public String genRoomUniqCode() {
		return userRoomService.genUniqCode();
	}

	@Override
	public void addUserRankPoint(int userId, String userName, String gameId, int rankPoint, int rankType, Date date) throws Exception {
		rankService.addUserRankPoint(userId, userName, gameId, rankPoint, rankType, date);
	}

	@Override
	public Result reloadMallConf(String server) {
		if("all".equals(server) || serverConfig.instanceId.equals(server)) {
			configManager.initShopConfig();
		}
		return Result.success();
	}

	@Override
	public Result resetPasswd(int userId, String passwd) {
		return userService.resetPasswd(userId, passwd);
	}

	@Override
	public Result changeUserType(int userId, int type) {
		return userService.changeUserType(userId, type);
	}

	@Override
	public List<ServerModel> getAllServer(String serverType) {
		return cluster.getAllServer(serverType);
	}

	@Override
	public void stopService(String instanceId) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			cluster.stop(instanceId);	
		}		
	}
	
	@Override
	public void startService(String instanceId) {
		if("all".equals(instanceId) || serverConfig.instanceId.equals(instanceId)) {
			cluster.start(instanceId);
		}
	}

	@Override
	public String onWxPayCallback(String xml) throws Exception {
		return shopService.processWxPayCallback(xml);
	}

	@Override
	public String onAliPayCallback(String xml) throws Exception {
		try {
			return shopService.processAliPayCallback(xml);
		} catch (Exception e) {
			logger.error("", e);
			return "get exception";
		}
	}
	
}
