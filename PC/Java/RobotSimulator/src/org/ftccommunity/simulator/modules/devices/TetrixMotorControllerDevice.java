package org.ftccommunity.simulator.modules.devices;

import org.ftccommunity.simulator.data.MotorSimData;
import org.ftccommunity.simulator.data.SimData;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class TetrixMotorControllerDevice extends Device {

	// GUI stuff for the Debug windows
	public Label mMotor1SpeedDebugLabel;
	public Label mMotor2SpeedDebugLabel;

    /**
     * Default constructor.
     */
	public TetrixMotorControllerDevice() {
		super(DeviceType.TETRIX_MOTOR);
		mSimData = new SimData[2];
		mSimData[0] = new MotorSimData();	// Add 1st motor
		mSimData[1] = new MotorSimData();	// Add 2nd motor
	}

	public void processBuffer(byte[] packet, byte[] mCurrentStateBuffer ) {
		int p;

        p=16+0*32;
/*
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

            	// Use the lock in the MotorData object to lock before write
            	super.lock.writeLock().lock();
                try {
                	if (mCurrentStateBuffer[p+4+5] == (byte)0x80) {
                		mMotor1FloatMode=true;
                		mMotor1Speed=0.0f;
                	} else {
	                	mMotor1Speed = (float)mCurrentStateBuffer[p+4+5]/100.0f;
                	}

                	if (mCurrentStateBuffer[p+4+6] == (byte)0x80) {
                		mMotor2FloatMode=true;
                		mMotor2Speed=0.0f;
                	} else {
	                	mMotor2Speed = (float)mCurrentStateBuffer[p+4+6]/100.0f;
                	}
                } finally {
                	super.lock.writeLock().unlock();
                }
            }
        }
        */
    }


	//
	// GUI Routines
	//

	public void updateDebugGuiVbox() {
		mMotor1SpeedDebugLabel.setText("" + mSimData[0]);
		mMotor2SpeedDebugLabel.setText("" + mSimData[1]);
	}

	public void setupDebugGuiVbox(VBox vbox) {
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(8);

		Text title = new Text("Put NAME here");
	    title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
	    vbox.getChildren().add(title);

	    HBox hboxMotor1 = new HBox();
	    hboxMotor1.setPadding(new Insets(5, 12, 5, 12));
	    hboxMotor1.setSpacing(10);

		Text motor1Text = new Text("Motor 1");
		VBox.setMargin(motor1Text, new Insets(0, 0, 0, 8));
		hboxMotor1.getChildren().add(motor1Text);

		Label mMotor1SpeedDebugLabel = new Label("label");
		this.mMotor1SpeedDebugLabel = mMotor1SpeedDebugLabel;
		hboxMotor1.getChildren().add(mMotor1SpeedDebugLabel);
		vbox.getChildren().add(hboxMotor1);

		HBox hboxMotor2 = new HBox();
		hboxMotor2.setPadding(new Insets(5, 12, 5, 12));
		hboxMotor2.setSpacing(10);

	 	Text motor2Text = new Text("Motor 2");
	 	VBox.setMargin(motor2Text, new Insets(0, 0, 0, 8));
	 	hboxMotor2.getChildren().add(motor2Text);

	 	Label mMotor2SpeedDebugLabel = new Label("label");
	 	this.mMotor2SpeedDebugLabel = mMotor2SpeedDebugLabel;
	 	hboxMotor2.getChildren().add(mMotor2SpeedDebugLabel);
	 	vbox.getChildren().add(hboxMotor2);
	}
}
