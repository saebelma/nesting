package nesting.geometry.elements;

import nesting.svg.*;

/**
 * An extensionless figure with an exact location in the Euclidean plane. As in all other classes
 * representing geometrical objects, all fields defining an object are public and final. All methods
 * transforming an object (translation, rotation, etc.) don't change the object, they return a new
 * object.
 */
public class Point implements Comparable<Point>, Drawable {

    /**
     * The point of origin with coordinates (0.0, 0.0).
     */
    public static final Point ORIGIN = new Point(0.0, 0.0);

    /**
     * The x-coordinate.
     */
    public final double x;

    /**
     * The y-coordinate.
     */
    public final double y;

    /**
     * Constructs a point with the given coordinates.
     * 
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the Euclidean distance between two points.
     * 
     * @param a a point
     * @param b another point
     * @return the Euclidean distance between the points
     */
    public static double distance(Point a, Point b) {
        return Math.hypot(b.x - a.x, b.y - a.y);
    }

    /**
     * Returns the midpoint between two given points, i.e. the point on a line segment connecting the
     * points that is equidistant from both points.
     * 
     * @param a a point
     * @param b another point
     * @return the midpoint between the points
     */
    public static Point midpoint(Point a, Point b) {
        return new Point((a.x + b.x) / 2, (a.y + b.y) / 2);
    }

    /**
     * Returns a new point which is the result of rotating a the point around a given vertex by the
     * given angle.
     * 
     * @param origin        the vertex of the rotation
     * @param rotationAngle the counter-clockwise angle of rotation in radians
     * @return a new point which is the result of rotating the original point around the vertex by the
     *         given angle
     */
    public Point rotate(Point origin, double rotationAngle) {
        double deltaX = x - origin.x;
        double deltaY = y - origin.y;
        double deltaXPrime = deltaX * Math.cos(rotationAngle)
                - deltaY * Math.sin(rotationAngle);
        double deltaYPrime = deltaX * Math.sin(rotationAngle)
                + deltaY * Math.cos(rotationAngle);
        return new Point(origin.x + deltaXPrime, origin.y + deltaYPrime);
    }

    /**
     * Returns a new point which is the result of rotating a given point around a given vertex by the
     * given angle.
     * 
     * @param p             a point
     * @param origin        the vertex of the rotation
     * @param rotationAngle the counter-clockwise angle of rotation in radians
     * @return a new point which is the result of rotating the original point around the vertex by the
     *         given angle
     */
    public static Point rotate(Point p, Point origin, double rotationAngle) {
        double deltaX = p.x - origin.x;
        double deltaY = p.y - origin.y;
        double deltaXPrime = deltaX * Math.cos(rotationAngle)
                - deltaY * Math.sin(rotationAngle);
        double deltaYPrime = deltaX * Math.sin(rotationAngle)
                + deltaY * Math.cos(rotationAngle);
        return new Point(origin.x + deltaXPrime, origin.y + deltaYPrime);
    }

    /**
     * Returns a new point which is the result of translating this point by the given vector
     * 
     * @param vector a vector
     * @return a new point which is the result of translating this point by the given vector
     */
    public Point translate(Vector vector) {
        return new Point(x + vector.x, y + vector.y);
    }

    /**
     * Returns a new point which is the result of translating this point by the given integer vector
     * 
     * @param vector an integer vector
     * @return a new point which is the result of translating this point by the given integer vector
     */
    public Point translate(IntegerVector vector) {
        return new Point(x + vector.x, y + vector.y);
    }

    @Override
    public String toString() {
        return "Point(" + x + ", " + y + ")";
    }

    /**
     * Comparator for sorting lexically, i.e. first by x-coordinate, then by y-coordinate.
     */
    @Override
    public int compareTo(Point point) {
        if (x == point.x) {
            if (y > point.y)
                return 1;
            else if (y < point.y)
                return -1;
            else
                return 0;
        } else if (x > point.x)
            return 1;
        else
            return -1;
    }

    /**
     * Returns an svg string representing the coordinates of this point. The y-coordinate is multiplied
     * by (-1) in order to account for the screen coordinate system used in SVG.
     * 
     * @return an svg string representing the coordinates of this point
     */
    public String toSVGCoordinates() {
        return x + "," + (-1) * y;
    }

    @Override
    public SVGElement toSVGElement() {
        return new SVGElement(
                "<circle cx=\"" + x + "\" cy=\"" + (-1) * y
                        + "\" r=\"3\" fill=\"" + SVG.getStrokeColor() + "\"/>",
                new SVGBounds(x, x, y, y));
    }

}
