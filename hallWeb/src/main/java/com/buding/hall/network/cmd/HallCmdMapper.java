package com.buding.hall.network.cmd;

import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketType;

import com.buding.common.network.command.CmdMapper;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class HallCmdMapper extends CmdMapper<PacketType, CmdData> {

}
