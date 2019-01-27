package com.buding.common.network.codec;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public interface NetWorkListener {

	void msgRead(byte[] msg);

	void channelActive(ChannelHandlerContext ctx) throws Exception;

	void channelInactive(ChannelHandlerContext ctx) throws Exception;

	void exceptionCaught(Throwable cause) throws Exception;
}
