package com.buding.hall.network.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.GenOrderRequest;
import packet.game.Hall.GenOrderResponse;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.result.TResult;
import com.buding.common.token.TokenClient;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.shop.channel.ChannelRepostory;
import com.buding.hall.module.shop.channel.PayChannel;
import com.buding.hall.module.user.helper.UserSecurityHelper;

@Component
public class GenOrderCmd extends HallCmd {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ChannelRepostory channelRepostory;
	
	@Autowired
	protected TokenClient tokenClient;
	
	@Autowired
	protected UserSecurityHelper userSecurityHelper;
	
	@Autowired
	protected HallPushHelper pushHelper;
	
	@Autowired
	ConfigManager configManager;
	
	@Override
	public void execute(CmdData data) throws Exception {
		GenOrderRequest req = GenOrderRequest.parseFrom(data.packet.getData());
		
		int userId = data.session.userId;
		if(userId == 0) {
			pushHelper.pushErrorMsg(data.session, PacketType.GenOrderResponse, "用户未登录");
			return;
		}
		//从 mall_conf 表获取商品信息
		ProductConfig conf = configManager.getItemConf(req.getProductId());
		if (conf == null) {
			logger.info("act=createOrderError;userId={};productId={}", userId, req.getProductId());			
			pushHelper.pushErrorMsg(data.session, PacketType.GenOrderResponse, "商品不存在");
			return;
		}
		
		PayChannel channel = channelRepostory.getChannel(req.getPlatformId());
		if(channel == null) {
			logger.error("PayChannel not found for platform {}", req.getPlatformId());
			pushHelper.pushErrorMsg(data.session, PacketType.GenOrderResponse, "支付平台不存在");
			return;
		}
		TResult<GenOrderResponse> rr = channel.createOrder(userId, req, conf);
		if(rr.isOk()) {
			pushHelper.pushPBMsg(data.session, PacketType.GenOrderResponse, rr.t.toByteString());
		} else {
			pushHelper.pushErrorMsg(data.session, PacketType.GenOrderResponse, rr.msg);
		}
	}

	@Override
	public PacketType getKey() {
		return PacketType.GenOrderRequest;
	}	
}
