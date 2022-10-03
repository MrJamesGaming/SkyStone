package teamcode.league2;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import java.util.Timer;
import java.util.TimerTask;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;
import teamcode.common.Interval;
import teamcode.common.SkyStoneConfiguration;
import teamcode.common.Utils;
import teamcode.common.Vector3D;

@Autonomous(name = "Optimal Red Side Auto")
public class OptimalRedSideAuto extends AbstractOpMode {

    private static final Interval MIDDLE_STONE_BOUNDS = new Interval(-200, -50);
    private static final Interval RIGHT_STONE_BOUNDS = new Interval(50, 200);

    private DriveSystemLeague2 drive;
    private ArmSystemLeague2 arm;
    private VisionLeague2 vision;
    private Timer timer;

    @Override
    protected void onInitialize() {
        drive = new DriveSystemLeague2(hardwareMap);
        arm = new ArmSystemLeague2(this);
        vision = new VisionLeague2(hardwareMap);
        timer = new Timer();
        setStartState();
    }

    @Override
    protected void onStart() {
        toScanningPos();
        SkyStoneConfiguration config = scan();
        Debug.log(config);
        intakeFirstStone(config);
        scoreFirstStoneAndGrabFoundation(config);
        pullFoundation();
//        intakeSecondStone(config);
//        scoreSecondStone(config);
        park();
    }

    private void setStartState() {
        arm.setClawPosition(true);
        arm.setWristPosition(false);
        arm.resetLift();
    }

    private void toScanningPos() {
        // move toward the stones
        drive.vertical(6.5, 0.6);
        drive.lateral(16.5, 0.4);
    }

    private SkyStoneConfiguration scan() {
        // allow some time for Vuforia to process image
        sleep(1000);
        Vector3D skystonePos = vision.getSkystonePosition();
        if (skystonePos != null) {
            double horizontalPos = -skystonePos.getY();
            if (RIGHT_STONE_BOUNDS.contains(horizontalPos)) {
                return SkyStoneConfiguration.THREE_SIX;
            } else if (MIDDLE_STONE_BOUNDS.contains(horizontalPos)) {
                return SkyStoneConfiguration.TWO_FIVE;
            }
        }
        return SkyStoneConfiguration.ONE_FOUR;
    }

    private void intakeFirstStone(SkyStoneConfiguration config) {
        int stone = config.getSecondStone();
        // travel toward the tape to set up for intake
        drive.vertical((6 - stone) * Utils.SKYSTONE_LENGTH_INCHES + 1.5, 1);
        drive.lateral(25, 0.7);
        arm.intake(0.8, 0.6);
        AutoUtilsLeague2.stopIntakeWhenFull(arm);
        drive.vertical(-10, 0.4);
        arm.setClawPosition(false);
        drive.lateral(-20, 0.4);
        drive.turn(3, 0.4);
    }

    private void scoreFirstStoneAndGrabFoundation(SkyStoneConfiguration config) {
        int stone = config.getSecondStone();
        double distance = 81 + (6 - stone) * Utils.SKYSTONE_LENGTH_INCHES;
        // get ready to pull foundation
        arm.grabFoundation(true);
        TimerTask scorePositionTask = new TimerTask() {
            @Override
            public void run() {
                AutoUtilsLeague2.armScorePosition(arm);
            }
        };
        timer.schedule(scorePositionTask, 1500);

        drive.vertical(-distance, 0.7);
        drive.turn(90, 0.4);
        TimerTask grabFoundationTask = new TimerTask() {
            @Override
            public void run() {
                arm.toggleFoundationGrabbers(false);
            }
        };
        timer.schedule(grabFoundationTask, 500);
        drive.vertical(12, 0.2);
        arm.setClawPosition(true);
        sleep(750);
        TimerTask retractArmTask = new TimerTask() {
            @Override
            public void run() {
                AutoUtilsLeague2.armRetractSequence(arm);
            }
        };
        // schedule so that we can proceed onto the next step without waiting for the arm
        timer.schedule(retractArmTask, 0);
    }

    private void pullFoundation() {
        // arc
        drive.customMotion(4500, -0.16, -0.65, -0.16, -0.65);
        arm.toggleFoundationGrabbers(true);
        drive.vertical(7,1);
        drive.lateral(-9, 0.4);
    }

    private void intakeSecondStone(SkyStoneConfiguration config) {
        int stone = config.getFirstStone();
        drive.vertical(-4, 0.5);
        drive.turn(20, 0.4);
        drive.vertical(50, 0.7);
//        arm.intake(0.8, 0.6);
//        drive.vertical(-10, 0.2);
//        drive.lateral(18, 0.4);
    }

    private void scoreSecondStone(SkyStoneConfiguration config) {
        // extra 5 to account for error
        drive.turn(-185, 0.6);
        int stone = config.getFirstStone();
        drive.vertical((6 - stone) * Utils.SKYSTONE_LENGTH_INCHES + 24, 1);

        TimerTask scorePositionTask = new TimerTask() {
            @Override
            public void run() {
                AutoUtilsLeague2.armScorePosition(arm);
            }
        };
        timer.schedule(scorePositionTask, 0);
        arm.setClawPosition(true);
        arm.grabFoundation(false);
        sleep(1000);
        TimerTask retractArmTask = new TimerTask() {
            @Override
            public void run() {
                AutoUtilsLeague2.armRetractSequence(arm);
            }
        };
        // schedule so that we can proceed onto the next step without waiting for the arm
        timer.schedule(retractArmTask, 0);
        drive.vertical(24, 1);
    }

    private void pushFoundation() {
        arm.grabFoundation(true);
        drive.vertical(48, 1);
        arm.grabFoundation(true);
        sleep(500);
    }

    private void park() {
        drive.vertical(-36, 1);
    }

    @Override
    protected void onStop() {
    }

}
