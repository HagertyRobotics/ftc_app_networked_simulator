package hagerty.simulator.legacy.data;
         
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimData {


	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	public SimData() {
	}
}