package teamcode.common;

/**
 * A 2-dimensional bounding box.
 */
public class BoundingBox2D {

    private double x1, y1, x2, y2;

    public BoundingBox2D(Vector2D topLeft, Vector2D bottomRight) {
        this(topLeft.getX(), topLeft.getY(), bottomRight.getX(), bottomRight.getY());
    }

    public BoundingBox2D(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getY1() {
        return y1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getY2() {
        return y2;
    }

    public void setY2(double y2){
        this.y2 = y2;
    }

    public boolean contains(Vector2D point) {
        return contains(point.getX(), point.getY());
    }

    /**
     * Returns true if this bounding box contains the point specified by the x and y values.
     */
    public boolean contains(double x, double y) {
        return x1 <= x && x <= x2 && y1 <= y && y <= y2;
    }

    @Override
    public String toString() {
        return String.format("x1=%.1f, y1=%.1f, x2=%.1f, y2=%.1f", x1, y1, x2, y2);
    }

}
