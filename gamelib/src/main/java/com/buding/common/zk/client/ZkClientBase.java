package com.buding.common.zk.client;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public class ZkClientBase {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public ZkClient zkClient;
	public String zkServer;
	
	protected void initZkClient(ZkSerializer serializer) {
		zkClient = new ZkClient(zkServer, 10000, 10000, serializer);
		logger.info("connect zkclient ok........");
	}
	
	public void setZkServer(String zkServer) {
		this.zkServer = zkServer;
	}
}
