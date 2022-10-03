package teamcode.league2;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDCoefficients;

import teamcode.common.Utils;
import teamcode.common.Vector2D;

public class DriveSystemLeague2 {

    // correct ticks = current ticks * correct distance / current distance
    private static final double INCHES_TO_TICKS_VERTICAL = 45.3617021277;
    private static final double INCHES_TO_TICKS_LATERAL = -49.6078431373;
    private static final double INCHES_TO_TICKS_DIAGONAL = -64.29;
    private static final double DEGREES_TO_TICKS = 9.08617635929;
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

    public DriveSystemLeague2(HardwareMap hardwareMap) {
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

    public void vertical(double inches, double speed) {
        if (getRunMode() == DcMotor.RunMode.STOP_AND_RESET_ENCODER) {
            setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        int ticks = (int) (inches * INCHES_TO_TICKS_VERTICAL);
        setTargetPosition(ticks, ticks, ticks, ticks);
        setRunMode(DcMotor.RunMode.RUN_TO_POSITION);
        for (DcMotor motor : motors) {
            motor.setPower(speed);
        }
        while (!nearTarget()) ;
        brake();
    }

    /**
     * @param inches positive to drive to the right, negative to drive to the left
     */
    public void lateral(double inches, double speed) {
        if (getRunMode() == DcMotor.RunMode.STOP_AND_RESET_ENCODER) {
            setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        int ticks = (int) (inches * INCHES_TO_TICKS_LATERAL);
        setTargetPosition(-ticks, ticks, ticks, -ticks);
        setRunMode(DcMotor.RunMode.RUN_TO_POSITION);
        for (DcMotor motor : motors) {
            motor.setPower(speed);
        }
        while (!nearTarget()) ;
        brake();
    }

    /**
     * Drives at an angle whose reference angle is 45 degrees and lies in the specified quadrant.
     *
     * @param quadrant 0, 1, 2, or 3 corresponds to I, II, III, or IV respectively
     * @param inches   the inches to be travelled
     * @param speed    [0.0, 1.0]
     */
    public void diagonal(int quadrant, double inches, double speed) {
        if (getRunMode() == DcMotor.RunMode.STOP_AND_RESET_ENCODER) {
            setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        int ticks = (int) (inches * INCHES_TO_TICKS_DIAGONAL);
        int[] targets = new int[4];
        double[] powers = new double[4];

        switch (quadrant) {
            case 0:
                // forward right
                targets[0] = ticks;
                targets[3] = ticks;

                powers[0] = speed;
                powers[3] = speed;
                break;
            case 1:
                // forward left
                targets[1] = ticks;
                targets[2] = ticks;

                powers[1] = speed;
                powers[2] = speed;
                break;
            case 2:
                // backward left
                targets[0] = -ticks;
                targets[3] = -ticks;

                powers[0] = speed;
                powers[3] = speed;
                break;
            case 3:
                // backward right
                targets[1] = -ticks;
                targets[2] = -ticks;

                powers[1] = speed;
                powers[2] = speed;
                break;
            default:
                throw new IllegalArgumentException("quadrant must be 0, 1, 2, or 3");
        }

        setTargetPosition(targets[0], targets[1], targets[2], targets[3]);
        setRunMode(DcMotor.RunMode.RUN_TO_POSITION);

        for (int i = 0; i < 4; i++) {
            DcMotor motor = motors[i];
            motor.setPower(powers[i]);
        }

        while (!nearTarget()) ;
        brake();
    }

    /**
     * @param degrees degrees to turn clockwise
     * @param speed   [0.0, 1.0]
     */
    public void turn(double degrees, double speed) {
        if (getRunMode() == DcMotor.RunMode.STOP_AND_RESET_ENCODER) {
            setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        int ticks = (int) (degrees * DEGREES_TO_TICKS);
        setTargetPosition(ticks, -ticks, ticks, -ticks);
        setRunMode(DcMotor.RunMode.RUN_TO_POSITION);
        for (DcMotor motor : motors) {
            motor.setPower(speed);
        }
        while (!nearTarget()) ;
        brake();
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
