package teamcode.test;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import teamcode.common.AbstractOpMode;
import teamcode.league2.DriveSystemLeague2;

@Autonomous(name = "Rotation Calibration")
public class RotationCalibration extends AbstractOpMode {

    public DriveSystemLeague2 driveSystem;


    @Override
    protected void onInitialize() {
        driveSystem = new DriveSystemLeague2(hardwareMap);
    }

    @Override
    protected void onStart() {
        driveSystem.turn(360 * 6, 0.5);
    }

    @Override
    protected void onStop() {

    }

}
