package teamcode.league1;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.List;

import teamcode.common.BoundingBox2D;
import teamcode.common.SkyStoneConfiguration;
import teamcode.league2.DriveSystemLeague2;
import teamcode.common.AbstractOpMode;
import teamcode.common.Vector2D;

@Disabled
@Autonomous(name = "Red Double Stone")
public class TTAutoRedDoubleStone extends AbstractOpMode {

    /**
     * A bounding box which is used to see if a skystone is in the center of the camera's view.
     */
    private static final BoundingBox2D SKYSTONE_BOUNDING_BOX = new BoundingBox2D(0, 0, 720, 1280);

    private DriveSystemLeague2 driveSystem;
    private League1TTArm arm;
    private TTVision vision;
    private SkyStoneConfiguration skyStoneConfig;
    private boolean hasDroppedFirstStone;

    @Override
    protected void onInitialize() {
        driveSystem = new DriveSystemLeague2(hardwareMap);
        arm = new League1TTArm(hardwareMap);
        vision = new TTVision(hardwareMap);
        vision.enable();
    }

    @Override
    protected void onStart() {
        initArm();
        skyStoneConfig = determineSkyStoneConfig();
        int firstStop = skyStoneConfig.getSecondStone();
        int secondStop = skyStoneConfig == SkyStoneConfiguration.ONE_FOUR ? 6 : skyStoneConfig.getFirstStone();
        grabStone();
        stoneToFoundation(firstStop);
        foundationToStone(secondStop);
        grabStone();
        stoneToFoundation(secondStop);
        driveSystem.vertical(-27, 1);
    }


    /**
     * Opens the claw and lowers the arm for starting position.
     */
    private void initArm() {
        arm.openClaw();
        arm.lower(1);
    }

    /**
     * Approaches the second SkyStone to determine the configuration of the SkyStones.
     */
    private SkyStoneConfiguration determineSkyStoneConfig() {
        driveSystem.vertical(22.5, 0.5);
        driveSystem.lateral(-2, 0.3);
        sleep(1000);
        if (seesSkyStone()) {
            driveSystem.lateral(2, 0.3);
            return SkyStoneConfiguration.THREE_SIX;
        }
        driveSystem.lateral(-6.5, 0.3);
        sleep(1000);
        if (seesSkyStone()) {
            return SkyStoneConfiguration.TWO_FIVE;
        }
        driveSystem.lateral(-8, 1);
        return SkyStoneConfiguration.ONE_FOUR;
    }

    /**
     * Returns true if the skystone is in the center of the camera's field of view.
     */
    private boolean seesSkyStone() {
        List<Recognition> recognitions = vision.getRecognitions();
        for (Recognition recognition : recognitions) {
            if (recognition.getLabel().equals(TTVision.LABEL_SKYSTONE)) {
                Vector2D center = TTVision.getCenter(recognition);
                if (SKYSTONE_BOUNDING_BOX.contains(center)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Robot grabs the stone in front of it and backs out.
     */
    private void grabStone() {
        if (hasDroppedFirstStone) {
            driveSystem.vertical(16.5, 1);
        } else {
            driveSystem.vertical(13.5, 1);
        }
        arm.closeClaw();
        sleep(700);
        arm.liftTimed(0.15, 0.3);
        driveSystem.vertical(-16.5, 1);
    }

    /**
     * Robot drives towards the foundation from the stone area, turns to face it, and releases a
     * stone onto it. Assumes the foundation has been moved.
     *
     * @param stone the position of the stone that was grabbed in the stone area
     */
    private void stoneToFoundation(int stone) {
        driveSystem.turn(90, 0.6);
        driveSystem.vertical(90 - stone * 8, 1);
        arm.liftTimed(0.25, 1);
        driveSystem.vertical(16, 1);
        arm.openClaw();
        driveSystem.vertical(-5, 1);
        arm.lower(1);
        hasDroppedFirstStone = true;
    }

    /**
     * Robot approaches stone area from foundation to target a stone.
     *
     * @param stone the position of the stone to be targeted in the stone area.
     */
    private void foundationToStone(int stone) {
        driveSystem.vertical(-100 + stone * 8, 1);
        driveSystem.turn(-90, 0.6);
    }


    @Override
    protected void onStop() {
        vision.disable();
    }

}
