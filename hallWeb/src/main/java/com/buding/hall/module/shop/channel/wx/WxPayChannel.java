package com.buding.hall.module.shop.channel.wx;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.ConfirmOrderRequest;
import packet.game.Hall.GenOrderRequest;
import packet.game.Hall.GenOrderResponse;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.UserOrder;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.helper.HttpUtils;
import com.buding.hall.helper.WXUtil;
import com.buding.hall.module.constants.Constants;
import com.buding.hall.module.order.dao.UserOrderDao;
import com.buding.hall.module.shop.OrderStatus;
import com.buding.hall.module.shop.channel.BasePayChannel;
import com.buding.hall.module.shop.channel.Channels;
import com.buding.hall.module.shop.service.ShopService;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
@Component
public class WxPayChannel extends BasePayChannel {

	@Autowired
	UserOrderDao userOrderDao;

	@Autowired
	ShopService shopService;

	@Override
	public TResult<GenOrderResponse> createOrder(int userId, GenOrderRequest req, ProductConfig conf) throws Exception {
		String orderId = shopService.genOrderId(userId + "-wx-");

		if (orderId == null) {
			return TResult.fail1("生成订单失败");
		}

		// 把相关参数传给微信支付
		Map<String, String> map = new HashMap<String, String>();
		map.put("appid", Constants.WX_APP_ID);
		map.put("mch_id", Constants.WX_MCH_ID);
		map.put("nonce_str", getRandomStr());
		map.put("body", conf.desc);
		map.put("out_trade_no", orderId);
//		map.put("total_fee", conf.price.currenceCount*100+""); //单位分
		map.put("total_fee", 1+""); //单位分 TODO
		map.put("spbill_create_ip", "127.0.0.1");
		map.put("notify_url", "http://www.xskdqmj3d.cn/a/wx/order_notify");
		map.put("trade_type", "APP");
		map.put("sign", WXUtil.getSign(map, Constants.WX_MCH_SECRET));

		// 把map转换成xml，并发送到微信支付接口
		String info = WXUtil.map2xml(map);
		System.out.println("unitOrder:" + info);
		String restxml = HttpUtils.post(Constants.ORDER_PAY, info);
		System.out.println(restxml);
		Map<String, String> returnMap = WXUtil.xml2Map(restxml);

		Map<String, String> resultmap = new HashMap<String, String>();
		if (WXUtil.CheckSign(returnMap, Constants.WX_MCH_SECRET)) {
			// 返回的键要相对应，所以要改过来
			resultmap.put("appid", Constants.WX_APP_ID);
			resultmap.put("partnerid", Constants.WX_MCH_ID);
			resultmap.put("prepayid", returnMap.get("prepay_id"));
			resultmap.put("noncestr", returnMap.get("nonce_str"));
			resultmap.put("timestamp", System.currentTimeMillis() / 1000 + "");
			resultmap.put("package", "WXPay");
			resultmap.put("sign", WXUtil.getSign(resultmap, Constants.WX_MCH_SECRET));
			resultmap.put("return_code", "SUCCESS");
			resultmap.put("return_msg", "OK");
		} else {
			resultmap.put("return_code", "FAIL");
			resultmap.put("return_msg", "Wrong Sign");
		}

		String inf = new Gson().toJson(resultmap);

		GenOrderResponse.Builder rsp = GenOrderResponse.newBuilder();
		rsp.setOrderId(orderId);
		rsp.setData(ByteString.copyFrom(inf.getBytes("UTF-8")));

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
		return Result.success();
	}

	public static String getRandomStr() {
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		String str = "QWERTYUIOPASDFGHJKLZXCVBNM0123456789";
		for (int i = 0; i < 32; i++) {
			sb.append(str.charAt(r.nextInt(str.length())));
		}
		return sb.toString();
	}

	@Override
	public int getName() {
		return Channels.wx;
	}

}
