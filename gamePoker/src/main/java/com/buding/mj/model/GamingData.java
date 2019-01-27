package com.buding.mj.model;

import java.util.List;

/**
 * @author tiny qq_381360993
 * @Description:
 * 管理后台可以设置牌型数据供调试用，该对象用于携带调试数据
 */
public class GamingData {
	public boolean debugMode = true;
	public Card baopai;
	public int curOperPosition;
	public Card newCard;
	public PlayerCard player1;
	public PlayerCard player2;
	public PlayerCard player3;
	public PlayerCard player4;
	
	public List<Card> remainCardlist;
	public List<Card> preSetRemainCard;
}