package teamcode.common;

public class Vector3D {

    private double x;
    private double y;
    private double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void multiply(double scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
    }

    public void add(Vector3D vector) {
        x += vector.x;
        y += vector.y;
        z += vector.z;
    }

    public boolean isZero() {
        return x == 0.0 && y == 0.0 && z == 0;
    }

    @Override
    public String toString() {
        return String.format("x=%.1f, y=%.1f, z=%.1f", x, y, z);
    }

    @Override
    public Vector3D clone() {
        return new Vector3D(x, y, z);
    }

}
