package com.buding.ptest;

import org.kohsuke.args4j.Option;

/**
 * @author vinceruan qq_404086388
 * @Description:
 * 
 */
public class PTestCmd {
	@Option(name = "-s")
	public String server = "114.215.135.213";
	
	//并发测试 客户端数量
	@Option(name = "-wc")
	public int workerCount = 1;

	//并发测试 游戏次数
	@Option(name = "-gc")
	public int gameCount = 3;	
}
