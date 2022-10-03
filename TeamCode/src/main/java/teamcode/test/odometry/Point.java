package teamcode.test.odometry;

public class Point {
    double x;
    double y;

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }
    public Point(){
    }

    /**
     * calculate the slope
     * @param p2 the second point needed to form a line
     * @return the slope as a double
     */
    public double slope(Point p2){
        return (y - p2.y) / (x - p2.x);
    }

    public boolean equals(Point other){
        return this.y == other.y && this.x == other.x;
    }


}
