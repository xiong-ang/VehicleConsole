package com.giovanniterlingen.hc05;

/**
 * Created by xiongang on 2018/3/3.
 */

public class CarState {
    public volatile String speedSignal=CarState.stopSpeedSignal;
    public volatile boolean isChangeDirection=false;
    public volatile String steerSignal="000";

    public static final String lowSpeedSignal="111";
    public static final String midSpeedSignal="222";
    public static final String highSpeedSignal="333";
    public static final String reverseSpeedSignal="444";
    public static final String leftSmallSteerSignal="555";
    public static final String leftLargeSteerSignal="666";
    public static final String rightSmallSteerSignal="777";
    public static final String rightLargeSteerSignal="888";
    public static final String stopSpeedSignal="999";

    public static String GetState(String signal)
    {
        switch (signal)
        {
            case lowSpeedSignal:
                return "Speed: 1";
            case midSpeedSignal:
                return "Speed: 2";
            case highSpeedSignal:
                return "Speed: 3";
            case reverseSpeedSignal:
                return "Speed: -1";
            case leftSmallSteerSignal:
                return "Steer: L1/2";
            case leftLargeSteerSignal:
                return "Steer: L1";
            case rightSmallSteerSignal:
                return "Steer: R1/2";
            case rightLargeSteerSignal:
                return "Steer: R1";
            case stopSpeedSignal:
                return "Speed: 0";
            default:
                return "Invalid Signal";
        }
    }
}
