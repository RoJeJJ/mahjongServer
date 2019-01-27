package com.buding.hall.network.cmd;

import org.springframework.beans.factory.annotation.Autowired;

import packet.msgbase.MsgBase.PacketType;

import com.buding.common.network.command.BaseCmd;
import com.buding.common.network.command.CmdMapper;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public abstract class HallCmd extends BaseCmd<PacketType, CmdData>{
	@Autowired
	HallCmdMapper cmdMapper;

	@Override
	public CmdMapper<PacketType, CmdData> getCmdMapper() {
		return cmdMapper;
	}
}
