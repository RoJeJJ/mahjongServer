package com.buding.mj.common;

import java.util.ArrayList;
import java.util.List;

public class MJContext {
	public List<Byte> cardsInCard = new ArrayList<Byte>();
	public List<Integer> cardsDown = new ArrayList<Integer>();
	public byte tingCard = 0;
	public byte tingType = 0;// 1 单调听 2 听刻子 3 听顺子
}
