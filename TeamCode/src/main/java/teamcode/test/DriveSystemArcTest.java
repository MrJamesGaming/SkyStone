package teamcode.test;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDCoefficients;

import teamcode.common.Debug;
import teamcode.common.Utils;
import teamcode.common.Vector2D;
import teamcode.league2.HardwareComponentNamesLeague2;

public class DriveSystemArcTest {

    // correct ticks = current ticks * correct distance / current distance
    private static final double INCHES_TO_TICKS_VERTICAL = 45.3617021277;
    private static final double INCHES_TO_TICKS_LATERAL = -49.6078431373;
    private static final double INCHES_TO_TICKS_DIAGONAL = -64.29;
    private static final double DEGREES_TO_TICKS = 9.08617635929;

    private static final double WHEEL_BASE_WIDTH_LATERAL = 12.5;
    private static final double DEGREES_TO_ARC_TICKS = 22.22;

    /**
     * Maximum number of ticks a motor's current position must be away from it's target for it to
     * be considered near its target.
     */
    private static final double TICK_ERROR_TOLERANCE = 40.0;
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

    public DriveSystemArcTest(HardwareMap hardwareMap) {
        frontLeft = hardwareMap.get(DcMotor.class, HardwareComponentNamesLeague2.FRONT_LEFT_DRIVE);
        frontRight = hardwareMap.get(DcMotor.class, HardwareComponentNamesLeague2.FRONT_RIGHT_DRIVE);
        backLeft = hardwareMap.get(DcMotor.class, HardwareComponentNamesLeague2.BACK_LEFT_DRIVE);
        backRight = hardwareMap.get(DcMotor.class, HardwareComponentNamesLeague2.BACK_RIGHT_DRIVE);
        motors = new DcMotor[]{frontLeft, frontRight, backLeft, backRight};
        correctDirections();
        setPID();
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
        setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        int ticks = (int) (inches * INCHES_TO_TICKS_VERTICAL);

        for (DcMotor motor : motors) {
            motor.setTargetPosition(ticks);
        }
        setRunMode(DcMotor.RunMode.RUN_TO_POSITION);

        for (DcMotor motor : motors) {
            motor.setPower(speed);
        }

        while (!nearTarget()) ;
        brake();
    }

