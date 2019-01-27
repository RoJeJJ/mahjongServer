package com.buding.mj.common;

import java.util.ArrayList;
import java.util.List;

import com.buding.api.desk.MJDesk;
import com.buding.game.GameCardDealer;
import com.buding.mj.constants.MJConstants;

/**
 * TODO 需要重构成通用的,目前跟大庆麻将牌型耦合了。
 * @author Administrator
 *
 */
public class MJCardDealer extends GameCardDealer<MJDesk<byte[]>> {

	@Override
	public void dealCard() {
		//洗牌
		this.washCards();
//		this.testWashCards();
	}

	// 洗牌
	public void washCards() {
		this.mGameData.mDeskCard.reset();

		//1-9 万\条\筒
		List<Byte> cards = new ArrayList<Byte>();
		for (int j = 0; j < 3; j++) {
			for (int i = 1; i <= 9; i++) {
				int ib = (j << MJConstants.MAHJONG_CODE_COLOR_SHIFTS) + i;
				byte b = (byte) (ib & 0xff);
				cards.add(b);
				cards.add(b);
				cards.add(b);
				cards.add(b);
			}
		}

//		东南西北 中发白
		for (int i = 0; i < 4; i++) {
			for (int j = 65; j < 72; j++) {
				byte b = (byte) (j  & 0xff);
				cards.add(b);
			}
		}

		cards = new CardWasher().wash(cards);
		this.mGameData.mDeskCard.cards.addAll(cards);
	}

	public void testWashCards() {
		this.mGameData.mDeskCard.reset();


		List<Byte> cards = new ArrayList<Byte>();
		for (int j = 0; j < 3; j++) {
			for (int i = 1; i <= 9; i++) {
				if (j == 2 && i == 9){
					continue;
				}
				int ib = (j << MJConstants.MAHJONG_CODE_COLOR_SHIFTS) + i;
				byte b = (byte) (ib & 0xff);
				cards.add(b);
				cards.add(b);
				cards.add(b);
				cards.add(b);
			}
		}

//		东南西北 发白
		for (int i = 0; i < 4; i++) {
			for (int j = 65; j < 72; j++) {
				if (j == 69)continue;
				byte b = (byte) (j  & 0xff);
				cards.add(b);
			}
		}

//		cards = new CardWasher().wash(cards);
		byte hongzhong = (byte) (69  & 0xff);
		int ib = (2 << MJConstants.MAHJONG_CODE_COLOR_SHIFTS) + 9;
		byte jiutong = (byte) (ib & 0xff);

		cards.add(0,hongzhong);
		cards.add(0,hongzhong);
		cards.add(0,hongzhong);

		cards.add(0,jiutong);
		cards.add(0,jiutong);
		cards.add(0,jiutong);

		cards.add(27,hongzhong);
		cards.add(28,jiutong);

		this.mGameData.mDeskCard.cards.addAll(cards);
	}

	@Override
	public void dealPublicCard() {
		
	}
}
