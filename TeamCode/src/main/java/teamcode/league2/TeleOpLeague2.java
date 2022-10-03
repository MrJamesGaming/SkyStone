package teamcode.league2;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.Timer;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;
import teamcode.common.Utils;
import teamcode.common.Vector2D;

@TeleOp(name = "Tele Op")
public class TeleOpLeague2 extends AbstractOpMode {

    private static final double MAX_LIFT_HEIGHT_INCHES = 21;
    private static final double LIFT_SCORE_STEP_INCHES = 4.25;
    private static final double LIFT_INITIAL_SCORE_HEIGHT = 4.25;
    private static final double LIFT_HOME_CLEARANCE_HEIGHT_INCHES_TO_SCORING = 12.0;
    private static final double LIFT_HOME_CLEARANCE_HEIGHT_INCHES_TO_HOME = 9.0;
    private static final double LIFT_MANUAL_STEP_INCHES = 1;
    private static final int MAX_SCORE_LEVEL = 5;

    private static final long CLOSE_CLAW_DELAY = 1000;
    private static final long OPEN_CLAW_DELAY = 1000;

    private static final double TURN_SPEED_MODIFIER = 0.3;
    private static final double VERTICAL_SPEED_MODIFIER = 0.3;
    private static final double LATERAL_SPEED_MODIFIER = 0.3;

    private int scoreLevel;

    private DriveSystemLeague2 driveSystem;
    private ArmSystemLeague2 arm;
    private Timer timer;

    private StoneBoxState stoneBoxState;
    private WristState wristState;
    private ClawState clawState;
    private IntakeState intakeState;
    private DriveMode driveMode;

    private int scoreLevel;
    private boolean colorSensorIsActive;
    private boolean reverseDrive;

    @Override
    protected void onInitialize() {
        driveSystem = new DriveSystemLeague2(hardwareMap);
        arm = new ArmSystemLeague2(this);
        timer = getNewTimer();
    }

    @Override
    protected void onStart() {
        setStartState();

        new StoneBoxControl().start();
        new IntakeControl().start();
        new ScoreControl().start();
        new DriveControl().start();

        while (opModeIsActive()) ;
    }

    protected void onStop() {
    }

    private void setStartState() {
        // reset the current skystone score level height
        scoreLevel = 1;

        // Open the claw
        openClaw(false);

        // Retract arm
        retractArm(false);

        arm.resetLift();

        arm.grabFoundation(false);
        // Turn off the intake
        intakeOff();

        // The stone box should be empty
        stoneBoxState = StoneBoxState.EMPTY;
        driveMode = DriveMode.MANUAL;
        colorSensorIsActive = true;
        reverseDrive = false;
    }

    private enum StoneBoxState {
        EMPTY,
        FULL
    }

    private enum WristState {
        RETRACTED,
        EXTENDED
    }

    private enum ClawState {
        OPEN,
        CLOSED
    }

    private enum IntakeState {
        INGRESS,
        EGRESS,
        OFF
    }

    private void intakeOff() {
        arm.intake(0.0);
        intakeState = IntakeState.OFF;
    }

    private void intake() {
        arm.intake(0.5);
        intakeState = IntakeState.INGRESS;
    }

    private void outtake(double power) {
        arm.intake(-power);
        intakeState = IntakeState.EGRESS;
    }

    private void closeClaw(boolean wait) {
        arm.setClawPosition(false);
        if (wait) {
            Utils.sleep(CLOSE_CLAW_DELAY);
        }
        clawState = ClawState.CLOSED;
    }

    private void openClaw(boolean wait) {
        arm.setClawPosition(true);
        if (wait) {
            Utils.sleep(OPEN_CLAW_DELAY);
        }
        clawState = ClawState.OPEN;
    }

    private void extendWrist() {
        arm.extendWristIncrementally();
        wristState = WristState.EXTENDED;
    }

    private void retractArm(boolean wait) {
        arm.setWristPosition(false);
        if (wait) {
            Utils.sleep(500);
        }
        wristState = WristState.RETRACTED;
    }

    private class DriveControl extends Thread {
        @Override
        public void run() {
            boolean yDown = false;
            while (opModeIsActive()) {
                double vertical = gamepad1.right_stick_y;
                double lateral = gamepad1.right_stick_x;
                double turn = gamepad1.left_stick_x;
                if (gamepad1.left_trigger == 0) {
                    vertical *= VERTICAL_SPEED_MODIFIER;
                    lateral *= LATERAL_SPEED_MODIFIER;
                    turn *= TURN_SPEED_MODIFIER;
                }
                Vector2D velocity = new Vector2D(lateral, vertical);
                if (gamepad2.y && !yDown) {
                    reverseDrive = !reverseDrive;
                    yDown = true;
                } else {
                    yDown = false;
                }
                if (reverseDrive) {
                    velocity.multiply(-1);
                }
                if (clawState == ClawState.CLOSED) {
                    vertical *= -1;
                    lateral *= -1;
                }
                Vector2D velocity = new Vector2D(lateral, vertical);
                driveSystem.continuous(velocity, turn);
                if ((gamepad2.x || gamepad1.dpad_left) && canUseGrabbers) {
                    arm.toggleFoundationGrabbers(!arm.foundationGrabbersAreOpen());
                    grabberCooldown();
                }
            }
        }
    }

