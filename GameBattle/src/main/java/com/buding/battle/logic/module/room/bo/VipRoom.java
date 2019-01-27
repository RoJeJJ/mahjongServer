package com.buding.battle.logic.module.room.bo;

import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.VipDesk;
import com.buding.battle.logic.module.match.Match;
import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.UserRoom;
import com.buding.hall.config.DeskConfig;

public class VipRoom extends RoomImpl {
	public VipRoom(Match parent) {
		super(parent);
	}

	@Override
	public Result playerEnroll(BattleContext ctx) {
		UserRoom desk = (UserRoom)ctx.params.get("vipDesk");
		if(desk == null) {
			return Result.fail("vip房间不存在");
		}
		
		return Result.success();
	}

	@Override
	public synchronized TResult<CommonDesk> applyDesk(BattleContext ctx) throws Exception {
		String deskId = ctx.getDeskId();
		CommonDesk deskIns = this.guard.getDeskById(ctx.deskId);
		
		if(ctx.params.get("vipDesk") == null) {
			logger.error("act=applyDesk;error=noVipDesk;deskId={};roomId={};", ctx.deskId, getRoomId());
			return null;
		}
		
		if(deskIns == null) {
			deskIns = genDesk(deskId,ctx.getWanfa());
			deskIns = guard.tryAddDesk(deskIns);
		}
		if(ctx.session.isAdmin()){
			deskIns.markAsAdminUse();
		}
		if(ctx.session.isAdmin() == false && deskIns.isAdminUse()) {
			logger.error("act=applyDesk;error=noPermit4AdminDesk;userId={};deskId={};roomId={};", ctx.playerId, ctx.deskId, getRoomId());
			return TResult.fail1("不能进入管理桌");
		}
		return TResult.sucess1(deskIns);
	}

	@Override
	public CommonDesk genDesk(String deskId,int wanfa) {
		DeskConfig deskConf = this.getMatchConf().conditionInfo.deskConf;
		VipDesk desk = new VipDesk(this, this, deskConf, deskId, wanfa);
		UserRoom ur = ServiceRepo.userRoomDao.getByCode(deskId);
		if(ur == null) {
			return null;
		}
		
		desk.setDeskOwner(ur.getOwnerId());
		desk.setUserRoom(ur);
		return desk;
	}
	
}
