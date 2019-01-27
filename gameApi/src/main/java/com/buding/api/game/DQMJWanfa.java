package com.buding.api.game;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public interface DQMJWanfa {
	int KAI_PAI_ZHA = 0X1;
	int ZHIDUI = 0X2;
	int JIA37 = 0x4;
	int DANDIAO = 0x8;
	int GUA_DA_FENG = 0X10;
	int HONGZHONG_MTF = 0X20;
	int DAI_LOU = 0x40;
	int WUJIA_BUHU = 0X80;
	int DUI_DAO = 0X100;
	int GUN_BAO = 0X200;
	int ALL = KAI_PAI_ZHA | JIA37 | ZHIDUI | DANDIAO | GUA_DA_FENG | HONGZHONG_MTF | DAI_LOU | WUJIA_BUHU | DUI_DAO | GUN_BAO;
}
