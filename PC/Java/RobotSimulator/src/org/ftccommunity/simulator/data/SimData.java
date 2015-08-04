package org.ftccommunity.simulator.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.VBox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ftccommunity.simulator.modules.devices.DeviceType;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;


//@XmlRootElement(name="xSimData")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class SimData {

	@XmlElement
	protected StringProperty mName = null;

    // Do subclass level processing in this method
    protected abstract void construct();


	public String getName() {
		return mName.getValue();
	}

	public void setName(String name) {
		mName = new SimpleStringProperty(name);
	}
}