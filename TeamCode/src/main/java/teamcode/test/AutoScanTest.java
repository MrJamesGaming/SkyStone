package teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;
import teamcode.common.Interval;
import teamcode.common.SkyStoneConfiguration;
import teamcode.common.Vector2D;
import teamcode.common.Vector3D;
import teamcode.league2.DriveSystemLeague2;
import teamcode.league2.VisionLeague2;

@Autonomous(name = "Auto Scan Test")
public class AutoScanTest extends AbstractOpMode {

    private static final Interval MID_RED = new Interval(-200, -50);
    private static final Interval RIGHT_RED = new Interval(50, 200);

    private VisionLeague2 vision;
    private DriveSystemLeague2 drive;

    @Override
    protected void onInitialize() {
        vision = new VisionLeague2(hardwareMap);
        drive = new DriveSystemLeague2(hardwareMap);
    }
    @Override
    protected void onStart() {
        scanRedSide();
    }

    private void scanRedSide() {
        drive.lateral(16,0.4);
        sleep(500);
        Vector3D skystonePos = vision.getSkystonePosition();
        SkyStoneConfiguration config = determineConfigRedSide(skystonePos);
        Debug.log(config);
        while (opModeIsActive()) ;
    }

    private SkyStoneConfiguration determineConfigRedSide(Vector3D skystonePos) {
        if (skystonePos != null) {
            double horizontalDistanceFromRobot = -skystonePos.getY();
            if (MID_RED.contains(horizontalDistanceFromRobot)) {
                return SkyStoneConfiguration.TWO_FIVE;
            } else if (RIGHT_RED.contains(horizontalDistanceFromRobot)) {
                return SkyStoneConfiguration.THREE_SIX;
            }
        }
        return SkyStoneConfiguration.ONE_FOUR;
    }

    @Override
    protected void onStop() {
    }

}