    private class ScoreControl extends Thread {
        @Override
        public void run() {
            boolean yDown = false;
            while (opModeIsActive()) {
                if (gamepad1.dpad_left) {
                    // extend manually
                    extendWrist();
                } else if (gamepad1.dpad_right) {
                    // retract manually
                    retractWrist(true);
                } else if (gamepad1.dpad_up || gamepad2.dpad_right) {
                    // move setLiftHeight up
                    double inches = Math.min(arm.getLiftHeight() + LIFT_MANUAL_STEP_INCHES, MAX_LIFT_HEIGHT_INCHES);
                    Debug.log("Lift up to: " + inches);
                    arm.setLiftHeight(inches, 1.0);
                    if (gamepad2.dpad_right) {
                        arm.resetLift();
                    }
                } else if (gamepad1.dpad_down || gamepad2.dpad_left) {
                    // move lift down
                    double inches = Math.max(arm.getLiftHeight() - LIFT_MANUAL_STEP_INCHES, 0);
                    Debug.log("Lift down to: " + inches);
                    arm.setLiftHeight(inches, 1.0);
                    if (gamepad2.dpad_left) {
                        arm.resetLift();
                    }
                } else if (gamepad1.b) {
                    // first height
                    scoreLevel = 1;
                    Debug.log("Reset height");
                }
                if (gamepad1.right_bumper) {
                    if (!yDown) {
                        scorePosition();
                    }
                    yDown = true;
                } else {
                    yDown = false;
                }
                if (gamepad1.x) {
                    if(arm.intakeIsFull()){
                        arm.setClawPosition(true);
                        driveSystem.vertical(-10, 0.3);
                        homePosition();
                    } else {
                        toggleClaw();
                    }
                } else if (stoneBoxState == StoneBoxState.FULL && clawState == ClawState.OPEN) {
                    closeClaw(false);
                }
            }
        }

        private void scorePosition() {
            if (wristState == WristState.EXTENDED) {
                return;
            }
            double newHeight = scoreLevel * LIFT_SCORE_STEP_INCHES;
            newHeight = Math.min(newHeight, MAX_LIFT_HEIGHT_INCHES); // janky solution to be removed later
            if (scoreLevel == 1) {
                newHeight = 7.2;
            }
            Debug.log("Lift to level " + scoreLevel + ": " + newHeight + " inches");

            if (newHeight < LIFT_HOME_CLEARANCE_HEIGHT_INCHES_TO_SCORING) {
                arm.setLiftHeight(LIFT_HOME_CLEARANCE_HEIGHT_INCHES_TO_SCORING, 1.0);
                extendWrist();
                arm.setLiftHeight(newHeight, 1.0);
            } else {
                arm.setLiftHeight(newHeight, 1.0);
                extendWrist();
            }
            scoreLevel++;
            if (scoreLevel > MAX_SCORE_LEVEL) {
                scoreLevel = MAX_SCORE_LEVEL;
            }
        }

        private void homePosition() {
            if (wristState == WristState.EXTENDED) {
                double height = arm.getLiftHeight();
                if (height < LIFT_HOME_CLEARANCE_HEIGHT_INCHES_TO_HOME) {
                    Debug.log(3);
                    arm.setLiftHeight(LIFT_HOME_CLEARANCE_HEIGHT_INCHES_TO_HOME, 1.0);
                }
                retractArm(false);
            }

            openClaw(false);
            Utils.sleep(500);
            arm.resetLift();
        }

        private void toggleClaw() {
            if (clawState == ClawState.OPEN) {
                closeClaw(true);
            } else {
                openClaw(true);
            }
        }
    }

    private class IntakeControl extends Thread {
        @Override
        public void run() {
            while (opModeIsActive()) {
                if (gamepad1.y || stoneBoxState == StoneBoxState.FULL) {
                    // Turn off the intake when left bumper is pressed
                    // or stone box is full
                    intakeOff();
                } else if (gamepad1.right_trigger > 0) {
                    // Turn on the intake
                    intake();
                } else if (gamepad1.left_bumper) {
                    colorSensorIsActive = false;
                    // Turn on the outtake
                    openClaw(false);
                    outtake();
                    TimerTask enableColorSensor = new TimerTask() {
                        @Override
                        public void run() {
                            colorSensorIsActive = true;
                        }
                    };
                    timer.schedule(enableColorSensor, 2000);
                }
            }
        }
    }

    private class StoneBoxControl extends Thread {
        @Override
        public void run() {
            while (opModeIsActive()) {
                while (gamepad2.left_bumper) {
                    colorSensorIsActive = false;
                }
                colorSensorIsActive = true;
                if (arm.intakeIsFull() && colorSensorIsActive) {
                    stoneBoxState = StoneBoxState.FULL;
                } else {
                    stoneBoxState = StoneBoxState.EMPTY;
                }
            }
        }
    }

}
