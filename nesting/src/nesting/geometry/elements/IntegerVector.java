package nesting.geometry.elements;

import nesting.svg.Drawable;
import nesting.svg.SVGElement;

/**
 * A vector is a geometric object that has magnitude (or length) and direction. It is represented
 * here as a pair of integer coordinates (x-coordinate and y-coordinate).
 */
public class IntegerVector implements Comparable<IntegerVector>, Drawable {

    public static final IntegerVector ORIGIN = new IntegerVector(0, 0);

    /**
     * The vector's x-coordinate.
     */
    public final long x;

    /**
     * The vector's y-coordinate.
     */
    public final long y;

    /**
     * Constructs an integer vector with the given coordinates.
     * 
     * @param x the vector's x-coordinate
     * @param y the vector's y-coordinate
     */
    public IntegerVector(long x, long y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the negative vector to this vector.
     * 
     * @return the negative vector to this vector
     */
    public IntegerVector getNegativeVector() {
        return new IntegerVector(-x, -y);
    }

    /**
     * Returns a vector which is the result of adding this vector n times.
     * 
     * @param n the number of times this vector is added
     * @return a vector which is the result of adding this vector n times
     */
    public IntegerVector times(int n) {
        return new IntegerVector(n * x, n * y);
    }

    /**
     * Returns a vector which is the result of this vector plus the given vector.
     * 
     * @param vector the vector to be added
     * @return a vector which is the result of adding the given vector to this vector
     */
    public IntegerVector plus(IntegerVector vector) {
        return new IntegerVector(x + vector.x, y + vector.y);
    }

    /**
     * Returns the vector sum.
     * 
     * @param v_1 an integer vector
     * @param v_2 another integer vector
     * @return the sum of the two integer vectors
     */
    public static IntegerVector add(IntegerVector v_1, IntegerVector v_2) {
        return new IntegerVector(v_1.x + v_2.x, v_1.y + v_2.y);
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
    public int compareTo(IntegerVector point) {
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
        return "IntegerVector(" + x + ", " + y + ")";
    }

    @Override
    public SVGElement toSVGElement() {
        return (new Point(x, y)).toSVGElement();
    }

    public Point toPoint() {
        return new Point(x, y);
    }

    public Vector toVector() {
        return new Vector(x, y);
    }

    public IntegerVector reflect() {
        return new IntegerVector(-x, -y);
    }
}
