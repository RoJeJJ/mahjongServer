package com.buding.api.context;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class HandResult {
	public static int GAME_RESULT_WIN = 1;
	public static int GAME_RESULT_LOSE = 2;
	public static int GAME_RESULT_EVEN = 3;
	
	public long startTime;
	public long endTime;
	public PlayHandResult[] playDetail = null;
	
	public HandResult(int playerCount) {
		playDetail = new PlayHandResult[playerCount];
	}
}
