package com.buding.ptest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.retry.RetryResult;
import com.buding.test.Looper;


/**
 * @author vinceruan qq_404086388
 * @Description:
 * 
 */
public class PLooper extends Looper {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	public PLooper(String serverIp, int serverPort, String userName, String password) {
		super(serverIp, serverPort, userName, password);
		shouldInitCmd = false;
	}


	@Override
	public RetryResult loop() {
		RetryResult r = super.loop();
		
		if(player != null && player.getGameServerProxy() != null) {
			player.getGameServerProxy().tick();
		}
		
		if(player != null && player.getHallServerProxy() != null) {
			player.getHallServerProxy().tick();
		}
		
		if(player != null && player.getMsgServerProxy() != null) {
			player.getMsgServerProxy().tick();
		}
		return r;
	}
	
}
