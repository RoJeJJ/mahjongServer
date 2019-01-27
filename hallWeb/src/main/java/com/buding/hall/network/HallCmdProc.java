package com.buding.hall.network;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.loop.Looper;
import com.buding.common.loop.ServerLoop;
import com.buding.common.network.command.Cmd;
import com.buding.hall.network.cmd.CmdData;
import com.buding.hall.network.cmd.HallCmdMapper;


/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class HallCmdProc implements Looper, InitializingBean {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	HallCmdMapper hallCmdMapper;
	
	@Autowired
	@Qualifier("HallServerNetMsgLoop")
	ServerLoop serverLoop;
	
	@Autowired
	HallSessionManager hallSessionManager;
		
	BlockingQueue<CmdData> msgQueue = new LinkedBlockingQueue<CmdData>();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		serverLoop.register(this);	
	}

	@Override
	public void loop() throws Exception {
		while(true) {
			CmdData cmdData = msgQueue.take();
			if(cmdData == null) {
				return;
			}
			execute(cmdData);
			logger.warn("msgQueueSize:" + msgQueue.size());
		}
	}

	public void handleMsg(PacketBase packet, HallSession session) throws Exception {
		if(packet.getPacketType() == PacketType.HEARTBEAT) { //心跳包直接忽略
			return;
		}
		if(msgQueue.size() > 5000) {
			PacketBase.Builder pb = PacketBase.newBuilder();
			pb.setCode(-1);
			pb.setPacketType(packet.getPacketType());
			pb.setMsg("大厅服务器忙,请稍后重试");
			hallSessionManager.write(session, pb.build().toByteArray());
			return;
		}
		CmdData cmd = new CmdData(session, packet);
		msgQueue.put(cmd);
		
//		execute(cmd);
	}

	private void execute(CmdData cmdData) throws Exception {
		cmdData.startExecuteTime = System.currentTimeMillis();
		Cmd<PacketType, CmdData> cmd = hallCmdMapper.get(cmdData.packet.getPacketType());
		if(cmd == null) {
			PacketBase.Builder pb = PacketBase.newBuilder();
			pb.setCode(-1);
			pb.setPacketType(cmdData.packet.getPacketType());
			pb.setMsg("命令无法处理");
			hallSessionManager.write(cmdData.session, pb.build().toByteArray());
			return;
		}
		
		cmd.execute(cmdData);
		cmdData.endExecuteTime = System.currentTimeMillis();
		
		if(cmdData.packet.getPacketType() != PacketType.HEARTBEAT) {
			logger.info("type={};wait={};execute={}", cmdData.packet.getPacketType(), cmdData.startExecuteTime - cmdData.startWatingTime, cmdData.endExecuteTime - cmdData.startExecuteTime);	
		}
	}
}
