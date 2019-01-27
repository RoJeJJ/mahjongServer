package com.buding.common.token;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class TokenClient {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Map<Integer, String> tokenMap = new HashMap<>();
	
	@Autowired
	TokenServer tokenServer;
	
	public boolean verifyToken(int userId, String token) {
		logger.info("check user {} token", userId, token);
		
		if(StringUtils.isBlank(token)) {
			return false;
		}
		
		if(token.equals(tokenMap.get(userId))) {
			return true;
		}
		
		boolean ret = tokenServer.verifyToken(userId, token);
		if(ret) {
			tokenMap.put(userId, token);
		}
		return ret;
	}
}
