package com.buding.hall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HallMain {
	private static final Logger LOG = LoggerFactory.getLogger(HallMain.class);
	
	public static void main(String[] args) throws Exception {
		new ClassPathXmlApplicationContext("hall-server.xml");
		System.out.println("======================");
		System.out.println("HallServer server start...");
		System.out.println("======================");

		LOG.info("===============================");
		LOG.info(" HallServer server start...............");
		LOG.info("===============================");
		
		System.in.read();
	}
}
