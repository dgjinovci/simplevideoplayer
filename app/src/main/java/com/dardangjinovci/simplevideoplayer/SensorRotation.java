package com.dardangjinovci.simplevideoplayer;

/**
 * Created by Dardan on 22-Feb-17.
 */

public enum SensorRotation {

    LANDSCAPE_SENSOR(0),
    LANDSCAPE_LEFT(90),
    LANDSCAPE_RIGHT(-90);

    int value;

    SensorRotation(int value) {
        this.value = value;
    }

    static SensorRotation fromValue(int value) {

        for (SensorRotation s : values())
            if (s.value == value) return s;

        throw new IllegalArgumentException("Id not found for SensorRotation");
    }
}
