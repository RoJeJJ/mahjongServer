package com.buding.battle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BattleMain {
	private static final Logger LOG = LoggerFactory.getLogger(BattleMain.class);

	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("battle-server.xml");
		System.out.println("======================");
		System.out.println("BattleServer server start...");
		System.out.println("======================");

		LOG.info("===============================");
		LOG.info(" BattleServer server start...............");
		LOG.info("===============================");
	}
}
