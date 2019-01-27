package com.buding.hall.module.msg.dao;

import java.util.Date;
import java.util.List;

import com.buding.db.model.ActNotice;
import com.buding.db.model.Marquee;
import com.buding.db.model.Msg;
import com.buding.db.model.UserMsg;
import org.springframework.stereotype.Component;

public interface MsgDao {
	UserMsg getUserMsg(int userId, long msgId);
	List<UserMsg> getUserMsg(int userId);
	List<ActNotice> getActAndNoticeList();
	void update(UserMsg msg);
	void insert(UserMsg msg);
	long insertMsg(Msg msg);
	void update(Msg msg);
	Msg getMsg(long msgId);
	List<Msg> listShareMsg(Date date);
	
	List<Marquee> getMarqueeList();
	Marquee getMarquee(long id);
	long insertMarquee(Marquee msg);
}
