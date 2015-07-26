package hagerty.simulator.legacy.data;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.VBox;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class SimData {

    protected StringProperty simDataName = null;
    private SimDataType mType=null;

	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public SimData(SimDataType type) {
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

	public SimDataType getType() {
		return mType;
	}

	public void setType(SimDataType type) {
		mType = type;
	}

	abstract public void setupDebugGuiVbox(VBox vbox);

	abstract public void populateDebugGuiVbox();
}