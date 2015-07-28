package org.ftccommunity.simulator;

import java.util.LinkedList;

public class ControllerData {
    LinkedList<Motor> motors;

    public ControllerData(int numberOfMotors) {
        motors =  new LinkedList<Motor>();
        for (int i = 0; i < numberOfMotors; i++) {
            motors.add(new Motor());
        }
    }

    @Deprecated
    public ControllerData() {
        motors =  new LinkedList<Motor>();
        for (int i = 0; i < 2; i++) {
            motors.add(new Motor());
        }
    }

    double getMotorSpeed(int motorNumber) {
        return motors.get(motorNumber - 1).getMotorSpeed();
    }

    void setMotorSpeed(int motorNumber, double motorSpeed) {
        motors.get(motorNumber - 1).setMotorSpeed(motorSpeed);
    }

    void setFloatMode(int motorNumber, boolean floatMode) {
        motors.get(motorNumber - 1).setFloatMode(floatMode);
        setMotorSpeed(motorNumber, 0.0);
    }

    boolean getFloatMode(int motorNumber) {
        return motors.get(motorNumber - 1).isFloatMode();
    }

    public class Motor {
        private double motorSpeed;
        private boolean floatMode;

        public Motor() {
            motorSpeed = 0;
            floatMode = false;
        }

        public double getMotorSpeed() {
            return motorSpeed;
        }

        public void setMotorSpeed(double motorSpeed) {
            this.motorSpeed = motorSpeed;
        }

        public boolean isFloatMode() {
            return floatMode;
        }

        public void setFloatMode(boolean floatMode) {
            this.floatMode = floatMode;
        }
    }
}