    //positive is to the right and negative is to the left
    public void lateral(double inches, double speed) {
        setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        int ticks = (int) (inches * INCHES_TO_TICKS_LATERAL);

        frontLeft.setTargetPosition(-ticks);
        frontRight.setTargetPosition(ticks);
        backLeft.setTargetPosition(ticks);
        backRight.setTargetPosition(-ticks);
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
        setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
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

        for (int i = 0; i < 4; i++) {
            DcMotor motor = motors[i];
            motor.setTargetPosition(targets[i]);
        }
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
    //positive is clockwise and negative is counterclockwise
    public void turn(double degrees, double speed) {
        setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        int ticks = (int) (degrees * DEGREES_TO_TICKS);
        frontLeft.setTargetPosition(ticks);
        frontRight.setTargetPosition(-ticks);
        backLeft.setTargetPosition(ticks);
        backRight.setTargetPosition(-ticks);
        setRunMode(DcMotor.RunMode.RUN_TO_POSITION);

        for (DcMotor motor : motors) {
            motor.setPower(speed);
        }

        while (!nearTarget()) ;
        //brake();
        setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    public void brake() {
        for (DcMotor motor : motors) {
            motor.setPower(0.0);
        }
    }

    private boolean nearTarget() {
        for (DcMotor motor : motors) {
            int targetPosition = motor.getTargetPosition();
            int currentPosition = motor.getCurrentPosition();
            double ticksFromTarget = Math.abs(targetPosition - currentPosition);
            if (ticksFromTarget >= TICK_ERROR_TOLERANCE) {
                return false;
            }
        }
        return true;
    }


    /**
     * @param counterClockwise true for pivot point being front left
     * @param power            power of the motor between 0 and 1
     */
    public void frontArc(boolean counterClockwise, double power, int degrees, double radius) {
        double w = Math.PI / 4; // 45 degrees / sec
        double robotHalfLength = WHEEL_BASE_WIDTH_LATERAL / 2;
        double p2 = w * (radius + robotHalfLength);
        double p1 = w * (radius - robotHalfLength);

        Debug.log("w: " + w);
        Debug.log("p1: " + p1);
        Debug.log("p2: " + p2);
        p1 = 0.5 * (p1 / p2);
        p2 = 0.5;

        double arcLengthInner = Math.PI * degrees * radius / 180.0;
        double arcLengthOuter = Math.PI * degrees * (radius + WHEEL_BASE_WIDTH_LATERAL) / 180.0;
        int innerTicks = (int)(arcLengthInner * DEGREES_TO_ARC_TICKS);
        int outerTicks = (int)(arcLengthOuter * DEGREES_TO_ARC_TICKS);
        setTargetPosition(innerTicks, outerTicks, innerTicks, outerTicks);
        Utils.sleep(6000);
        frontLeft.setPower(p1);
        backLeft.setPower(p1);
        frontRight.setPower(p2);
        backRight.setPower(p2);


//        Debug.log("gets here" + degrees);
//        setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//        int ticks = degrees * (int) DEGREES_TO_ARC_TICKS;
//        double length1 = (degrees * Math.PI * radius) / 180.0;
//        double length2 =  (degrees * Math.PI * (radius + WHEEL_BASE_WIDTH_LATERAL)) / 180.0;
//        double arcLengthChange = length2 - length1;
//        if(counterClockwise){
//            frontLeft.setTargetPosition(ticks);
//            backLeft.setTargetPosition(ticks);
//            frontRight.setTargetPosition(ticks + (int)arcLengthChange);
//            backRight.setTargetPosition(ticks + (int)arcLengthChange);
//        }else{
//            frontRight.setTargetPosition(ticks);
//            backRight.setTargetPosition(ticks);
//            frontLeft.setTargetPosition(ticks + (int)arcLengthChange);
//            backLeft.setTargetPosition(ticks + (int)arcLengthChange);
//        }
//
//        double innerPower = outerPower * (length1 / length2);
//
//        setRunMode(DcMotor.RunMode.RUN_TO_POSITION);
//        Debug.log("target fl " + frontLeft.getTargetPosition());
//        Debug.log("target fr " + frontRight.getTargetPosition());
//        Debug.log("target bl" + backLeft.getTargetPosition());
//        Debug.log("target fl " + backRight.getTargetPosition());
//        if (counterClockwise) {
//            if (degrees > 0) {
//                frontLeft.setPower(innerPower);
//                frontRight.setPower(outerPower);
//                backRight.setPower(outerPower);
//                backLeft.setPower(innerPower);
//            } else {
//                frontLeft.setPower(innerPower);
//                frontRight.setPower(outerPower);
//                backRight.setPower(outerPower);
//                backLeft.setPower(innerPower);
//            }
//        } else {
//            if (degrees > 0) {
//                frontLeft.setPower(outerPower);
//                frontRight.setPower(innerPower);
//                backRight.setPower(innerPower);
//                backLeft.setPower(outerPower);
//            } else {
//                frontLeft.setPower(-outerPower);
//                frontRight.setPower(innerPower);
//                backRight.setPower(-innerPower);
//                backLeft.setPower(-outerPower);
//            }
//        }
        Debug.log("P1: " + p1);
        Debug.log("P2: " + p2);
    }

    private boolean nearTargetArc() {
        for (DcMotor motor : motors) {
            int targetPosition = motor.getTargetPosition();
            int currentPosition = motor.getCurrentPosition();
            double ticksFromTarget = Math.abs(targetPosition - currentPosition);
            if (ticksFromTarget >= TICK_ERROR_TOLERANCE) {
                return false;
            }
        }
        return true;
    }


    private void setRunMode(DcMotor.RunMode mode) {
        for (DcMotor motor : motors) {
            motor.setMode(mode);
        }
    }
    private void setTargetPosition(int frontLeftTicks, int frontRightTicks, int backLeftTicks, int backRightTicks){
        frontLeft.setTargetPosition(frontLeftTicks);
        frontRight.setTargetPosition(frontRightTicks);
        backLeft.setTargetPosition(backLeftTicks);
        backRight.setTargetPosition(backRightTicks);
    }

}
