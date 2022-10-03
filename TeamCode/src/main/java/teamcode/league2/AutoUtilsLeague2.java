package teamcode.league2;

import java.util.Timer;
import java.util.TimerTask;

import teamcode.common.AbstractOpMode;
import teamcode.common.Utils;

public class AutoUtilsLeague2 {

    public static final double VERTICAL_SPEED = 0.7;
    public static final double LATERAL_SPEED = 0.7;
    public static final double TURN_SPEED = 0.5;
    public static final double INTAKE_LEFT_SPEED = 0.4;
    public static final double INTAKE_RIGHT_SPEED = 0.6;
    public static final double INTAKE_DURATION = 2.0;
    public static final double ARM_CLEARANCE_HEIGHT = 12;
    public static final double SCORE_HEIGHT = 6;

    public static void armScorePosition(ArmSystemLeague2 arm) {
        arm.setLiftHeight(ARM_CLEARANCE_HEIGHT, 1);
        arm.setWristPosition(true);
        Utils.sleep(1000);
        arm.setLiftHeight(SCORE_HEIGHT, 1);
    }

    public static void armRetractSequence(ArmSystemLeague2 arm) {
        arm.setLiftHeight(ARM_CLEARANCE_HEIGHT, 1);
        arm.setWristPosition(false);
        Utils.sleep(1000);
        arm.resetLift();
    }

    public static void stopIntakeWhenFull(final ArmSystemLeague2 arm) {
        final AbstractOpMode opMode = AbstractOpMode.currentOpMode();
        final Timer timer = opMode.getNewTimer();
        TimerTask stopIntake = new TimerTask() {
            @Override
            public void run() {
                while (!arm.intakeIsFull()) ;
                arm.setClawPosition(false);
                arm.intake(0);
                opMode.cancelTimer(timer);
            }
        };
        timer.schedule(stopIntake, 0);
    }

}
