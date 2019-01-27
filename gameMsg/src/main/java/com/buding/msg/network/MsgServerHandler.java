package com.buding.msg.network;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

@Sharable
public class MsgServerHandler extends SimpleChannelInboundHandler<byte[]> {
	private Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	MsgSessionManager sessionManager;

	@Autowired
	MsgCmdProc msgProc;

	/**
	 * 
	 */
	private static final AttributeKey<MsgSession> SESSION = AttributeKey.newInstance("NettyHandler.MsgSessionKey");

	@Override
	public void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
		// LOG.info("收到消息");
		PacketBase packet = PacketBase.parseFrom(msg);
		MsgSession session = (MsgSession) ctx.attr(SESSION).get();

		if (LOG.isDebugEnabled() && packet.getPacketType() != PacketType.HEARTBEAT) {
			LOG.debug("收到消息:" + packet.getPacketType().toString());
		}

		msgProc.handleMsg(packet, session);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		cleanWhenClosed(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOG.info("channelActive:{}", ctx.channel().remoteAddress());
		initWhenConnected(ctx);
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOG.info("channelInactive:{}", ctx.channel().remoteAddress());
		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof IOException && cause.getMessage() != null && cause.getMessage().equals("远程主机强迫关闭了一个现有的连接")) {
			MsgSession session = (MsgSession) ctx.attr(SESSION).get();
			int playerId = session == null ? -1 : session.userId;
			LOG.error("远程自动关闭连接:{}", playerId);
			return;
		}
		LOG.error("ChannelException:", cause);
	}

	public void initWhenConnected(ChannelHandlerContext ctx) {
		MsgSession session = new MsgSession();
		session.channel = ctx.channel();
		session.initTime = System.currentTimeMillis();
		sessionManager.put2AnonymousList(session);
		ctx.attr(SESSION).set(session);
	}

	public void onSessionClosed(MsgSession session, ChannelHandlerContext ctx) {
		sessionManager.schedule2Remove(session);
	}

	public void cleanWhenClosed(ChannelHandlerContext ctx) {
		MsgSession session = ctx.attr(SESSION).get();
		if (session != null) {
			onSessionClosed(session, ctx);
			ctx.attr(SESSION).set(null);
		}
	}
}
