package com.buding.api.context;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class FinalResult {
	public long startTime;
	public long endTime;
	public PlayFinalResult[] playDetail;
	
	public FinalResult(int playerCount) {
		playDetail = new PlayFinalResult[playerCount];
	}
}
