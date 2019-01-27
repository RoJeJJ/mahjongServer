package com.buding.hall.module.vip.service;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.UserRoom;

public interface UserRoomService {
	Result addRoom(UserRoom room) ;
	
	Result upsertRoom(UserRoom room);
	
	UserRoom getMyRoom(int userId, String matchId);
	UserRoom getMyRoom(int userId, long roomId) ;
		
	UserRoom getByRoomCode(String roomCode);
	
	String genUniqCode();
}
