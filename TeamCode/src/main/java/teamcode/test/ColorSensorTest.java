package teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.common.AbstractOpMode;
import teamcode.league2.ArmSystemLeague2;

@TeleOp(name = "Color Sensor Test")
public class ColorSensorTest extends AbstractOpMode {

    ArmSystemLeague2 arm;

    @Override
    protected void onInitialize() {

        arm = new ArmSystemLeague2(this);
    }

    @Override
    protected void onStart() {
        while(opModeIsActive()){
            arm.intakeIsFull();
        }
    }

    @Override
    protected void onStop() {

    }
}
