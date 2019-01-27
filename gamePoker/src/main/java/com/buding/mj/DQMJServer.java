package com.buding.mj;

import com.buding.game.GameLogiController;

/**
 * @author tiny qq_381360993
 * @Description: 大庆麻将
 * 
 */
public class DQMJServer extends GameLogiController {
	public DQMJServer() {
		this.m_dispatcher  = new MJStateDispatcher();
	}
}
