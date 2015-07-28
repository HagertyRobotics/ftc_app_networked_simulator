package hagerty.simulator.legacy.data;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.LinkedList;
import java.util.logging.Logger;

@XmlAccessorType(XmlAccessType.NONE)
public class LegacyMotorSimData extends SimData {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	volatile float mMotor1Speed=0.0f;
	volatile float mMotor2Speed=0.0f;
	volatile boolean mMotor1FloatMode=false;
	volatile boolean mMotor2FloatMode=false;

    /*// GUI stuff for the Debug windows

*/
    public LegacyMotorSimData() {
		super(SimDataType.LEGACY_MOTOR);
		construct();
	}

    @Override
    protected void construct() {
        System.out.println("Building Legacy Motor SimData");
    }



    public void processBuffer(int port, byte[] packet, byte[] mCurrentStateBuffer ) {
        int p;

        p=16+port*32;
    	// This is for Port P0 only.  16 is the base offset.  Each port has 32 bytes.
        // If I2C_ACTION is set, take some action
//        if (packet[p+32] == (byte)0xff) { // Action flag
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
                            mMotor1Speed = (float) mCurrentStateBuffer[p + 4 + 5] / 100.0f;
                        }

	                	if (mCurrentStateBuffer[p+4+6] == (byte)0x80) {
	                		mMotor2FloatMode=true;
	                		mMotor2Speed=0.0f;
	                	} else {
                            mMotor2Speed = (float) mCurrentStateBuffer[p + 4 + 6] / 100.0f;
                        }
                    } finally {
                    	super.lock.writeLock().unlock();
                    }
                }

            }
    }


//
// GUI Routines
//

    public LinkedList<Label> populateDebugGuiVbox() {
        LinkedList<Label> labels = new LinkedList<>();

        Label mMotor1SpeedDebugLabel = new Label("");
        Label mMotor2SpeedDebugLabel = new Label("");

		mMotor1SpeedDebugLabel.setText("" + mMotor1Speed);
		mMotor2SpeedDebugLabel.setText("" + mMotor2Speed);

        labels.add(mMotor1SpeedDebugLabel);
        labels.add(mMotor2SpeedDebugLabel);
        return labels;
    }

	public void setupDebugGuiVbox(VBox vbox) {
        LinkedList<Label> labels = populateDebugGuiVbox();

    	vbox.setPadding(new Insets(10));
    	vbox.setSpacing(8);

		Text title = new Text(getSimDataName());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
	    vbox.getChildren().add(title);

	    HBox hboxMotor1 = new HBox();
	    hboxMotor1.setPadding(new Insets(5, 12, 5, 12));
	    hboxMotor1.setSpacing(10);

		Text motor1Text = new Text("Motor 1");
		VBox.setMargin(motor1Text, new Insets(0, 0, 0, 8));
		hboxMotor1.getChildren().add(motor1Text);

        hboxMotor1.getChildren().add(labels.get(0));
        vbox.getChildren().add(hboxMotor1);

		HBox hboxMotor2 = new HBox();
		hboxMotor2.setPadding(new Insets(5, 12, 5, 12));
		hboxMotor2.setSpacing(10);

	 	Text motor2Text = new Text("Motor 2");
	 	VBox.setMargin(motor2Text, new Insets(0, 0, 0, 8));
	 	hboxMotor2.getChildren().add(motor2Text);

        hboxMotor2.getChildren().add(labels.get(1));
        vbox.getChildren().add(hboxMotor2);
	}


	public float getMotor1Speed() {
		return mMotor1Speed;
	}

	public float getMotor2Speed() {
		return mMotor2Speed;
	}

	public boolean getMotor1FloatMode() {
		return mMotor1FloatMode;
	}

	public boolean getMotor2FloatMode() {
		return mMotor2FloatMode;
	}
}