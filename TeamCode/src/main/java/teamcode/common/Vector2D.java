package teamcode.common;

/**
 * Represents a 2-dimensional vector.
 */
public final class Vector2D implements Cloneable {

    private double x;
    private double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2D forward() {
        return new Vector2D(0, 1);
    }

    public static Vector2D right() {
        return new Vector2D(1, 0);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY() {
        this.y = y;
    }

    public void multiply(double scalar) {
        x *= scalar;
        y *= scalar;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public void normalize() {
        double magnitude = magnitude();
        x /= magnitude;
        y /= magnitude;
    }

    public void add(Vector2D vector) {
        x += vector.x;
        y += vector.y;
    }

    public double dotProduct(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    /**
     * @return the angle in radians
     */
    public double angleBetween(Vector2D other) {
        return Math.acos(this.dotProduct(other) / (this.magnitude() * other.magnitude()));
    }

    public boolean isZero() {
        return x == 0.0 && y == 0.0;
    }

    /**
     * @return the angle in radians from -pi to pi.
     */
    public double getDirection() {
        return Math.atan2(y, x);
    }

    @Override
    public String toString() {
        return String.format("x=%.1f, y=%.1f", x, y);
    }

    @Override
    public Vector2D clone() {
        return new Vector2D(x, y);
    }

}