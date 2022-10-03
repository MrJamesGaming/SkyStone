package teamcode.test;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.List;

import teamcode.common.Vector2D;
import teamcode.league2.DriveSystemLeague2;


public class OdometryWheels {
    private static final double DEGREE_ERROR = 3.0;
    private DcMotor XWheel;
    private DcMotor YWheel;
    private List<Vector2D> compositeComponents;
    private List<Double> magnitudes;
    private List<Vector2D> path;
    private long apex;
    private double distanceX;
    private double distanceY;
    private int compositeTickX;
    private int compositeTickY;
    private final double DISTANCE_APART_INCHES = 1;
    private final DriveSystemLeague2 driveSystem;



    /* Ok so just a notepad for myself on Odometry
    youve got a composite of Vectors and an ideal composite path

    Interpret each vector as a line and find a slope, use that to determine points and thus distance away from the ideal
    Hopefully future me knows what the heck I meant
     */

    public OdometryWheels(HardwareMap hardwareMap, List<Vector2D> path) {
        XWheel = hardwareMap.get(DcMotor.class, "Xwheel Odometry");
        YWheel = hardwareMap.get(DcMotor.class, "YWheel Odometry");
        apex = System.currentTimeMillis();
        this.path = path;
        driveSystem = new DriveSystemLeague2(hardwareMap);
        XWheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        YWheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        XWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        YWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    }

    public void updateComposite() {
        int currentTickX = XWheel.getCurrentPosition();
        int currentTickY = YWheel.getCurrentPosition();
        compositeTickX += currentTickX;
        compositeTickY += currentTickY;
        long currentTime = System.currentTimeMillis() - apex;
        double xVelocity = (((double) currentTickX) / ((double) currentTime));
        double yVelocity = (((double) currentTickY) / ((double) currentTime));
        Vector2D velocity = new Vector2D(xVelocity, yVelocity);
        compositeComponents.add(velocity);
        comparePaths();
    }


    private void comparePaths() {
        for (int i = 0; i < compositeComponents.size(); i++) {
            if (compositeComponents.get(i).getDirection() + DEGREE_ERROR == path.get(i).getDirection() || compositeComponents.get(i).getDirection() - DEGREE_ERROR == path.get(i).getDirection()) {
                //good case, nothing is necessary
            } else {
                double direction = path.get(i).angleBetween(compositeComponents.get(i));
                double distance = compositeComponents.get(i).magnitude();
                driveSystem.turn(direction, 1);
                //can change speed if necessary

                driveSystem.vertical(-distance, 1);
                //bad case, need to self correct.
            }
        }
    }

}
