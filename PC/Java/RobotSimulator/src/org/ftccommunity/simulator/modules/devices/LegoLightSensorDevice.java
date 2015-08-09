package org.ftccommunity.simulator.modules.devices;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.ftccommunity.simulator.data.AnalogSimData;
import org.ftccommunity.simulator.data.MotorSimData;
import org.ftccommunity.simulator.data.SimData;

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

        // If I2C_ACTION is set, take some action
        // if (packet[p+32] == (byte)0xff) { // Action flag
        if ((packet[p] & (byte)0x01) == (byte)0x01) { // I2C Mode
        	if ((packet[p] & (byte)0x80) == (byte)0x80) { // Read mode
            	// Copy this port's 32 bytes into buffer
        		System.arraycopy(packet, p, mCurrentStateBuffer, p, 32);
        		int a = (int) ((AnalogSimData)mSimData[0]).getAnalogValue() * 1024;
        		mCurrentStateBuffer[q] = (byte)(a&0xff);
        		mCurrentStateBuffer[q+1] = (byte)((a>>8)&0xff);

            } else { // Write mode
            	// Copy this port's 32 bytes into buffer
            	//System.arraycopy(packet, p, mCurrentStateBuffer, p, 32);
            }
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
