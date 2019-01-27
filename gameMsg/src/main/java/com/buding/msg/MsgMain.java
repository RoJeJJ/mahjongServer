package com.buding.msg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MsgMain {
	private static final Logger LOG = LoggerFactory.getLogger(MsgMain.class);

	public static void main(String[] args) throws Exception {
		new ClassPathXmlApplicationContext("msg-server.xml");
		System.out.println("======================");
		System.out.println("GameMsg server start...");
		System.out.println("======================");

		LOG.info("===============================");
		LOG.info(" GameMsg server start...............");
		LOG.info("===============================");
		
		System.in.read();
	}
}
