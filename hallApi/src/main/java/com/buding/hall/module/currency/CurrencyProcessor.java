package com.buding.hall.module.currency;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public interface CurrencyProcessor {
	//检查货币是否充足
	Result checkEnough(int userId, int amount);
	//添加货币
	Result add(int userId, int amount, String changeReason);
	//扣除货币
	Result sub(int userId, int amount, String changeReason);
	//改变货币
	Result change(int userId, int amount, String changeReason);
	//获取货币
	TResult<Integer> get(int userId);
	//获取货币类型
	int getCurrentType();
}
