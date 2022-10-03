package teamcode.test.odometry;



import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import java.lang.reflect.Array;
import java.util.ArrayList;

import teamcode.common.AbstractOpMode;
import teamcode.common.Vector2D;

import static java.lang.Math.*;

public class SecondDraftOdometryWheels {
    private static final double DISTANCE_TOLERANCE = 10;
    private static final double Y_POINT_TOLERANCE_INTERSECT = 0.003;
    private static final double X_POINT_TOLERANCE_INTERSECT = 0.003;
    private static final double ODOMETRY_WHEELS_TICKS_TO_INCHES = 23;
    //arbetrary value
    private static final double CENTIMETERS_TO_INCHES = 1/2.54;

    private final DcMotor xWheelLeft;
    private final DcMotor xWheelRight;
    private final DcMotor yWheel;
    private final double apex;
    private Vector2D current;
    private final double DISTANCE_TOLERANCE_X = 5;
    private final double DISTANCE_TOLERANCE_Y = 5;

    private int encoderValueX;
    private int previousEncoderValueX;

    private int encoderValueY;
    private int previousEncoderValueY;

    private Point globalRobotPosition;
    private double globalDirection;
    //Inches

    private DriveSystemOdometryTest driveSystem;

    private Point perpendicularPointToRobot;
    //for generating


    public String getWheelEncoderValues(){
        return "leftX: " + xWheelLeft.getCurrentPosition() + "\n" + "rightX: " + xWheelRight.getCurrentPosition() + "\n" + "Y: " + yWheel.getCurrentPosition();
    }

    /**
     * Creates the Odometry Wheels Object
     * @param opMode
     * @param globalRobotPosition starting position of the robot at the beginning of the OpMode
     * @param driveSystem the robots driveSystem, allowing us access to the speed fields and full implementation of the pure pursuit algorithm
     */
    public SecondDraftOdometryWheels(AbstractOpMode opMode, Point globalRobotPosition, DriveSystemOdometryTest driveSystem){
        this.driveSystem = driveSystem;
        HardwareMap hardwareMap = opMode.hardwareMap;
        xWheelLeft = hardwareMap.get(DcMotor.class, "Odometry Left X Wheel");
        xWheelRight = hardwareMap.get(DcMotor.class, "Odometry Right X Wheel");
        yWheel = hardwareMap.get(DcMotor.class, "Odometry Y Wheel");
        this.globalRobotPosition = globalRobotPosition;
        globalDirection = 0;
        apex = opMode.getRuntime();
        new Thread(){
            @Override
            public void run(){
                while(AbstractOpMode.currentOpMode().opModeIsActive()){
                    updateGlobalPosistion();
                }
            }
        }.start();

    }

    private void updateGlobalPosistion(){
        int distanceTravelledX = encoderValueX - previousEncoderValueX;
        int distanceTravelledY = encoderValueY - previousEncoderValueY;
        current = new Vector2D(distanceTravelledX, distanceTravelledY);
        double direction = current.getDirection();
        previousEncoderValueX = encoderValueX;
        previousEncoderValueY = encoderValueY;
    }

    public CurvePoint getFollowPointPath(ArrayList<CurvePoint> path, Point robotLocation, double followRadius){
        CurvePoint followMe = new CurvePoint(path.get(0));
        for(int i = 0; i < path.size() - 1; i++){
            CurvePoint startLine = path.get(i);
            CurvePoint endLine = path.get(i + 1);
            ArrayList<Point> intersections = lineCircleIntersection(robotLocation, followRadius,startLine.toPoint(), endLine.toPoint());
            double closestAngle = Double.MAX_VALUE;
            for(Point currentIntersection: intersections) {
                double angle = atan2(currentIntersection.y - globalRobotPosition.y, currentIntersection.x - globalRobotPosition.x) * 180 / PI;
                double deltaAngle = abs(angleWrapper(angle - globalDirection));
                if(deltaAngle < closestAngle){
                    closestAngle = deltaAngle;
                    followMe.setPoint(currentIntersection);
                }

            }
        }
        return followMe;
    }
    //TODO implement a method to stop oscillation at the end of the path
    //TODO using a list of path points, figure out where I am in the path using perpendicular lines. Timestamp 15:00 in the GF tutorial

    public int getPositionInPath(ArrayList<CurvePoint> pathPoints){

        for(int i = 0; i < pathPoints.size() - 1; i++){
            CurvePoint current = pathPoints.get(i);
            if(i + 1 < pathPoints.size()) {
                CurvePoint next = pathPoints.get(i + 1);
                Point nextPoint = next.toPoint();
                Point currentPoint = current.toPoint();
                double slope = currentPoint.slope(nextPoint);
                double perpendicularSlope = 1.0 / slope;
                Point perpendicularPoint = new Point(globalRobotPosition.x + 1, perpendicularSlope + globalRobotPosition.y);


            }else{
                if(robotIsNearPoint(current)) {
                    //return current;
                }else{
                    //return globalRobotPosition;
                }
            }


        }
        return 0;
    }

    private boolean robotIsNearPoint(CurvePoint current) {
        Point currentPoint = current.toPoint();
        return abs(currentPoint.x - globalRobotPosition.x) < DISTANCE_TOLERANCE_X && abs(currentPoint.y - globalRobotPosition.y) < DISTANCE_TOLERANCE_Y;
    }

