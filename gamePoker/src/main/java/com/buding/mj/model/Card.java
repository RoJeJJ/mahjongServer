package com.buding.mj.model;

/**
 * @author tiny qq_381360993
 * @Description:
 * 单张牌
 */
public class Card {
	public String name;
	public int code;
	
	public Card(int code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public String toString() {
		return name+":"+code;
	}
}
