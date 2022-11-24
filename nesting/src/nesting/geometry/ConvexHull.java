package nesting.geometry;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nesting.geometry.elements.*;
import nesting.tuple.PolygonSet;
import nesting.util.CircularArrayList;
import nesting.util.CircularList;

/**
 * Contour polygon algorithm for calculating the convex hull of a set of points in two steps. First,
 * the contour of the set of points is calculated using a double plane sweep. Second, concave angles
 * are eliminated from that contour.
 */
public class ConvexHull {

    private List<Point> points;

    private Deque<Point> leftContourPoints;
    private Deque<Point> rightContourPoints;
    private List<Point> contourPolygonPoints;
    private CircularList<Point> convexHull;

    private ConvexHull() {
    }

    /**
     * Returns the convex hull polygon of a given polygon.
     * 
     * @param polygon a polygon
     * @return the convex hull polygon
     */
    public static Polygon of(Polygon polygon) {
        return new Polygon(of(polygon.vertices));
    }

    /**
     * Returns the convex hull polygon for the given set of polygons.
     * 
     * @param set a set of polygons
     * @return the convex hull polygon for the set of polygons
     */
    public static Polygon of(PolygonSet set) {
        return new Polygon(of(set.getPolygons().stream()
                .flatMap(polygon -> polygon.vertices.stream())
                .collect(Collectors.toList())));
    }

    /**
     * Returns a circular list of the vertices of the convex hull of a given set of points. Runtime is
     * <i>O</i>(<i>n</i> log <i>n</i>).
     * 
     * @param points a list of points
     * @return a circular list of the vertices of the convex hull of the given set of points
     */
    public static CircularList<Point> of(List<Point> points) {
        return of(points, false);
    }

    /**
     * Returns a circular list of the vertices of the convex hull of a given set of points, which are
     * assumed to be sorted by x-coordinate. Runtime is <i>O</i>(<i>n</i>).
     * 
     * @param points a list of points sorted by x-coordinate
     * @return a circular list of the vertices of the convex hull of the given set of points
     */
    public static CircularList<Point> ofPresorted(List<Point> points) {
        return of(points, true);
    }

    /**
     * Returns a circular list of the vertices of the convex hull of a given tree set of points. It is
     * assumed that the tree set's comparator has sorted the points by x-coordinate. Runtime is
     * <i>O</i>(<i>n</i>).
     * 
     * @param points a tree set of points
     * @return a circular list of the vertices of the convex hull of the given set of points
     */
    public static CircularList<Point> of(TreeSet<Point> points) {
        return of(new ArrayList<>(points), true);
    }

    /**
     * This method performs only the first step of the algorithm and returns the contour polygon.
     * 
     * @param polygon a polygon
     * @return the contour polygon of that polygon
     */
    public static Polygon getContourPolygon(Polygon polygon) {
        ConvexHull ch = new ConvexHull();
        ch.points = new ArrayList<>(polygon.vertices);
        Collections.sort(ch.points, new ByXCoordinate());
        ch.calculateContourPolygon();
        return new Polygon(ch.contourPolygonPoints);
    }

    private static CircularList<Point> of(List<Point> points,
            boolean presorted) {
        ConvexHull ch = new ConvexHull();
        ch.points = new ArrayList<>(points);
        if (!presorted) Collections.sort(ch.points, new ByXCoordinate());
        ch.calculateContourPolygon();
        ch.calculateConvexHull();
        return ch.convexHull;
    }

    private void calculateContourPolygon() {

        // Calculate left and right contour
        doLeftRightSweep();
        doRightLeftSweep();

        // Eliminate duplicate points
        if (leftContourPoints.getFirst() == rightContourPoints.getLast())
            rightContourPoints.removeLast();
        if (rightContourPoints.getFirst() == leftContourPoints.getLast())
            leftContourPoints.removeLast();

        // Concatenate left and right contour
        contourPolygonPoints = Stream
                .concat(leftContourPoints.stream(), rightContourPoints.stream())
                .collect(Collectors.toList());
    }

    private void doLeftRightSweep() {
        leftContourPoints = new ArrayDeque<>();

        // Add left extreme point and initialize sweep status structure
        leftContourPoints.add(points.get(0));
        double maxY = points.get(0).y;
        double minY = maxY;

        // Perform left-right sweep
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).y > maxY) {
                leftContourPoints.addFirst(points.get(i));
                maxY = points.get(i).y;
            }
            if (points.get(i).y < minY) {
                leftContourPoints.addLast(points.get(i));
                minY = points.get(i).y;
            }
        }
    }

    private void doRightLeftSweep() {
        rightContourPoints = new ArrayDeque<>();

        // Add right extreme point and initialize sweep status structure
        rightContourPoints.add(points.get(points.size() - 1));
        double maxY = points.get(points.size() - 1).y;
        double minY = maxY;

        // Perform right-left sweep
        for (int i = points.size() - 2; i > 0; i--) {
            if (points.get(i).y > maxY) {
                rightContourPoints.addLast(points.get(i));
                maxY = points.get(i).y;
            }
            if (points.get(i).y < minY) {
                rightContourPoints.addFirst(points.get(i));
                minY = points.get(i).y;
            }
        }
    }

    private void calculateConvexHull() {
        convexHull = new CircularArrayList<>(contourPolygonPoints);

        int i = 0;
        while (i < convexHull.size() - 2) {

            // Check if point is to the the left of current line segment
            if (!DirectedLineSegment.isLeftOf(convexHull.get(i + 2),
                    new DirectedLineSegment(convexHull.get(i),
                            convexHull.get(i + 1)))) {

                // If not, we retrace
                int j = i;
                while (j > -1
                        && !DirectedLineSegment.isLeftOf(convexHull.get(i + 2),
                                new DirectedLineSegment(convexHull.get(j),
                                        convexHull.get(j + 1))))
                    j--;

                // Delete all points between current segment and original point
                convexHull.subList(j + 2, i + 2).clear();

                // Reset index
                i = j;
            }

            // Go to next point
            i++;
        }
    }

    private static class ByXCoordinate implements Comparator<Point> {

        @Override
        public int compare(Point p1, Point p2) {
            return Double.compare(p1.x, p2.x);
        }
    }
}
