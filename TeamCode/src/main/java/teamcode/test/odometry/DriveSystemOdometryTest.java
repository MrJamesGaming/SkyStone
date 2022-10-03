package teamcode.test.odometry;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDCoefficients;

import teamcode.common.Utils;
import teamcode.common.Vector2D;
import teamcode.league2.HardwareComponentNamesLeague2;

public class DriveSystemOdometryTest {

     //correct ticks = current ticks * correct distance / current distance
     double xPower;
     double yPower;
     double turnPower;

    /**
     * Maximum number of ticks a motor's current position must be away from it's target for it to
     * be considered near its target.
     */
    private static final int TICK_ERROR_TOLERANCE = 30;
    /**
     * Proportional.
     */
    private static final double P = 2.5;
    /**
     * Integral.
     */
    private static final double I = 0.1;
    /**
     * Derivative.
     */
    private static final double D = 0.0;

    private final DcMotor frontLeft, frontRight, backLeft, backRight;
    private final DcMotor[] motors;

    public DriveSystemOdometryTest(HardwareMap hardwareMap) {
        frontLeft = hardwareMap.get(DcMotor.class, HardwareComponentNamesLeague2.FRONT_LEFT_DRIVE);
        frontRight = hardwareMap.get(DcMotor.class, HardwareComponentNamesLeague2.FRONT_RIGHT_DRIVE);
        backLeft = hardwareMap.get(DcMotor.class, HardwareComponentNamesLeague2.BACK_LEFT_DRIVE);
        backRight = hardwareMap.get(DcMotor.class, HardwareComponentNamesLeague2.BACK_RIGHT_DRIVE);
        motors = new DcMotor[]{frontLeft, frontRight, backLeft, backRight};
        correctDirections();
        setPID();
        setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    private void correctDirections() {
        //frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    private void setPID() {
        PIDCoefficients coefficients = new PIDCoefficients();
        coefficients.i = I;
        coefficients.p = P;
        coefficients.d = D;
        for (DcMotor motor : motors) {
            DcMotorEx ex = (DcMotorEx) motor;
            ex.setPIDCoefficients(DcMotor.RunMode.RUN_TO_POSITION, coefficients);
        }
    }

    public DcMotor[] getMotors() {
        return motors;
    }

    public void continuous(Vector2D velocity, double turnSpeed) {
        setRunMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        double direction = velocity.getDirection();

        double maxPow = Math.sin(Math.PI / 4);
        double power = velocity.magnitude() / maxPow;

        double angle = direction - Math.PI / 4;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double frontLeftPow = power * sin + turnSpeed;
        double frontRightPow = power * cos - turnSpeed;
        double backLeftPow = power * cos + turnSpeed;
        double backRightPow = power * sin - turnSpeed;
        frontLeft.setPower(frontLeftPow);
        frontRight.setPower(frontRightPow);
        backLeft.setPower(backLeftPow);
        backRight.setPower(backRightPow);

    }



    public void brake() {
        for (DcMotor motor : motors) {
            motor.setPower(0.0);
        }
    }

    private boolean nearTarget() {
        for (DcMotor motor : motors) {
            if (!Utils.motorNearTarget(motor, TICK_ERROR_TOLERANCE)) {
                return false;
            }
        }
        return true;
    }

    private DcMotor.RunMode getRunMode() {
        return frontLeft.getMode();
    }

    private void setRunMode(DcMotor.RunMode mode) {
        for (DcMotor motor : motors) {
            motor.setMode(mode);
        }
    }

    private void setTargetPosition(int frontLeftTicks, int frontRightTicks, int backLeftTicks, int backRightTicks) {
        frontLeft.setTargetPosition(frontLeft.getCurrentPosition() + frontLeftTicks);
        frontRight.setTargetPosition(frontRight.getCurrentPosition() + frontRightTicks);
        backLeft.setTargetPosition(backLeft.getCurrentPosition() + backLeftTicks);
        backRight.setTargetPosition(backRight.getCurrentPosition() + backRightTicks);
    }

}
