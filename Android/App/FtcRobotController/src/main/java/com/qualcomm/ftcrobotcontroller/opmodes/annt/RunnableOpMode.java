package com.qualcomm.ftcrobotcontroller.opmodes.annt;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The <code>ActiveOpMode</code> annotation is used to specify that an op mode should be registered
 * for display on the driver station.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RunnableOpMode {
    /**
     * The name of the op mode.
     * This is displayed on the driver station.
     */
    String value();
}