package com.buding.common.network.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.springframework.beans.factory.InitializingBean;

import com.buding.common.network.codec.NettyServerInitializer;

/**
 * @author tiny qq_381360993
 * @Description:netty服务器
 * 
 */
public class NettyServer implements InitializingBean, Runnable {

	private int port;

	private NettyServerInitializer protocolInitalizer;

	@Override
	public void afterPropertiesSet() throws Exception {
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	public void run() {
		try {
			EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
			EventLoopGroup workerGroup = new NioEventLoopGroup();
			try {
				ServerBootstrap b = new ServerBootstrap(); // (2)
				b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) // (3)
						.childHandler(protocolInitalizer) // (4)
						.option(ChannelOption.SO_BACKLOG, 2048) // (5)
						.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

				System.out.println("NettyServer 启动了:" + port);

				// 绑定端口，开始接收进来的连接
				ChannelFuture f = b.bind(port).sync(); // (7)

				// 等待服务器 socket 关闭 。
				// 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
				f.channel().closeFuture().sync();

			} finally {
				workerGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();

				System.out.println("NettyServer 关闭了:" + port);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public NettyServerInitializer getProtocolInitalizer() {
		return protocolInitalizer;
	}

	public void setProtocolInitalizer(NettyServerInitializer protocolInitalizer) {
		this.protocolInitalizer = protocolInitalizer;
	}
}
