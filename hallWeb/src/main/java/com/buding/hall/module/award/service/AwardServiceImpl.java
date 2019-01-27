package com.buding.hall.module.award.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.common.result.Result;
import com.buding.db.model.Award;
import com.buding.db.model.UserAward;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.award.dao.AwardDao;
import com.buding.hall.module.item.service.ItemService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AwardServiceImpl implements AwardService {
	@Autowired
	AwardDao awardDao;
	
	@Autowired
	ItemService itemService;

	@Override
	public long addAward2User(int userId, Award award) {
		long id = this.awardDao.insert(award);
		award.setId(id);
		addAward2User(userId, id);
		return id;
	}

	@Override
	public void addAward2User(int userId, long awardId) {
		UserAward ua = new UserAward();
		ua.setAwardId(awardId);
		ua.setReceived(false);
		ua.setUserId(userId);
		ua.setMtime(new Date());
		this.awardDao.insertUserAward(ua);
	}

	@Override
	public long addAward(Award award) {
		return this.awardDao.insert(award);
	}

	@Override
	public Result receiveAward(int userId, long awardId) {
		Award award = this.awardDao.getAward(awardId);
		if(award == null) {
			return Result.fail("礼品不存在");
		}
		if(award.getAwardType() == 1 && award.getReceiverId() != userId) {
			return Result.fail("礼品不属于你");
		}
		
		UserAward ua = this.awardDao.get(awardId, userId);
		if(ua != null && ua.getReceived()) {
			return Result.fail("重复领取");
		}
		
		if(award.getInvalidTime().before(new Date())) {
			return Result.fail("礼品已经过期");
		}
		
		if(ua == null) {
			ua = new UserAward();
			ua.setAwardId(awardId);
			ua.setMtime(new Date());
			ua.setReceived(true);
			ua.setUserId(userId);
			this.awardDao.insertUserAward(ua);
		} else {
			ua.setReceived(true);
			this.awardDao.updateUserAward(ua);
		}
				
		List<ItemPkg> list = new Gson().fromJson(award.getItems(), new TypeToken<List<ItemPkg>>(){}.getType());
		ItemChangeReason reason = award.getAwardReason() == null ? ItemChangeReason.OTHER : ItemChangeReason.valueOf(award.getAwardReason());
		this.itemService.addItem(userId, reason, award.getSrcSystem() + "_" + award.getAwardNote()+"_"+award.getId(), list);
		
		return Result.success();
	}

}
