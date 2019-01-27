package main;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * @author vinceruan qq_404086388
 * @Description:
 * 
 */
public class CmdArgs {
	@Option(name = "-s")
	public String server = "114.215.135.213";
	
	@Option(name = "-u")
	public String user = "a2";
	
	@Option(name = "-p")
	public String passwd = "123456";
	
	@Option(name = "-m")
	public int multiUser = 1; 

	@Argument
	public List<String> arguments = new ArrayList<String>();
}
