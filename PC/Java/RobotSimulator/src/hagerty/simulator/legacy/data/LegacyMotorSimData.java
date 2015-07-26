package hagerty.simulator.legacy.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

@XmlAccessorType(XmlAccessType.NONE)
public class LegacyMotorSimData extends SimData {

	volatile float mMotor1Speed=0.0f;
	volatile float mMotor2Speed=0.0f;
	volatile boolean mMotor1FloatMode=false;
	volatile boolean mMotor2FloatMode=false;

	// GUI stuff for the Debug windows
	public Label mMotor1SpeedDebugLabel;
	public Label mMotor2SpeedDebugLabel;

	public LegacyMotorSimData() {
		super(SimDataType.LEGACY_MOTOR);
		construct();
	}

    @Override
    protected void construct() {
        System.out.println("Building Legacy Motor SimData");
    }
    
	public void populateDebugGuiVbox() {
		mMotor1SpeedDebugLabel.setText("" + mMotor1Speed);
		mMotor2SpeedDebugLabel.setText("" + mMotor2Speed);
	}

	public void setupDebugGuiVbox(VBox vbox) {
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



	public float getMotor1Speed() {
		return mMotor1Speed;
	}

	public float getMotor2Speed() {
		return mMotor2Speed;
	}

	public void setMotor1Speed(float motorSpeed){
		mMotor1Speed = motorSpeed;
	}

	public void setMotor2Speed(float motorSpeed){
		mMotor2Speed = motorSpeed;
	}

	public void setMotor1FloatMode(boolean floatMode) {
		mMotor1FloatMode = floatMode;
		setMotor1Speed(0.0f);
	}

	public void setMotor2FloatMode(boolean floatMode) {
		mMotor2FloatMode = floatMode;
		setMotor2Speed(0.0f);
	}

	public boolean getMotor1FloatMode() {
		return mMotor1FloatMode;
	}

	public boolean getMotor2FloatMode() {
		return mMotor2FloatMode;
	}
}