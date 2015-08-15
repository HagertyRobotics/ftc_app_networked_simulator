package org.ftccommunity.simulator.modules.devices;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;

import org.ftccommunity.simulator.data.MotorSimData;
import org.ftccommunity.simulator.data.SimData;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.ftccommunity.simulator.net.protocol.SimulatorData;

@XmlRootElement(name="TetrixMotorControllerDevice")
@XmlAccessorType(XmlAccessType.NONE)
public class TetrixMotorControllerDevice extends Device {

	// GUI stuff for the Debug windows
	private Label mMotor1SpeedDebugLabel;
	private Label mMotor2SpeedDebugLabel;

    /**
     * Default constructor.
     */
	public TetrixMotorControllerDevice() {
		super(SimulatorData.Type.Types.LEGACY_MOTOR);
		mSimData = new SimData[2];
		mSimData[0] = new MotorSimData();	// Add 1st motor
		mSimData[1] = new MotorSimData();	// Add 2nd motor
	}

	public void processBuffer(byte[] packet, byte[] mCurrentStateBuffer, int portNum ) {
		int p;

        p=16+portNum*32;

        // This is for Port P0 only.  16 is the base offset.  Each port has 32 bytes.
        // If I2C_ACTION is set, take some action
//	        if (packet[p+32] == (byte)0xff) { // Action flag
        if ((packet[p] & (byte)0x01) == (byte)0x01) { // I2C Mode
        	if ((packet[p] & (byte)0x80) == (byte)0x80) { // Read mode
            	// Copy this port's 32 bytes into buffer
        		System.arraycopy(packet, p, mCurrentStateBuffer, p, 32);

            } else { // Write mode
            	// Copy this port's 32 bytes into buffer
            	System.arraycopy(packet, p, mCurrentStateBuffer, p, 32);

            	((MotorSimData)mSimData[0]).setMotorSpeed(mCurrentStateBuffer[p+4+5]);
            	((MotorSimData)mSimData[1]).setMotorSpeed(mCurrentStateBuffer[p+4+6]);
            }
        }
    }


	//
	// GUI Routines
	//

	public void updateDebugGuiVbox() {
		mMotor1SpeedDebugLabel.setText("" + ((MotorSimData)mSimData[0]).getMotorSpeed());
		mMotor2SpeedDebugLabel.setText("" + ((MotorSimData)mSimData[1]).getMotorSpeed());
	}

	public void setupDebugGuiVbox(VBox vbox) {
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(8);

		Text title = new Text(DeviceType.getName(mType));
	    title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
	    vbox.getChildren().add(title);

	    HBox hboxMotor1 = new HBox();
	    hboxMotor1.setPadding(new Insets(5, 12, 5, 12));
	    hboxMotor1.setSpacing(10);

		Text motor1Text = new Text(mSimData[0].getName());
		VBox.setMargin(motor1Text, new Insets(0, 0, 0, 8));
		hboxMotor1.getChildren().add(motor1Text);

		Label mMotor1SpeedDebugLabel = new Label("label");
		this.mMotor1SpeedDebugLabel = mMotor1SpeedDebugLabel;
		hboxMotor1.getChildren().add(mMotor1SpeedDebugLabel);
		vbox.getChildren().add(hboxMotor1);

		HBox hboxMotor2 = new HBox();
		hboxMotor2.setPadding(new Insets(5, 12, 5, 12));
		hboxMotor2.setSpacing(10);

	 	Text motor2Text = new Text(mSimData[1].getName());
	 	VBox.setMargin(motor2Text, new Insets(0, 0, 0, 8));
	 	hboxMotor2.getChildren().add(motor2Text);

	 	Label mMotor2SpeedDebugLabel = new Label("label");
	 	this.mMotor2SpeedDebugLabel = mMotor2SpeedDebugLabel;
	 	hboxMotor2.getChildren().add(mMotor2SpeedDebugLabel);
	 	vbox.getChildren().add(hboxMotor2);
	}
}
