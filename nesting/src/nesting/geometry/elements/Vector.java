package nesting.geometry.elements;

/**
 * A (Euclidean) vector is a geometric object that has magnitude (or length) and direction. It is
 * represented here as a pair of coordinates (x-coordinate and y-coordinate).
 */
public class Vector {

    /**
     * A vector representing the positive x-axis.
     */
    public static final Vector xAxis = new Vector(1.0, 0.0);

    public static final Vector ORIGIN = new Vector(0.0, 0.0);

    /**
     * The vector's x-coordinate.
     */
    public final double x;

    /**
     * The vector's y-coordinate.
     */
    public final double y;

    /**
     * Constructs a vector with the given coordinates.
     * 
     * @param x the vector's x-coordinate
     * @param y the vector's y-coordinate
     */
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs the position vector of a given point relative to a given point of origin.
     * 
     * @param origin the point of origin
     * @param p      a point
     */
    public Vector(Point origin, Point p) {
        this(p.x - origin.x, p.y - origin.y);
    }

    /**
     * Constructs the position vector of a given point relative to the point of origin of the coordinate
     * system.
     * 
     * @param p a point
     */
    public Vector(Point p) {
        this(Point.ORIGIN, p);
    }

    /**
     * Returns the vector sum.
     * 
     * @param v_1 a vector
     * @param v_2 another vector
     * @return the sum of the two vectors
     */
    public static Vector add(Vector v_1, Vector v_2) {
        return new Vector(v_1.x + v_2.x, v_1.y + v_2.y);
    }

    /**
     * Returns the angle between the positive x-axis and the vector (interpreted as a position vector)
     * in radians (0 to 2 * pi).
     * 
     * @return the angle to the positive x-axis in radians (0 to 2 * pi)
     */
    public double getAngle() {
        return getAngle(xAxis, this);
    }

    /**
     * Returns the length of the vector.
     * 
     * @return the length of the vector
     */
    public double getLength() {
        return Math.hypot(x, y);
    }

    /**
     * @param v1 first vector
     * @param v2 second vector
     * @return the angle between the vectors in radians (0 to 2 * pi)
     */
    public static double getAngle(Vector v1, Vector v2) {
        double angle = Math.atan2(v2.y, v2.x) - Math.atan2(v1.y, v1.x);
        return (angle < 0) ? angle + 2 * Math.PI : angle;
    }

    /**
     * @param v1 first integer vector
     * @param v2 second integer vector
     * @return the angle between the integer vectors in radians (0 to 2 * pi)
     */
    public static double getAngle(IntegerVector v1, IntegerVector v2) {
        double angle = Math.atan2(v2.y, v2.x) - Math.atan2(v1.y, v1.x);
        return (angle < 0) ? angle + 2 * Math.PI : angle;
    }

    @Override
    public String toString() {
        return "Vector(" + x + ", " + y + ")";
    }
}
