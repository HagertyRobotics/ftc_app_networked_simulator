package org.ftccommunity.simulator.modules.devices;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.ftccommunity.simulator.data.AnalogSimData;
import org.ftccommunity.simulator.data.MotorSimData;
import org.ftccommunity.simulator.data.SimData;
import org.ftccommunity.utils.Utils;

import javax.xml.bind.annotation.XmlAccessType;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

@XmlRootElement(name="LegoLightSensorDevice")
@XmlAccessorType(XmlAccessType.NONE)
public class LegoLightSensorDevice extends Device {

	// GUI stuff for the Debug windows
	public Label mLightSensorDebugLabel;

	public LegoLightSensorDevice() {
		super(DeviceType.LEGO_LIGHT);
		mSimData = new SimData[1];
		mSimData[0] = new AnalogSimData();
	}

	public void processBuffer(byte[] packet, byte[] mCurrentStateBuffer, int portNum) {
		int p;
		int q;

        p=16+portNum*32;
        q=4+portNum*2;

        if ((packet[p] & (byte)0x01) == (byte)0x00) { // Analog Mode
        	// Copy this port's 32 bytes into buffer
    		int a = (int) (((AnalogSimData)mSimData[0]).getAnalogValue()*256.0);
    		mCurrentStateBuffer[q] = (byte)a;

    		// Set the Port ready bit in the global part of the Current State Buffer
    		int bufferStatus = ~(1 << portNum);
    		mCurrentStateBuffer[3] &= (byte)bufferStatus;
        }
    }

	public void updateDebugGuiVbox() {
		mLightSensorDebugLabel.setText("" + ((AnalogSimData)mSimData[0]).getAnalogValue());
	}

	public void setupDebugGuiVbox(VBox vbox) {
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(8);

		Text title = new Text(mType.getName());
	    title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
	    vbox.getChildren().add(title);

	    HBox hboxMotor1 = new HBox();
	    hboxMotor1.setPadding(new Insets(5, 12, 5, 12));
	    hboxMotor1.setSpacing(10);

		Text motor1Text = new Text(mSimData[0].getName());
		VBox.setMargin(motor1Text, new Insets(0, 0, 0, 8));
		hboxMotor1.getChildren().add(motor1Text);

		Label lightSensorDebugLabel = new Label("label");
		this.mLightSensorDebugLabel = lightSensorDebugLabel;
		hboxMotor1.getChildren().add(mLightSensorDebugLabel);
		vbox.getChildren().add(hboxMotor1);
	}
}
