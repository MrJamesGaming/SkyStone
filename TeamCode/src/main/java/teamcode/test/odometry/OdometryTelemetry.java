package teamcode.test.odometry;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;

@Autonomous(name = "Odometry")
public class OdometryTelemetry extends AbstractOpMode {

    SecondDraftOdometryWheels wheels;
    DriveSystemOdometryTest driveSystem;

    @Override
    protected void onInitialize() {
        driveSystem = new DriveSystemOdometryTest(this.hardwareMap);
        wheels = new SecondDraftOdometryWheels(this, new Point(50, 200), driveSystem);
    }

    @Override
    protected void onStart() {
        while(opModeIsActive()){
            Debug.log(wheels.getWheelEncoderValues());
        }
    }

    @Override
    protected void onStop() {

    }
}
