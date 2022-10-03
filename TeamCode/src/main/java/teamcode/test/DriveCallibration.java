package teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import java.util.TimerTask;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;
import teamcode.league2.DriveSystemLeague2;

@Autonomous(name = "Drive Callibration")
public class DriveCallibration extends AbstractOpMode {

    DriveSystemLeague2 driveSystem;

    @Override
    protected void onInitialize() {
        driveSystem = new DriveSystemLeague2(hardwareMap);
    }

    @Override
    protected void onStart() {
        debug();
        driveSystem.vertical(48, 0.4);
        driveSystem.vertical(-48, 0.4);
        driveSystem.lateral(24, 0.4);
        driveSystem.lateral(-24, 0.4);
        driveSystem.turn(360, 0.4);
    }

    private void debug() {
        TimerTask debug = new TimerTask() {
            @Override
            public void run() {
                while (opModeIsActive()) {
                    DcMotor[] motors = driveSystem.getMotors();
                    DcMotor frontLeft = motors[0];
                    DcMotor frontRight = motors[1];
                    DcMotor backLeft = motors[2];
                    DcMotor backRight = motors[3];

                    Debug.log("front left: " + frontLeft.getCurrentPosition() + " / " + frontLeft.getTargetPosition());
                    Debug.log("front right: " + frontRight.getCurrentPosition() + " / " + frontRight.getTargetPosition());
                    Debug.log("back left: " + backLeft.getCurrentPosition() + " / " + backLeft.getTargetPosition());
                    Debug.log("back right: " + backRight.getCurrentPosition() + " / " + backRight.getTargetPosition());
                }
            }
        };
        getNewTimer().schedule(debug, 0);
    }

    @Override
    protected void onStop() {

    }

}
