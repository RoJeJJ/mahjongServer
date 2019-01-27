
package com.buding.mj.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class ZhiduiModel {
	//打出一张牌就可以支对:Byte待出的牌->可以支对的牌
	public Map<Byte, Set<Integer>> chuAndZhiduiMap = new HashMap<Byte, Set<Integer>>();
	
	public boolean canZhidui() {
		return chuAndZhiduiMap.isEmpty() == false;
	}
}
