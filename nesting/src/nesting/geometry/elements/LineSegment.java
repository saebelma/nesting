package nesting.geometry.elements;

import java.util.List;

import nesting.svg.*;

/**
 * A part of a line that is bounded by two end points. The line segment includes the end points and
 * is assumed to be non-directed, i.e. the line segment from <code>a</code> to <code>b</code> should
 * behave exactly the same as the line segment from <code>b</code> to <code>a</code>.
 */
public class LineSegment implements Drawable {

    private final static double EPSILON = 0.001;

    /**
     * The first end point.
     */
    public final Point a;

    /**
     * The second end point.
     */
    public final Point b;

    /**
     * The line the line segment is part of.
     */
    public final Line line;

    /**
     * Constructs the undirected line segment between these two points.
     * 
     * @param a the first end point
     * @param b the second end point
     */
    public LineSegment(Point a, Point b) {
        this.a = a;
        this.b = b;
        line = new Line(a, b);
    }

    /**
     * Returns point of intersection or <code>null</code> if segments don't intersect. The intersection
     * test is implemented by calculating the intersection of the lines and then checking if the
     * intersection is on both segments. Because of limited precision floating-point arithmetic, a small
     * epsilon is defined for this last test.
     * 
     * @param ls1 one line segment
     * @param ls2 another line segment
     * @return <code>Point</code> of intersection or <code>null</code> if segments don't intersect
     */
    public static Point getIntersection(LineSegment ls1, LineSegment ls2) {
        Point v = Line.getIntersection(ls1.line, ls2.line);
        if (v != null && isOn(v, ls1) && isOn(v, ls2))
            return v;
        else
            return null;
    }

    /**
     * Testing two lists of line segments for intersection. This is, in effect, a brute-force
     * intersection test for two polygons. Time complexity is <i>O</i>(<i>n</i><sup>2</sup>) for two
     * polygons with <i>n</i> edges each.
     * 
     * @param edges_1 a list of line segments
     * @param edges_2 another list of line segments
     * @return <code>true</code> if any of the line segments in the first list intersects any of the
     *         line segments in the second list
     */
    public static boolean doIntersect(List<? extends LineSegment> edges_1,
            List<? extends LineSegment> edges_2) {
        for (LineSegment edge_1 : edges_1)
            for (LineSegment edge_2 : edges_2)
                if (getIntersection(edge_1, edge_2) != null) return true;
        return false;
    }

    private static boolean isOn(Point v, LineSegment ls) {
        if (v.x > Math.min(ls.a.x, ls.b.x) - EPSILON
                && v.x < Math.max(ls.a.x, ls.b.x) + EPSILON
                && v.y > Math.min(ls.a.y, ls.b.y) - EPSILON
                && v.y <= Math.max(ls.a.y, ls.b.y) + EPSILON)
            return true;
        else
            return false;
    }

    @Override
    public String toString() {
        return "LineSegment(" + a.toString() + ", " + b.toString() + ")";
    }

    @Override
    public SVGElement toSVGElement() {
        return new SVGElement(
                "<line x1=\"" + a.x + "\" y1=\"" + (-1) * a.y + "\" x2=\"" + b.x
                        + "\" y2=\"" + (-1) * b.y + "\"" + SVG.getStyle()
                        + "/>\n",
                new SVGBounds(Math.min(a.x, b.x), Math.max(a.x, b.x),
                        Math.min(a.y, b.y), Math.max(a.y, b.y)));
    }
}
