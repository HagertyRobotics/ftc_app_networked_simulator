package org.ftccommunity.simulator.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import java.util.concurrent.locks.ReentrantReadWriteLock;


//@XmlRootElement(name="xSimData")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class SimData {
	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private StringProperty mName = null;

	public SimData() {
		mName = new SimpleStringProperty("");
	}

    // Do subclass level processing in this method
    protected abstract void construct();


	public String getName() {
		return mName.getValue();
	}

	@XmlElement
	public void setName(String name) {
		mName.set(name);
	}
}