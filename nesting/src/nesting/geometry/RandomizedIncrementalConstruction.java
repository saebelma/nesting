package nesting.geometry;

import java.util.*;

import nesting.geometry.elements.*;

/**
 * Randomized incremental construction algorithm for the smallest enclosing circle of a polygon or
 * set of points. Expected runtime is <i>O</i>(<i>n</i>).
 */
public class RandomizedIncrementalConstruction {

    private List<Point> points;
    private Circle smallestEnclosingCircle;
    private int n = 0;

    /**
     * Constructs an algorithm and computes the smallest enclosing circle of a polygon.
     * 
     * @param polygon a polygon
     */
    public RandomizedIncrementalConstruction(Polygon polygon) {
        this(polygon.vertices);
    }

    /**
     * Constructs an algorithm and computes the smallest enclosing circle of a set of points.
     * 
     * @param points a list of points
     */
    public RandomizedIncrementalConstruction(List<Point> points) {
        this.points = points;
        calculateSmallestEnclosingCircle();
    }

    private void calculateSmallestEnclosingCircle() {
        if (points.size() == 0) {
            smallestEnclosingCircle = null;
        } else if (points.size() == 1) {
            smallestEnclosingCircle = new Circle(points.get(0), 0.0);
        } else {
            List<Point> P = new ArrayList<>(points);
            Collections.shuffle(P);
            Circle C = circleFromDiametralPoints(P.get(0), P.get(1));
            for (int i = 2; i < P.size(); i++) {
                n++;
                if (!C.contains(P.get(i))) {
                    C = circleWithQOnBoundary(P.subList(0, i), P.get(i));
                }
            }
            smallestEnclosingCircle = C;
        }
    }

    private Circle circleWithQOnBoundary(List<Point> P, Point q) {
        Circle C = circleFromDiametralPoints(P.get(0), q);
        for (int j = 1; j < P.size(); j++) {
            n++;
            if (!C.contains(P.get(j))) {
                C = circleWithQ1Q2OnBoundary(P.subList(0, j), P.get(j), q);
            }
        }
        return C;
    }

    private Circle circleWithQ1Q2OnBoundary(List<Point> P, Point q1, Point q2) {
        Circle C = circleFromDiametralPoints(q1, q2);
        for (int k = 0; k < P.size(); k++) {
            n++;
            if (!C.contains(P.get(k))) {
                C = Circle.from3Points(q1, q2, P.get(k));
            }
        }
        return C;
    }

    // Constructs a circle from two diametral points
    private Circle circleFromDiametralPoints(Point p1, Point p2) {
        Point midpoint = Point.midpoint(p1, p2);
        return new Circle(midpoint,
                Math.hypot(p1.x - midpoint.x, p1.y - midpoint.y));
    }

    /**
     * Returns the input list of points or vertices.
     * 
     * @return a list of points
     */
    public List<Point> getPoints() {
        return points;
    }

    /**
     * Returns the smallest enclosing circle of the input polygon or list of points.
     * 
     * @return the smallest enclosing circle
     */
    public Circle getSmallestEnclosingCircle() {
        return smallestEnclosingCircle;
    }

}
