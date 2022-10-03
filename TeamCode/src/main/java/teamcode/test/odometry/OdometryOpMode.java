package teamcode.test.odometry;

import teamcode.common.AbstractOpMode;
import teamcode.common.Vector2D;
import teamcode.test.DriveSystemArcTest;
import java.math.*;
import java.util.ArrayList;

public class OdometryOpMode extends AbstractOpMode {

    private SecondDraftOdometryWheels wheels;
    private DriveSystemOdometryTest driveSystem;
    private Thread driveSystemUpdate;
    private double preferredAngle;

    ArrayList<CurvePoint> path = new ArrayList<>();


    @Override
    protected void onInitialize() {
        driveSystem = new DriveSystemOdometryTest(this.hardwareMap);
        wheels = new SecondDraftOdometryWheels(this, new Point(50, 200), driveSystem);
        preferredAngle = 90;

        driveSystemUpdate = new Thread(){
            @Override
            public void run(){
                while(opModeIsActive()){
                    driveSystem.continuous(new Vector2D(driveSystem.xPower, driveSystem.yPower), driveSystem.turnPower);
                    //while(wheels.nearTargetPoint(wheels.getPositionInPath(path)));
                }
            }
        };

    }

    @Override
    protected void onStart() {
        driveSystemUpdate.start();
        add(new CurvePoint(100.0, 300.0, 0.5, 0.5, 50.0,50.0, 50.0,1.0));
        //more addCommands
        wheels.followCurve(path, preferredAngle);
    }
    //to be addded in AbstractOpMode soon after this is all implemented
    public void add(CurvePoint point){
        path.remove(null);
        path.add(point);
        path.add(null);
        //making the end of the list always null to use as a bookmarker for when it is the last point of the path
    }

    @Override
    protected void onStop() {

    }
}