    private boolean isLastPoint(ArrayList<CurvePoint> path, CurvePoint current){
        return path.indexOf(current) == path.size() -1;
    }

    //To be implemented into the new Drive System for league 3
    public void followCurve(ArrayList<CurvePoint> allPoints, double followAngle){
        for(int i = 0; i < allPoints.size(); i++) {
            CurvePoint followMe = getFollowPointPath(allPoints, globalRobotPosition, allPoints.get(0).followDistance);
            //assumes all followDistance is the same, gotta make a findPositionInPath method
            goToPosition(followMe.x, followMe.y, followMe.moveSpeed, followAngle, followMe.turnSpeed);
        }
    }

    /**
     * moves to any point on the field with a defined coordinate plane
     * @param x The X coordinate to be moved to
     * @param y the Y coordinate to be moved to
     * @param power full power the robot should be at
     * @param preferredAngle angle the robot should be moving at
     */
    public void goToPosition(double x, double y, double power, double preferredAngle, double turnPower){
        double deltaX = x - globalRobotPosition.x;
        double deltaY = y - globalRobotPosition.y;
        double distanceTravelled = pow(deltaX, 2) + pow(deltaY, 2);
        //change in robots position throughout this function
        double absoluteAngle = atan2(deltaY, deltaX);
        double relativeAngle = angleWrapper(absoluteAngle - globalDirection);
        //change in the robots position
        double relativeDistanceX = distanceTravelled * cos(relativeAngle);
        double relativeDistanceY = distanceTravelled * sin(relativeAngle);
        //relative distance the robot is travelling
        double powerX = relativeDistanceX / (abs(relativeDistanceX) + Math.abs(relativeDistanceY));
        double powerY = relativeDistanceY / (abs(relativeDistanceX) + Math.abs(relativeDistanceY));
        //power calculations
        driveSystem.xPower = powerX * power;
        driveSystem.yPower = powerY * power;
        //assigning power to driveTrain
        double relativeTurnAngle = relativeAngle - 180 + preferredAngle;
        driveSystem.turnPower = Range.clip(relativeTurnAngle / 30, -1, 1) * turnPower;
        if(distanceTravelled < DISTANCE_TOLERANCE){
            driveSystem.turnPower = 0;
        }



    }

    /**
     * Asserts that the angle stays between 0 and 360
     * @param angle passed in angle
     * @return
     */
    private double angleWrapper(double angle){
        while(angle < -180){
            angle += 360;
        }

        while(angle > 180){
            angle -= 360;
        }

        return angle;
    }

    private ArrayList<Point> lineCircleIntersection(Point circleCenter, double radius, Point linePoint1, Point linePoint2){
        //(mx+b)^2 = r^2 + x^2
        if(Math.abs(linePoint1.y - linePoint2.y) < Y_POINT_TOLERANCE_INTERSECT){
            linePoint1.y = linePoint2.y + Y_POINT_TOLERANCE_INTERSECT;
        }
        if(Math.abs(linePoint1.x - linePoint2.x) < X_POINT_TOLERANCE_INTERSECT){
            linePoint1.x = linePoint2.x + X_POINT_TOLERANCE_INTERSECT;
        }

        double slope1 = linePoint2.slope(linePoint1);

        double x1 = linePoint1.x - circleCenter.x;
        double y1 = linePoint1.y - circleCenter.y;
        //Defines everything in terms of the Circle center by offsetting it which is added back later, this simplifies the math

        double a =  1.0 + pow(slope1, 2);
        double b = (2 * slope1 * y1) - (2 * pow(slope1,2) * x1);
        double c = (pow(slope1,2) * pow(x1, 2)) - (2 * slope1 * y1 * x1) + pow(y1,2) - pow(radius, 2);
        ArrayList<Point> allPoints = new ArrayList();
        try{
            double discriminant = sqrt((pow(b,2) - 4 * a * c));
            double xRoot1 = (-b + discriminant) / (2 *a);
            double xRoot2 = (-b - discriminant) / (2 *a);

            double yRoot1 = slope1 * (xRoot1 - x1) + y1;
            double yRoot2 = slope1 * (xRoot2 - x1) + y1;
            xRoot1 += circleCenter.x;
            yRoot1 += circleCenter.y;
            xRoot2 += circleCenter.x;
            yRoot2 += circleCenter.y;

            double minX = linePoint1.x < linePoint2.x ? linePoint1.x: linePoint2.x;
            double maxX = linePoint1.x > linePoint2.x ? linePoint1.x: linePoint2.x;
            if(xRoot1 > minX && xRoot1 < maxX){
                allPoints.add(new Point(xRoot1, yRoot1));
            }
            if(xRoot2 > minX && xRoot2 < maxX){
                allPoints.add(new Point(xRoot2, yRoot2));
            }


        }catch(ArithmeticException e){
        }
        return allPoints;

    }

    public boolean nearTargetPoint(CurvePoint point) {
        if(point.x == globalRobotPosition.x && point.y == globalRobotPosition.y){
            return true;
        }
        return false;
    }


    /*
    input a ideal robot path, omega(w) and velocity V
    gives ideal odometry wheel values, convert to ticks using a tick constant inverted
    met by drive wheel movement unitl Odometry wheel satisfied.  while(!odometryWheel.nearTarget());
    Drive wheel directions and velocities will give us a movement in some x y w direction
    increase the scalar quantity of each drive wheel quantity by some scalar

     */

}
