package nesting.geometry.elements;

import nesting.svg.Drawable;
import nesting.svg.SVGElement;

/**
 * A point with integer coordinates. Main use is in search spaces where points have to be added to
 * and removed from collections. Checking integer coordinates for equality is more reliable than
 * checking floating point coordinates.
 */
public class IntegerPoint implements Comparable<IntegerPoint>, Drawable {

    /**
     * The point of origin with coordinates (0, 0).
     */
    public static final IntegerPoint ORIGIN = new IntegerPoint(0, 0);

    /**
     * The x-coordinate.
     */
    public final long x;

    /**
     * The y-coordinate.
     */
    public final long y;

    /**
     * Constructs an integer point with the given coordinates.
     * 
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public IntegerPoint(long x, long y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns a new point which is the result of translating this integer point by a vector
     * corresponding to the given integer point.
     * 
     * @param point an integer point
     * @return a new integer point which is the result of translating this point by a vector
     *         corresponding to the integer point
     */
    public IntegerPoint translate(IntegerPoint point) {
        return new IntegerPoint(x + point.x, y + point.y);
    }

    /**
     * Returns a new point which is the result of translating this integer point by a vector.
     * 
     * @param point a vector
     * @return a new integer point which is the result of translating this point by the vector
     */
    public IntegerPoint translate(IntegerVector vector) {
        return new IntegerPoint(x + vector.x, y + vector.y);
    }

    /**
     * Returns an integer point which is this point reflected around the point of origin. Interpreting
     * the point as a position vector, this is its negative vector.
     * 
     * @return an integer point reflected around the point of origin
     */
    public IntegerPoint reflect() {
        return new IntegerPoint(-x, -y);
    }

    /**
     * Returns the integer position vector corresponding to this integer point.
     * 
     * @return the integer position vector corresponding to this integer point
     */
    public IntegerVector getIntegerVector() {
        return new IntegerVector(x, y);
    }

    public Point toPoint() {
        return new Point(x, y);
    }

    /**
     * Integer points are equal if they have the same coordinates. Overriding <code>equals</code> is
     * maybe the whole point of this class.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof IntegerPoint) {
            IntegerPoint point = (IntegerPoint) o;
            return this.x == point.x && this.y == point.y;
        } else {
            return false;
        }
    }

    /**
     * In looking for the fastest way to implement search spaces, we may experiment with hash tables, so
     * here is a hash function for integer points.
     */
    @Override
    public int hashCode() {
        return (int) (x * 13 + y * 31);
    }

    /**
     * Lexical sorting for integer points.
     */
    @Override
    public int compareTo(IntegerPoint point) {
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

    @Override
    public String toString() {
        return "IntegerPoint(" + x + ", " + y + ")";
    }

    @Override
    public SVGElement toSVGElement() {
        return (new Point(x, y)).toSVGElement();
    }
}
