package teamcode.league1;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.Timer;
import java.util.TimerTask;

import teamcode.common.AbstractOpMode;
import teamcode.league2.HardwareComponentNamesLeague2;

public class League1TTArm {

    private static final double CLAW_OPEN_POS = 0.4;
    private static final double CLAW_CLOSE_POS = 0.0;
    private static final double CLAW_MID_POS = 0.2;
    private static final double CLAW_POSITION_ERROR = 0.1;

    private final CRServo lift;
    private final ColorSensor liftSensor;
    private final Servo claw;
    private final Timer timer;

    public League1TTArm(HardwareMap hardwareMap) {
        lift = hardwareMap.get(CRServo.class, HardwareComponentNamesLeague2.ARM_LIFT);
        lift.setDirection(DcMotorSimple.Direction.REVERSE);
        liftSensor = hardwareMap.get(ColorSensor.class, HardwareComponentNamesLeague2.ARM_LIFT_SENSOR);
        claw = hardwareMap.get(Servo.class, HardwareComponentNamesLeague2.ARM_CLAW);
        timer = AbstractOpMode.currentOpMode().getNewTimer();
    }

    public void testColorSensor(Telemetry telemetry, ColorSensor sensor) {
        int red = sensor.red();
        int blue = sensor.blue();
        int green = sensor.green();
        telemetry.addData("red", red);
        telemetry.addData("blue", blue);
        telemetry.addData("green", green);
        telemetry.update();
    }

    public void raise(double power) {
        power = Math.abs(power);
        final boolean[] stopLift = new boolean[1];
        scheduleStopLiftFlag(stopLift, 2);
        while (getLiftColor() != LiftColor.RED && !stopLift[0] && AbstractOpMode.currentOpMode().opModeIsActive()) {
            lift.setPower(power);
        }
        lift.setPower(0.0);
    }
    @Deprecated //Doubt I will use this method but it may be useful in case, the TapeColorSensing Class seems more viable
    public void blueTapeListening(final double power){
        Thread blueTape = new Thread(){
            @Override
            public void run(){
                while (getLiftColor() != LiftColor.BLUE && AbstractOpMode.currentOpMode().opModeIsActive()) {
                    lift.setPower(0);
                }
                raise(power);
            }
        };
        blueTape.start();
    }

    public void lower(double power) {
        power = Math.abs(power);
        final boolean[] stopLift = new boolean[1];
        scheduleStopLiftFlag(stopLift, 2);
        while (getLiftColor() != LiftColor.BLUE && !stopLift[0] && AbstractOpMode.currentOpMode().opModeIsActive()) {
            lift.setPower(-power);
        }
        lift.setPower(0.0);
    }

    public void liftTimed(double seconds, double power) {
        lift.setPower(power);
        boolean[] stopLift = new boolean[1];
        scheduleStopLiftFlag(stopLift, seconds);
        while (!stopLift[0] && AbstractOpMode.currentOpMode().opModeIsActive()) ;
        lift.setPower(0.0);
    }

    /**
     * Assigns true to the first element of the array once the timeout period has passed.
     */
    private void scheduleStopLiftFlag(final boolean[] stopLiftFlag, double seconds) {
        TimerTask stop = new TimerTask() {
            @Override
            public void run() {
                stopLiftFlag[0] = true;
            }
        };
        timer.schedule(stop, (long) (seconds * 1000));
    }

    private LiftColor getLiftColor() {
        int r = liftSensor.red();
        int b = liftSensor.blue();
        if (r < 800 && b < 800 && r > 250 && b > 250) {
            if (r > b) {
                return LiftColor.RED;
            } else if (b > r) {
                return LiftColor.BLUE;
            }
        }
        return LiftColor.NONE;
    }

    public void liftContinuous(double power) {
        lift.setPower(power);
    }

    public void openClaw() {
        claw.setPosition(CLAW_OPEN_POS);
    }

    public void closeClaw() {
        claw.setPosition(CLAW_CLOSE_POS);

    }

    public void midClaw(){
        claw.setPosition(CLAW_MID_POS);
    }
    public boolean clawIsOpen() {
        return Math.abs(claw.getPosition() - CLAW_OPEN_POS) < CLAW_POSITION_ERROR;
    }

    public boolean clawIsMid(){
        return Math.abs(claw.getPosition() - CLAW_MID_POS) < CLAW_POSITION_ERROR;
    }

    public boolean clawIsClose(){
        return Math.abs(claw.getPosition() - CLAW_CLOSE_POS) < CLAW_POSITION_ERROR;
    }
    public Servo getClaw() {
        return claw;
    }

    public ColorSensor getLiftSensor() {
        return liftSensor;
    }

    private enum LiftColor {
        RED, BLUE, NONE
    }

}
