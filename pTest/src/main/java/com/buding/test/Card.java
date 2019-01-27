package com.buding.test;

/**
 * @author vinceruan qq_404086388
 * @Description:
 * 
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
