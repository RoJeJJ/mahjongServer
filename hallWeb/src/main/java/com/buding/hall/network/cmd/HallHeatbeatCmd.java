package com.buding.hall.network.cmd;

import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketType;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class HallHeatbeatCmd extends HallCmd {
	
	@Override
	public void execute(CmdData data){
		
	}

	@Override
	public PacketType getKey() {
		return PacketType.HEARTBEAT;
	}
}
