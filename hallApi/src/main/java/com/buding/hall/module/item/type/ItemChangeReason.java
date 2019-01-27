package com.buding.hall.module.item.type;

public enum ItemChangeReason {
	UserBuyItem, //商城购买
	TaskFinish, //任务奖励
	RankAward, //排行榜奖励
	Exchange, //兑换
	ENROLL, //开局服务费
	GAME_WIN_LOSE, //每局输赢
	BANK_ASSIST, // 破产救济
	LOGIN_AWARD, //登录奖励
	CREATE_ROOM, //开房销毁
	ADMIN_CHANGE, //后台更改
	ADMIN_MAIL_ATTACH, //邮件附件赠送
	MOVE, //后台转送,
	AGENT_CHARGE, //从代理商充值
	DESTORY_RET, //房间销毁退还
	OTHER,
}
