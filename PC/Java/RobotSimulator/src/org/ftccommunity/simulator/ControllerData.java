package org.ftccommunity.simulator;

public class ControllerData {
    double[] mMotorSpeed = {
            0.0, 0.0
    };

    boolean[] mFloatMode = {
            false, false
    };

    double getMotorSpeed(int motorNumber) {
        return mMotorSpeed[motorNumber - 1];
    }

    void setMotorSpeed(int motorNumber, float motorSpeed) {
        mMotorSpeed[motorNumber - 1] = motorSpeed;
    }

    void setFloatMode(int motorNumber, boolean floatMode) {
        mFloatMode[motorNumber - 1] = floatMode;
        setMotorSpeed(motorNumber, 0);
    }

    boolean getFloatMode(int motorNumber) {
        return mFloatMode[motorNumber - 1];
    }

}
