package com.buding.hall.module.robot.dao;

import java.util.List;

import com.buding.db.model.RobotSetting;
import org.springframework.stereotype.Component;

public interface RobotDao {
	List<RobotSetting> loadRobotSettingList();
	void update(RobotSetting setting);
	RobotSetting getByMatchId(String matchId);
}
