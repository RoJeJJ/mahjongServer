package com.buding.hall.helper;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.RoomResultResponse;
import packet.game.Hall.VistorRegisterResponse;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.LoginResponse;
import packet.user.User.UserInfoSyn;

import com.buding.db.model.User;
import com.buding.db.model.UserGameOutline;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.type.UserRole;
import com.buding.hall.network.HallSession;
import com.buding.hall.network.HallSessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class HallPushHelper {
	@Autowired
	HallSessionManager hallSessionManager;
	
	@Autowired
	UserDao userDao;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void pushErrorMsg(HallSession session, PacketType type, String msg) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(-1);
		pb.setPacketType(type);
		pb.setMsg(msg);
		hallSessionManager.write(session, pb.build().toByteArray());
	}
	
	public void pushVistorRsp(HallSession session, String username, String passwd) {
		VistorRegisterResponse.Builder vb = VistorRegisterResponse.newBuilder();
		vb.setAccount(username);
		vb.setPassword(passwd);
		pushPBMsg(session, PacketType.VistorRegisterResponse, vb.build().toByteString());
	}
	
	public void pushRoomResultResponse(HallSession session, RoomResultResponse bb) {
		pushPBMsg(session, PacketType.RoomResultResponse, bb.toByteString());
	}
	
	public void pushRegistorRsp(HallSession session, boolean ok, String msg) {
		if(ok) {
			pushPBMsg(session, PacketType.RegisterResponse, null);
		} else {
			pushErrorMsg(session, PacketType.RegisterResponse, msg);
		}
	}
	
	public void pushLoginRsp(HallSession session, User user, String token, String msgServerAddr, String gameServerAddr) {
		LoginResponse.Builder lb = LoginResponse.newBuilder();
		lb.setUserId(user.getId());
		lb.setToken(token);
		lb.setMsgServerAddr(msgServerAddr);
		lb.setGameServerAddr(gameServerAddr);
		LoginResponse msg = lb.build();
		pushPBMsg(session, PacketType.LoginRequest, msg.toByteString());
		logger.info("act=pushLoginRsp;data={}", new JsonFormat().printToString(msg));
	}
	
	public void pushUserInfoSyn(int userId) {
		UserGameOutline outline = userDao.getUserGameOutline(userId);
		HallSession session = hallSessionManager.getIoSession(userId);
		if(session == null) {
			return;
		}
		
		User user = userDao.getUser(userId);
		
		UserInfoSyn.Builder lb = UserInfoSyn.newBuilder();
		lb.setUserId(user.getId());
		lb.setNickName(user.getNickname());
		lb.setCoin(user.getCoin());
		lb.setFanka(user.getFanka());
		lb.setHeadImg(user.getHeadImg());
		lb.setContinueWinCount(outline == null ? 0 : outline.getContinueWinCount());
		lb.setTotalGameCount(outline == null ? 0 : outline.getTotalCount());
		lb.setSex(user.getGender() == null ? 1 : user.getGender());
		SocketAddress remoteAddress = session.getChannel().remoteAddress();		
		if(remoteAddress instanceof InetSocketAddress) {
			lb.setIp(((InetSocketAddress)remoteAddress).getAddress().getHostAddress());	
		} else {
			lb.setIp("未知");
		}
		double winRate = 0;
		if(outline != null && outline.getTotalCount() > 0) {
			winRate = (outline.getWinCount() * 1.0) / outline.getTotalCount();
		}
		lb.setWinRate(winRate);
		
		List<Integer> downcards = new ArrayList<Integer>();
		if(outline != null && outline.getMaxFanDowncards() != null) {
			downcards = new Gson().fromJson(outline.getMaxFanDowncards(), new TypeToken<List<Integer>>(){}.getType());
		}
		lb.addAllDowncard(downcards);
		
		
		List<Integer> handcards = new ArrayList<Integer>();
		if(outline != null && outline.getMaxFanHandcards() != null) {
			handcards = new Gson().fromJson(outline.getMaxFanHandcards(), new TypeToken<List<Integer>>(){}.getType());
		}
		lb.addAllHandcard(handcards);
		
		if(outline != null && outline.getMaxFanDesc() != null) {
			lb.setMaxFanType(outline.getMaxFanDesc());	
		}
		lb.setCreateMultiRoom(false);
		if(user.getRole() != null && (user.getRole() & UserRole.USER_ROLE_AUTH) == UserRole.USER_ROLE_AUTH) {
			lb.setCreateMultiRoom(true);
		}
		
		pushPBMsg(session, PacketType.UserInfoSyn, lb.build().toByteString());
	}
	
	
	public void pushPBMsg(HallSession session, PacketType type, ByteString data) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(0);
		pb.setPacketType(type);
		if(data != null) {
			pb.setData(data);
		}
		hallSessionManager.write(session, pb.build().toByteArray());
	}
	
}
