package com.buding.common.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Netty4Codec extends ByteToMessageCodec<byte[]> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private int HEAD_LENGTH = 4;
	
	public Netty4Codec() {
		
	}
	
	public Netty4Codec(int packLen) {
		HEAD_LENGTH = packLen;
	}
	
	public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
//		logger.info("netmsg reach");
        if (in.readableBytes() < HEAD_LENGTH) {  //这个HEAD_LENGTH是我们用于表示头长度的字节数。  由于上面我们传的是一个int类型的值，所以这里HEAD_LENGTH的值为4.
            return;
        }
        in.markReaderIndex();                  //我们标记一下当前的readIndex的位置
        int dataLength = in.readInt();       // 读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
//        logger.info("packet len : " + dataLength);
        if (dataLength < 0) { // 我们读到的消息体长度为0，这是不应该出现的情况，这里出现这情况，关闭连接。
            ctx.close();
        }
 
        if (in.readableBytes() < dataLength - 4) { //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
            in.resetReaderIndex();
            return;
        }
 
        byte[] body = new byte[dataLength-4];  //  嗯，这时候，我们读到的长度，满足我们的要求了，把传送过来的数据，取出来吧~~
        in.readBytes(body);
        out.add(body);
    }

	@Override
	protected void encode(ChannelHandlerContext arg0, byte[] body, ByteBuf out)
			throws Exception {
		int dataLength = body.length;  //读取消息的长度
        out.writeInt(dataLength+4);  //先将消息长度写入，也就是消息头
        out.writeBytes(body);
	}

}
