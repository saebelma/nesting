package nesting.svg;

/**
 * The bounds of some drawable geometric object.
 */
public class SVGBounds {

    /**
     * An <code>SVGBounds</code> object with empty bounds (all zero).
     */
    public static final SVGBounds EMPTY = new SVGBounds(0.0, 0.0, 0.0, 0.0);

    /**
     * Lower bound of x-coordinates.
     */
    public final double minX;

    /**
     * Upper bound of x-coordinates.
     */
    public final double maxX;

    /**
     * Lower bound of y-coordinates.
     */
    public final double minY;

    /**
     * Upper bound of y-coordinates.
     */
    public final double maxY;

    /**
     * Constructs an svg bounds object with the given values.
     * 
     * @param minX lower bound of x-coordinates
     * @param maxX upper bound of x-coordinates
     * @param minY lower bound of y-coordinates
     * @param maxY pper bound of y-coordinates
     */
    public SVGBounds(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    /**
     * Returns the width of these bounds.
     * 
     * @return the width of these bounds
     */
    public double getWidth() {
        return maxX - minX;
    }

    /**
     * Returns the height of these bounds.
     * 
     * @return the height of these bounds
     */
    public double getHeight() {
        return maxY - minY;
    }
}
