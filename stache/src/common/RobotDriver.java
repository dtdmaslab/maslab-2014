package common;

import org.opencv.core.Mat;

public interface RobotDriver {
    /**
     * Properties.
     */
    public static final int teamId = 16;

    /**
     * Output.
     */
    public void setLeftWheel(float voltage);
    public void setRightWheel(float voltage);
    // TODO: Once we figure out how many servos we will have, make individual
    // functions for each of them.
    public void setServoN(int n, float voltage);

    /**
     * Input.
     */
    public Mat getCamera();
    public float getGyro();
    // TODO: Once we figure out how many sensors we will have, make individual
    // functions for each of them.
    public float getSensorN(int n);
}
