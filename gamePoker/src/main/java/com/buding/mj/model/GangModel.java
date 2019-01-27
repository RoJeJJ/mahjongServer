
package com.buding.mj.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author hbp
 * @Description:
 * 
 */
public class GangModel {
	//打出一张牌就可以支对:Byte待出的牌->可以支对的牌
	public Map<Byte, Set<Integer>> moAndGangMap = new HashMap<Byte, Set<Integer>>();
	
	public boolean canGang() {
		return moAndGangMap.isEmpty() == false;
	}
}
