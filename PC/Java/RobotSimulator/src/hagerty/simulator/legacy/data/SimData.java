package hagerty.simulator.legacy.data;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.VBox;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class SimData {

    protected final StringProperty simDataName;

	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public SimData() {
		this.simDataName = new SimpleStringProperty("S0");
	}

	public String getSimDataName() {
		return simDataName.getValue();
	}

	abstract public void setupDebugGuiVbox(VBox vbox);

	abstract public void populateDebugGuiVbox();
}