package teamcode.league1;


import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import teamcode.league2.HardwareComponentNamesLeague2;

public class TTArm {

    private static final double TICK_ERROR = 25.0;

    private final CRServo armLift;
    private final Servo armClaw;
    private double lastPosition = 0;

    public TTArm(HardwareMap hardwareMap) {
        armLift = hardwareMap.get(CRServo.class, HardwareComponentNamesLeague2.ARM_LIFT);
        armClaw = hardwareMap.get(Servo.class, HardwareComponentNamesLeague2.ARM_CLAW);
    }

    public void brake() {
        armLift.setPower(0.0);
    }

    public void armMove(double power) {
        armLift.setPower(power);
    }

    public void rotateClaw(double position) {
        armClaw.setPosition(position);
    }

    public double getClawPos() {
        return this.armClaw.getPosition();
    }
}
