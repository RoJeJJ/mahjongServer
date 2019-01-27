
package com.buding.mj.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class ChuTingModel {
	//打出一张牌就可以听:Byte待出的牌->可以听哪些牌
	public Map<Byte, Set<Byte>> chuAndTingMap = new HashMap<Byte, Set<Byte>>();
	
	public boolean canTing() {
		return chuAndTingMap.isEmpty() == false;
	}
}
