package teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import teamcode.common.AbstractOpMode;
import teamcode.common.BoundingBox2D;
import teamcode.common.Debug;
import teamcode.common.SkyStoneConfiguration;
import teamcode.common.Vector2D;
import teamcode.common.Vector3D;
import teamcode.league2.VisionLeague2;

@Autonomous(name = "Vision Calibrator")
public class VisionCalibrator extends AbstractOpMode {

    private static final BoundingBox2D MIDDLE_STONE_BOUNDS = new BoundingBox2D(-10, 0, 110, 0);
    private static final BoundingBox2D RIGHT_STONE_BOUNDS = new BoundingBox2D(120, 0, 500, 0);

    private VisionLeague2 vision;

    @Override
    protected void onInitialize() {
        vision = new VisionLeague2(hardwareMap);
    }

    @Override
    protected void onStart() {
        while (opModeIsActive()) {
            // forward 12
            Vector3D pos = vision.getSkystonePosition();
            if (pos != null) {
                double horizontalDistanceFromRobot = -pos.getY();
                Debug.log("skystone pos: " + horizontalDistanceFromRobot);
//            SkyStoneConfiguration config = determineSkystoneConfig(horizontalDistanceFromRobot);
//            Debug.log(config);
            }
        }
    }

    private SkyStoneConfiguration determineSkystoneConfig(double horizontalDistanceFromRobot) {
        Vector2D visionPos = new Vector2D(horizontalDistanceFromRobot, 0);
        if (MIDDLE_STONE_BOUNDS.contains(visionPos)) {
            return SkyStoneConfiguration.TWO_FIVE;
        } else if (RIGHT_STONE_BOUNDS.contains(visionPos)) {
            return SkyStoneConfiguration.ONE_FOUR;
        } else {
            return SkyStoneConfiguration.THREE_SIX;
        }
    }

    @Override
    protected void onStop() {
    }

}
