package nesting.geometry.elements;

/**
 * A collection of points along a straight path extending indefinitely in both directions.
 */
public class Line {

    /**
     * One point defining the line.
     */
    public final Point a;

    /**
     * Another point defining the line. Ideally, this point should be different from the first one,
     * although this is not checked.
     */
    public final Point b;

    /**
     * The line in coordinate form.
     */
    public final LineCoordinates coordinates;

    /**
     * Constructs a line through these two points.
     * 
     * @param a a point
     * @param b another point
     */
    public Line(Point a, Point b) {
        this.a = a;
        this.b = b;
        this.coordinates = getCoordinates();
    }

    /**
     * Constructs a line from a point and a vector.
     * 
     * @param a a point
     * @param v a vector
     */
    public Line(Point a, Vector v) {
        this(a, new Point(a.x + v.x, a.y + v.y));
    }

    /**
     * Constructs a line through a certain point with a certain angle between the line and the positive
     * x-axis.
     * 
     * @param a     a point
     * @param angle an angle in radians
     */
    public Line(Point a, double angle) {
        this(a, new Point(a.x + Math.cos(angle), a.y + Math.sin(angle)));
    }

    private LineCoordinates getCoordinates() {
        double lc_a = this.a.y - this.b.y;
        double lc_b = this.b.x - this.a.x;
        double lc_c = this.b.x * this.a.y - this.a.x * this.b.y;

        return new LineCoordinates(lc_a, lc_b, lc_c);
    }

    /**
     * Calculates the point of intersection between two lines using the coordinate form. Returns
     * <code>null</code> if the lines are parallel.
     * 
     * @param l1 a line
     * @param l2 another line
     * @return <code>Point</code> of intersection, <code>null</code> if lines are parallel
     */
    public static Point getIntersection(Line l1, Line l2) {
        LineCoordinates lc1 = l1.coordinates;
        LineCoordinates lc2 = l2.coordinates;

        double denominator = lc1.a * lc2.b - lc2.a * lc1.b;
        if (denominator != 0.0) {
            double x_intersection = (lc1.c * lc2.b - lc2.c * lc1.b)
                    / denominator;
            double y_intersection = (lc1.a * lc2.c - lc2.a * lc1.c)
                    / denominator;
            return new Point(x_intersection, y_intersection);
        } else
            return null;
    }

    /**
     * Coordinates of a line. The line consists of all points satisfying the equation
     * <i>ax</i>+<i>by</i>=<i>c</i>.
     */
    public static class LineCoordinates {

        /**
         * Constant <i>a</i> of the coordinate form.
         */
        public final double a;

        /**
         * Constant <i>b</i> of the coordinate form.
         */
        public final double b;

        /**
         * Constant <i>c</i> of the coordinate form.
         */
        public final double c;

        /**
         * Constructs a triple of line coordinates with the given values.
         * 
         * @param a constant <i>a</i> of the coordinate form
         * @param b constant <i>b</i> of the coordinate form
         * @param c constant <i>c</i> of the coordinate form
         */
        public LineCoordinates(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public String toString() {
            return "LineCoordinates(" + a + ", " + b + ", " + c + ")";
        }
    }
}
