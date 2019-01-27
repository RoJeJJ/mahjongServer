package com.buding.mj.common;

import java.util.List;


public interface MJRule {
	public MjCheckResult canHu(MJContext context);
	public boolean canChengPai(List<Byte> list, MjCheckResult result);
}
