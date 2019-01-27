package com.buding.hall.module.conf;

import java.util.List;

import com.buding.db.model.MallConf;
import com.buding.db.model.RoomConf;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public interface ConfDao {
	List<RoomConf> getRoomConfList();
	List<MallConf> getMallConfList();
}
