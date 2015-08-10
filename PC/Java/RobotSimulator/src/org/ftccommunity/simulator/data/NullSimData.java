package org.ftccommunity.simulator.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="NullSimData")
@XmlAccessorType(XmlAccessType.NONE)
public class NullSimData extends SimData {

	public NullSimData() {
		super();
		construct();
	}

    @Override
    protected void construct() {
        //System.out.println("Building Null SimData");
    }
}
