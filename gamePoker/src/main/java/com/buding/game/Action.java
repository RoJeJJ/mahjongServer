package com.buding.game;

import com.buding.mj.helper.MJHelper;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class Action {
	public int position;
	public String name;
	public String type;
	public int code;
	public int card1;
	public int card2;
	public String desc;
	public int seq;
	public int direct = 0;
	
	public Action(int seq, int position, int code, int card1, int card2, String desc, int direct) {
		this.seq = seq;
		this.code = code;
		this.name = MJHelper.getActionName(code);
		this.position = position;
		this.card1 = card1;
		this.card2 = card2;
		this.desc = desc;
		this.direct = direct;
	}
}
