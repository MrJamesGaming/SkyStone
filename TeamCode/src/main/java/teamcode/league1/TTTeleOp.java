package teamcode.league1;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.Timer;
import java.util.TimerTask;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;
import teamcode.common.Utils;
import teamcode.league2.DriveSystemLeague2;
import teamcode.common.Vector2D;

@Disabled
@TeleOp(name = "TT TeleOp")
public class TTTeleOp extends AbstractOpMode {

    private static final double TURN_SPEED_MODIFIER = 0.6;
    private static final double STRAIGHT_SPEED_MODIFIER = 0.75;
    private static final double CLAW_COOLDOWN_SECONDS = 0.5;
    private static final int SCORING_TICKS = 2100;
    private static final long CLOSE_CLAW_DELAY = 1000;
    private static final long OPEN_CLAW_DELAY = 2500;

    private DriveSystemLeague2 driveSystem;
    private MetaTTArm arm;
    private Timer timer1;
    private Timer timer2;
    private Timer timer3;

    private boolean canUseClaw;
    private boolean armIsExtended;

    @Override
    protected void onInitialize() {
        driveSystem = new DriveSystemLeague2(hardwareMap);
        arm = new MetaTTArm(this);
        timer2 = getNewTimer();
        timer3 = getNewTimer();
        canUseClaw = true;
        armIsExtended = false;
    }

    @Override
    protected void onStart() {
        new ArmInputListener().start();
        while (opModeIsActive()) {
            driveUpdate();
        }
    }

    protected void onStop() {
    }

    private void driveUpdate() {
        double vertical = gamepad1.right_stick_y * STRAIGHT_SPEED_MODIFIER;
        double horizontal = gamepad1.right_stick_x;
        double turn = -gamepad1.left_stick_x * TURN_SPEED_MODIFIER;
        if (gamepad1.right_bumper) {
            vertical = vertical / STRAIGHT_SPEED_MODIFIER;
            Vector2D velocity = new Vector2D(horizontal, vertical);
            driveSystem.continuous(velocity, turn);
        } else {
            Vector2D velocity = new Vector2D(horizontal, vertical);
            driveSystem.continuous(velocity, turn);
        }
    }

    private class ArmInputListener extends Thread {

        @Override
        public void run() {
            while (opModeIsActive()) {
                armUpdate();
            }
        }

        private void armUpdate() {
            Debug.log("update");
            // sequences
            if (gamepad1.left_trigger > 0) {
                arm.intake(gamepad1.left_trigger);
            } else if (gamepad1.right_trigger > 0) {
                extendArmSequence();
                // left bumper = abort sequence
                while (!gamepad1.left_bumper && !armIsExtended);
                cancelTimer(timer2);
                timer2 = getNewTimer();
            } else if (gamepad1.x) {
                if (armIsExtended) {
                    retractArmSequence();
                    Debug.log("entering loop");
                    while (!gamepad1.left_bumper && armIsExtended);
                    Debug.log("exiting loop");
                    cancelTimer(timer2);
                    timer2 = getNewTimer();
                } else if (canUseClaw) {
                    arm.setClawPosition(!arm.clawIsOpen());
                    clawCooldown();
                }
            }

            // setLiftHeight
            if (gamepad1.b) {
                arm.lift(SCORING_TICKS - arm.getLiftHeight(), 1);
            } else if (gamepad1.dpad_up) {
                arm.liftContinuously(0.75);
            } else if (gamepad1.dpad_down) {
                arm.liftContinuously(-0.75);
            }

            // wrist
            if (gamepad1.dpad_right) {
                arm.setWristPosition(true);
            } else if (gamepad1.dpad_left) {
                arm.setWristPosition(false);
            }
        }

        private void extendArmSequence() {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    closeClaw();
                    Debug.log("Going up");
                    arm.lift(SCORING_TICKS, 1.0);
                    Debug.log("Swinging out");
                    arm.extendWristIncrementally();
                    Debug.log("Going down");
                    arm.lift(-SCORING_TICKS, 1.0);
                    openClaw();
                    Debug.log("extended = true");
                    armIsExtended = true;
                }
            };
            timer2.schedule(task, 0);
        }

        private void retractArmSequence() {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    int t = 1200;
                    // retract
                    Debug.log("Going up");
                    liftArm(t, 1.0);
                    Utils.sleep(500);
                    Debug.log("Swinging in");
                    arm.setWristPosition(false);
                    Debug.log("Going down");
                    Utils.sleep(500);
                    liftArm(-t, 0.7);
                    Debug.log("extended = false");
                    armIsExtended = false;
                }
            };
            timer2.schedule(task, 0);
        }


        private void closeClaw() {
            arm.setClawPosition(false);
            Utils.sleep(CLOSE_CLAW_DELAY);
        }

        private void openClaw() {
            arm.setClawPosition(true);
            Utils.sleep(OPEN_CLAW_DELAY);
        }

        private void liftArm(final int ticks, final double power) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    arm.lift(ticks, power);
                }
            };
            timer3.schedule(task, 0);
        }

        private void clawCooldown() {
            canUseClaw = false;
            TimerTask enableClaw = new TimerTask() {
                @Override
                public void run() {
                    canUseClaw = true;
                }
            };
            timer1.schedule(enableClaw, (long) (CLAW_COOLDOWN_SECONDS * 1000));
        }

    }

}
