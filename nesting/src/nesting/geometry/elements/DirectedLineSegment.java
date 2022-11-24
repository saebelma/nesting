package nesting.geometry.elements;

/**
 * A line segment with a direction, i.e. going <i>from</i> <code>Point a</code> <i>to</i>
 * <code>Point b</code>. For example, the edges of a polygon are directed line segments going from
 * vertex to vertex in counter-clockwise order.
 */
public class DirectedLineSegment extends LineSegment {

    /**
     * Constructs a line segment from <code>Point a</code> to <code>Point b</code>.
     * 
     * @param a the start point of the line segment
     * @param b the end point of the line segment
     */
    public DirectedLineSegment(Point a, Point b) {
        super(a, b);
    }

    /**
     * Returns <code>true</code> if the point is on the left of the line. This function is used, for
     * example, in testing whether a polygon contains a certain point.
     * 
     * @param point a point
     * @param line  a line
     * @return <code>true</code> if the point is on the left of the line
     */
    public static boolean isLeftOf(Point point, DirectedLineSegment line) {
        double d = (point.x - line.a.x) * (line.b.y - line.a.y)
                - (point.y - line.a.y) * (line.b.x - line.a.x);
        return d < 0;
    }

    /**
     * Returns a vector defining the direction of the line.
     * 
     * @return a vector defining the direction of the line
     */
    public Vector getVector() {
        return new Vector(b.x - a.x, b.y - a.y);
    }

    /**
     * Returns a vector defining the reverse direction of the line, i.e. the direction of a line going
     * from <code>b</code> to <code>a</code>.
     * 
     * @return a vector defining the reverse direction of the line <code>a</code>
     */
    public Vector getReverseVector() {
        return new Vector(a.x - b.x, a.y - b.y);
    }

    /**
     * Returns a directed line segment with the same length and orientation of this line segment which
     * is offset to the right side of this segment by a given distance. This function is used mainly in
     * generating the parallel curve of a polygon.
     * 
     * @param distance distance in mm
     * @return a parallel line segment going in the same direction
     */
    public DirectedLineSegment getParallelSegment(double distance) {
        double angle = this.getVector().getAngle();
        Point a_prime = new Point(
                a.x + Math.cos(angle - Math.PI / 2) * distance,
                a.y + Math.sin(angle - Math.PI / 2) * distance);
        Point b_prime = new Point(
                b.x + Math.cos(angle - Math.PI / 2) * distance,
                b.y + Math.sin(angle - Math.PI / 2) * distance);
        return new DirectedLineSegment(a_prime, b_prime);
    }

    /**
     * Returns the directed line segment from <code>b</code> to <code>a</code>.
     * 
     * @return the reverse line segment
     */
    public DirectedLineSegment getReverseSegment() {
        return new DirectedLineSegment(b, a);
    }

    @Override
    public String toString() {
        return "DirectedLineSegment(" + a.toString() + ", " + b.toString()
                + ")";
    }
}
