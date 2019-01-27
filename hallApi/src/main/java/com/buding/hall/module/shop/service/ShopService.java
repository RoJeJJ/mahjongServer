package com.buding.hall.module.shop.service;

import com.buding.common.result.Result;

/**
 * 商店服务处理类,处理购买请求
 * @author Administrator
 *
 */
public interface ShopService {
	
	/**
	 * 购买道具
	 * 
	 * @param playerId
	 * @param id
	 * @return
	 * @throws Exception
	 */
	Result buyItem(int playerId, String id) throws Exception;

	/**
	 * 购买成功回调
	 * 
	 * @param
	 * @param
	 * @throws Exception
	 */
	Result finishOrder(String orderNo, boolean result) throws Exception;
	
	/**
	 * 生成订单号
	 * @param prefix
	 * @return
	 */
	String genOrderId(String prefix);
	
	String processWxPayCallback(String data) throws Exception;
	
	String processAliPayCallback(String data) throws Exception;
}
