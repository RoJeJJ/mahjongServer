package com.buding.db.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.UserRoom;
import com.buding.db.model.UserRoomGameTrack;
import com.buding.db.model.UserRoomResult;
import com.buding.db.model.UserRoomResultDetail;
import com.buding.hall.module.vip.dao.UserRoomDao;
import org.springframework.stereotype.Component;

@Component("userRoomDao")
public class UserRoomDaoImpl extends CachedServiceAdpter implements UserRoomDao {
	private final DbService dbService;

	@Autowired
	public UserRoomDaoImpl(DbService dbService) {
		this.dbService = dbService;
	}

	@Override
	public UserRoom getUserRoom(int userId, String matchId) {
		return this.getOne("select * from user_room where owner_id = ? and match_id = ? and room_state = 1 ", UserRoom.class, userId, matchId);
	}
	
	@Override
	public UserRoom getByCode(String roomCode) {
		return this.getOne("select * from user_room where room_code = ? and room_state = 1", UserRoom.class, roomCode);
	}

	@Override
	public void updateUserRoom(UserRoom model) {
		this.put2EntityCache(model);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(model);
		} else {
			this.dbService.submitUpdate2Queue(model);
		}
	}

	@Override
	public long addUserRoom(UserRoom room) {
		this.commonDao.save(room);
		return room.getId();		
	}

	@Override
	public UserRoom get(long roomId) {
		return this.commonDao.get(roomId, UserRoom.class);
	}

	@Override
	public List<UserRoom> getMyRoomList(int playerId) {
		return this.getList("select * from user_room where owner_id = ? and room_state = 1", UserRoom.class, playerId);
	}

	@Override
	public int getMyRoomListCount(int playerId) {
		return this.commonDao.count("select ifnull(count(*), 0) as c from user_room where owner_id = ? and room_state = 1",  playerId);
	}

	@Override
	public boolean isRoomExists(String roomCode) {
		return this.commonDao.count("select ifnull(count(*), 0) as c from user_room where room_code = ?", roomCode) > 0;
	}

	@Override
	public void updateLastActiveTime(String roomCode) {
		UserRoom room = this.getByCode(roomCode);
		if(room != null) {
			room.setLastActiveTime(new Date());
			this.updateUserRoom(room);
		}
	}

	@Override
	public void insertUserRoomResultDetail(UserRoomResultDetail detail) {
		this.commonDao.save(detail);
	}
	
	@Override
	public void insertUserRoomResult(UserRoomResult detail) {
		this.commonDao.save(detail);
	}

	@Override
	public void insertUserRoomGameTrack(UserRoomGameTrack track) {
		this.commonDao.save(track);
	}

	@Override
	public List<UserRoomResult> getUserRoomResultList(long userId) {
		List<UserRoomGameTrack> list = this.getList("select * from user_room_game_track where user_id = ? order by game_time desc limit 50", UserRoomGameTrack.class, userId);
		List<UserRoomResult> retList = new ArrayList<>();
		for(UserRoomGameTrack t : list) {
			UserRoomResult model = this.getOne("select * from user_room_result where room_id = ? order by start_time limit 1", UserRoomResult.class, t.getRoomId());
			retList.add(model);
		}
		return retList;
	}

	@Override
	public List<UserRoomResultDetail> getUserRoomResultDetailList(long roomId) {
		return this.getList("select * from user_room_result_detail where room_id = ? order by start_time desc", UserRoomResultDetail.class, roomId);
	}
	
}
