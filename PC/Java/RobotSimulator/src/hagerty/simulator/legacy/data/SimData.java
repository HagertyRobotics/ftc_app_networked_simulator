package hagerty.simulator.legacy.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.VBox;
import org.ftccommunity.simulator.net.protocol.SimulatorData;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class SimData {

	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	protected StringProperty simDataName = null;
    private SimulatorData.Type.Types mType=null;

	public SimData(SimulatorData.Type.Types type) {
		mType=type;
	}

    // Do subclass level processing in this method
    protected abstract void construct();

	public String getSimDataName() {
		return simDataName.getValue();
	}

	public void setSimDataName(String name) {
		this.simDataName = new SimpleStringProperty(name);
	}

	public SimulatorData.Type.Types getType() {
		return mType;
	}

	public void setType(SimulatorData.Type.Types type) {
		mType = type;
	}

	abstract public void setupDebugGuiVbox(VBox vbox);

	abstract public LinkedList<?> populateDebugGuiVbox();
	

}