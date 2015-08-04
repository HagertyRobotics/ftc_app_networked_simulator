package org.ftccommunity.simulator.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.ftccommunity.simulator.modules.devices.DeviceType;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

//@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="LegacySimData")
public class MotorSimData extends SimData {

	float mMotorSpeed;
	boolean mMotorFloatMode;

	public MotorSimData() {
		super();
		construct();
	}

    @Override
    protected void construct() {
        System.out.println("Building Legacy Motor SimData");
    }

	public float getMotorSpeed() {
		return mMotorSpeed;
	}

	public boolean getMotorFloatMode() {
		return mMotorFloatMode;
	}
}
