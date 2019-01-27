package com.buding.hall.module.shop.channel.test;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.ConfirmOrderRequest;
import packet.game.Hall.GenOrderRequest;
import packet.game.Hall.GenOrderResponse;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.UserOrder;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.module.order.dao.UserOrderDao;
import com.buding.hall.module.shop.OrderStatus;
import com.buding.hall.module.shop.channel.BasePayChannel;
import com.buding.hall.module.shop.channel.Channels;
import com.buding.hall.module.shop.service.ShopService;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class TestPayChannel extends BasePayChannel {

	@Autowired
	UserOrderDao userOrderDao;
	
	@Autowired
	ShopService shopService;
	
	@Override
	public TResult<GenOrderResponse> createOrder(int userId, GenOrderRequest req, ProductConfig conf) throws Exception {
		String orderId = shopService.genOrderId(userId + "-Test-");
		
		if(orderId == null) {
			return TResult.fail1("生成订单失败");
		}
		
		GenOrderResponse.Builder rsp = GenOrderResponse.newBuilder();
		rsp.setOrderId(orderId);
		
		UserOrder order = new UserOrder();
		order.setCtime(new Date());
		order.setMtime(new Date());
		order.setOrderId(orderId);
		order.setOrderStatus(OrderStatus.WAITING);
		order.setProductId(req.getProductId());
		order.setUserId(userId);
		
		userOrderDao.insert(order);
		
		rsp.setPlatformId(req.getPlatformId());
		return TResult.sucess1(rsp.build());
	}

	@Override
	public Result confirmOrder(int userId, ConfirmOrderRequest req) throws Exception {
		return shopService.finishOrder(req.getOrderId(), true);
	}

	@Override
	public int getName() {
		return Channels.test;
	}
	
}
