package com.buding.hall.module.vip.dao;

import java.util.List;

import com.buding.db.model.UserRoom;
import com.buding.db.model.UserRoomGameTrack;
import com.buding.db.model.UserRoomResult;
import com.buding.db.model.UserRoomResultDetail;

public interface UserRoomDao {
	UserRoom getUserRoom(int userId, String matchId);
	void updateUserRoom(UserRoom room);
	long addUserRoom(UserRoom room);
	UserRoom get(long roomId);
	UserRoom getByCode(String roomCode);
	boolean isRoomExists(String roomCode);
	List<UserRoom> getMyRoomList(int playerId);
	int getMyRoomListCount(int playerId);
	void updateLastActiveTime(String roomCode);
	
	void insertUserRoomResultDetail(UserRoomResultDetail detail);
	
	void insertUserRoomResult(UserRoomResult detail);
	
	List<UserRoomResult> getUserRoomResultList(long userId);
	
	List<UserRoomResultDetail> getUserRoomResultDetailList(long roomId);
	
	void insertUserRoomGameTrack(UserRoomGameTrack track);
}
