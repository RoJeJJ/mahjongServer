package com.buding.mj.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.buding.mj.helper.MJHelper;
import com.google.gson.GsonBuilder;

public class BaseMJRule implements MJRule {
	
	@Override
	public MjCheckResult canHu(MJContext ctx) {
		List<Byte> list = ctx.cardsInCard;
		List<Byte> doubleList = MJHelper.getAllDouble(list);

		for (byte dCard : doubleList) {
			List<Byte> temp = new ArrayList<Byte>(list);
			temp.remove((Byte) dCard);
			temp.remove((Byte) dCard);
			MjCheckResult ret = new MjCheckResult();
			if (canChengPai(temp, ret)) {
				return ret;
			}
		}

		return null;
	}

	private int countCard(byte b, List<Byte> list) {
		int i = 0;
		for (byte val : list) {
			if (val == b) {
				i++;
			}
		}
		return i;
	}

	public boolean canChengPai(List<Byte> list, MjCheckResult result) {
		if (list.size() == 0) {
			return true;
		}

		if (list.size() < 3) {
			return false;
		}

		int count = countCard(list.get(0), list);
		if (count > 2) {
			List<Byte> temp = new ArrayList<Byte>(list);
			temp.remove((Byte) list.get(0));
			temp.remove((Byte) list.get(0));
			temp.remove((Byte) list.get(0));
			MjCheckResult ret = new MjCheckResult();
			if (canChengPai(temp, ret)) {
				result.kezis.addAll(ret.kezis);
				result.shunzis.addAll(ret.shunzis);
				return true;
			}
		}

		Byte card1 = (byte) (list.get(0) + 1);
		Byte card2 = (byte) (list.get(0) + 2);
		if (list.contains(card1) && list.contains(card2)) {
			List<Byte> temp = new ArrayList<Byte>(list);
			temp.remove((Byte) list.get(0));
			temp.remove((Byte) card1);
			temp.remove((Byte) card2);
			MjCheckResult ret = new MjCheckResult();
			if (canChengPai(temp, ret)) {
				result.kezis.addAll(ret.kezis);
				result.shunzis.addAll(ret.shunzis);
				return true;
			}
		}

		return false;
	}
	
	public void test(int ...bytes) {
		List<Byte> list = new ArrayList<Byte>();
		for(int b : bytes) {
			list.add((byte)(b & 0xFF));
		}
		MJContext contxt = new MJContext();
		contxt.cardsInCard.addAll(list);
		 MjCheckResult ret = canHu(contxt);
		 System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(ret));
	}
	
	public static void main(String[] args) {
		new BaseMJRule().test(1,1,2,2,2);
	}
}
