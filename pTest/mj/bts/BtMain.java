package bts;
import jbt.execution.core.BTExecutorFactory;
import jbt.execution.core.ContextFactory;
import jbt.execution.core.ExecutionTask.Status;
import jbt.execution.core.IBTExecutor;
import jbt.execution.core.IBTLibrary;
import jbt.execution.core.IContext;
import jbt.model.core.ModelTask;
import bts.btlibrary.TerranMarineBTLibrary;

/**
 * @author vinceruan qq_404086388
 * @Description:
 * 
 */
public class BtMain {
	public static void main(String[] args) throws Exception {
		IBTLibrary btLibrary = new TerranMarineBTLibrary();
		/* Then we create the initial context that the tree will use. */
		IContext context = ContextFactory.createContext(btLibrary);
				
		context.setVariable("userName", "vinceruan");
		context.setVariable("password", "123456");
		context.setVariable("serverIp", "127.0.0.1");
		context.setVariable("serverPort", 5000);
		
		ModelTask terranMarineTree = btLibrary.getBT("Poker");
//		/* Then we create the BT Executor to run the tree. */
		IBTExecutor btExecutor = BTExecutorFactory.createBTExecutor(terranMarineTree, context);
//		/* And finally we run the tree through the BT Executor. */		
		do {
			System.out.println("===============================================");
			btExecutor.tick();
			Thread.sleep(1000);
		} while (btExecutor.getStatus() == Status.RUNNING);
		System.exit(0);
	}
}
