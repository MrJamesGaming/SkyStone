package teamcode.common;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

public class Utils {

    public static final double SKYSTONE_LENGTH_INCHES = 8;
    public static final double SKYSTONE_WIDTH_INCHES = 4;
    public static final double MAT_LENGTH_INCHES = 24;

    private Utils() {
    }

    public static boolean servoNearPosition(Servo servo, double position, double errorTolerance) {
        errorTolerance = Math.abs(errorTolerance);
        Debug.log("pos = " + servo.getPosition() + ", target = " + position);
        return Math.abs(servo.getPosition() - position) <= errorTolerance;
    }

    public static boolean motorNearTarget(DcMotor motor, int errorTolerance) {
        int current = motor.getCurrentPosition();
        int target = motor.getTargetPosition();
        int distance = Math.abs(target - current);
        Debug.log("Distance = " + distance);
        return distance <= errorTolerance;
    }

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
