package com.buding.msg.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.msg.network.MsgSession;
import com.buding.msg.network.MsgSessionManager;
import com.google.protobuf.ByteString;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class MsgPushHelper {
	@Autowired
	MsgSessionManager msgSessionManager;
	
	public void pushErrorMsg(MsgSession session, PacketType type, String msg) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(-1);
		pb.setPacketType(type);
		pb.setMsg(msg);
		msgSessionManager.write(session, pb.build().toByteArray());
	}
	
	public void pushPBMsg(int userId, PacketType type, ByteString data) {
		MsgSession session = msgSessionManager.getIoSession(userId);
		if(session != null) {
			pushPBMsg(session, type, data);
		}
	}
	
	public void pushPBMsg(MsgSession session, PacketType type, ByteString data) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(0);
		pb.setPacketType(type);
		if(data != null) {
			pb.setData(data);
		}
		msgSessionManager.write(session, pb.build().toByteArray());
	}
	
	public void pushAuthRsp(MsgSession session, PacketType type) {
		pushPBMsg(session, PacketType.AuthRequest, null);
	}
}
