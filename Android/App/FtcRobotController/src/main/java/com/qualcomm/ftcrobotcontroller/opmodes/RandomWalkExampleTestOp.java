package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import java.util.Random;

public class RandomWalkExampleTestOp extends OpMode {
    private static final int STANDARD_CYCLE_NUM = 45;
    Random randomGenerator;

    DcMotor motorRight;
    DcMotor motorLeft;
    double right;
    double left;

    int numberOfLoopsNeededToChange;
    int loopCounter;
    double probability;
    boolean isOddRun;

    public RandomWalkExampleTestOp() {
        java.security.SecureRandom tempGenerator = new java.security.SecureRandom();
        byte[] seedBytes = tempGenerator.generateSeed(Math.abs((tempGenerator.nextInt())) % 50);
        long seed = 0;
        for (Byte seedByte : seedBytes) {
            seed = (long) seedByte ^ seed;
        }
        randomGenerator = new Random(seed);

        right = 0;
        left = 0;
        loopCounter = 0;
        isOddRun = true;

        numberOfLoopsNeededToChange = 90;
        probability = 0;
    }

    public void start() {
        motorRight = hardwareMap.dcMotor.get("motor_2");
        motorLeft = hardwareMap.dcMotor.get("motor_1");

        // Two motors going forward in the simulator is having the other motor going backwards
        motorLeft.setDirection(DcMotor.Direction.REVERSE);
    }

    public void loop() {
       if (loopCounter >= numberOfLoopsNeededToChange) {
            // Create a variable to handle probability (standard deviation = .5; mean = .75)
            probability = range(randomGenerator.nextDouble(), 0, 1);
            numberOfLoopsNeededToChange = STANDARD_CYCLE_NUM;

            if (probability < .1) { // This block has a 9% chance of running
                numberOfLoopsNeededToChange *= 2;
            }
            if (probability >= .45) { // This block has a 55% chance of running
                left = 1;
                right = 1;
            } else if (probability >= .35) { // This block has 10% chance of running
                if (randomGenerator.nextBoolean()) { // This block has 5% chance of running
                    left = .75;
                    right = .25;
                } else { // This block has 5% chance of running
                    left = .25;
                    right = .75;
                }
            } else { // This block has a 35% chance of running
                // Generator a random number (0 - 1 and shift the range to (-1 - 0)
                left = randomGenerator.nextInt(2) - 1;
                right = randomGenerator.nextInt(2) - 1;

                if (left == right) {
                    numberOfLoopsNeededToChange /= 2;
                } else if (left == 0 || left == right ){
                    numberOfLoopsNeededToChange /= 3;
                }
            }
            // Reset the loop counter
            loopCounter = 0;
        }

        // write the values to the motors, flipping which run is written first
        if (isOddRun) {
            motorRight.setPower(right);
            motorLeft.setPower(left);
        } else {
            motorLeft.setPower(left);
            motorRight.setPower(right);
        }

        // Telemetry
        telemetry.addData("motor_pwr", "Left: " + left + " Right: " + right);
        telemetry.addData("prb", "Probability " + probability);
        // Flip the value of isOddRun
        isOddRun = !isOddRun;
        // Increment the loop counter
        loopCounter++;
    }

    public void stop() {
        motorRight.setPower(0);
        motorLeft.setPower(0);
    }

    public double range(double arbitraryNumber, double min, double max) {
        double tempNumber = Math.max(arbitraryNumber, min);
        tempNumber = Math.min(tempNumber, max);

        return tempNumber;
    }

}
