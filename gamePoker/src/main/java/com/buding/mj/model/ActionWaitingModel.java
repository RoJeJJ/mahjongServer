package com.buding.mj.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buding.mj.common.CardCombo;

/**
 * 该模型保存当前操作玩家能进行的操作
 * @author Administrator
 *
 */
public class ActionWaitingModel {
	//操作码
	public int opertaion = 0;
	public int playerTableIndex = 0;

	//可碰牌，2张牌byte拼成一个int, 和targetCard一起组合刻子
	public int peng_card_value = 0;

	//可吃牌，2张牌byte拼成一个int, 和targetCard一起组合顺子
//	public int chi_card_value = 0;

	// 吃听标位符
//	public int chi_flag = 0;
	
	public List<CardCombo> chiCombos = new ArrayList<CardCombo>();

	// 吃碰杠的目标牌,即别的玩家打出的会导致你可以吃碰杠的牌
	public byte targetCard = 0;
	
	//刚摸到的牌
	public byte newCard = 0;
	//
	// 是否是卡胡，胡卡张
	public boolean isKaHu = false;

	// 打出哪些牌可以听
	public List<Byte> tingList = new ArrayList<Byte>();

	public ChuTingModel chuAndTingModel = null;
	
	public ChuTingModel pengChuAndTingModel = null;

	public GangModel gangAndMoModel = null;

	//3,4,5
	public Map<CardCombo, ChuTingModel> chiChuAndTingModelMap = new HashMap<CardCombo, ChuTingModel>();
	
	public List<Integer> zhiDuiTingList = new ArrayList<Integer>();

	public int mustZhiDui = 0;//是否必须支队,0为非必须,1为必须


	public ActionWaitingModel() {
	}

	// 玩家吃牌参数是否正确
	public CardCombo chi_check(int value1, int value2) {
		if (value1 == 0 || value2 == 0 || value2 == value1)
			return null;
		//
		int delta = Math.abs(value2 - value1);
		if (delta == 0 || delta > 2)
			return null;

		for(CardCombo combo : chiCombos) {
			if((combo.card1 == value1 || combo.card2 == value1) && (combo.card1 == value2 || combo.card2 == value2)) {
				return combo;
			}
		}

		return null;
	}
}
