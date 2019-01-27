package com.buding.rank.processor;

import java.util.List;

import com.buding.rank.model.RankModel;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public interface RankProcessor {
	public boolean refresh();	
	public List<RankModel> getRank(int userId);
	public int getRankType();
}
