package com.buding.game.events;

import com.buding.api.player.PlayerInfo;
import com.buding.api.player.vo.PlayerGameInfo;

public class PlayerEvent {

	public int eventID = -1;
	public PlayerInfo info = new PlayerInfo();
	public PlayerGameInfo gameInfo = new PlayerGameInfo();
	public int card;
}
