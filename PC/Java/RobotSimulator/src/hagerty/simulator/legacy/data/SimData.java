package hagerty.simulator.legacy.data;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class SimData {


	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public SimData() {
	}
}