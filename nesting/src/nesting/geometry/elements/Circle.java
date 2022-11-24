package nesting.geometry.elements;

import nesting.irregular.NestingParameters;
import nesting.svg.*;

/**
 * A geometric shape consisting of all points in a plane that are at a given distance from a given
 * point. Given by a point (the center of the circle) and a radius.
 */
public class Circle implements Drawable {

    /**
     * The point of origin with coordinates (0.0, 0.0).
     */
    public static final Circle TABLE = new Circle(Point.ORIGIN,
            NestingParameters.tableRadius);

    private static final double EPSILON = 0.001;

    /**
     * The center of the circle.
     */
    public final Point center;

    /**
     * The radius of the circle.
     */
    public final double radius;

    /**
     * Constructs a circle with the given center and radius.
     * 
     * @param center the center of the circle
     * @param radius the radius of the circle
     */
    public Circle(Point center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    /**
     * Returns <code>true</code> if the circle contains the given point. In order to account for
     * floating point inaccuracy, a small epsilon is applied.
     * 
     * @param p a point
     * @return <code>true</code> if the circle contains the point
     */
    public boolean contains(Point p) {
        return Point.distance(center, p) - radius < EPSILON;
    }

    /**
     * Returns <code>true</code> if the circle contains the given polygon. In order to account for
     * floating point inaccuracy, a small epsilon is applied.
     * 
     * @param p a polygon
     * @return <code>true</code> if the circle contains the polygon
     */
    public boolean contains(Polygon p) {
        return p.vertices.stream().allMatch(point -> contains(point));
    }

    /**
     * Returns the area of this circle.
     * 
     * @return the area of this circle
     */
    public double getArea() {
        return radius * radius * Math.PI;
    }

    /**
     * Returns a circle that is constructed from three given points on the circumference.
     * 
     * @param a a point
     * @param b another point
     * @param c yet another point
     * @return a circle constructed from these points
     */
    public static Circle from3Points(Point a, Point b, Point c) {
        Line chord_1 = new Line(a, b);
        Line chord_2 = new Line(b, c);
        Point midpoint_1 = Point.midpoint(a, b);
        Point midpoint_2 = Point.midpoint(b, c);
        Vector normalVector_1 = new Vector(chord_1.coordinates.a,
                chord_1.coordinates.b);
        Vector normalVector_2 = new Vector(chord_2.coordinates.a,
                chord_2.coordinates.b);
        Line perpendicularBisector_1 = new Line(midpoint_1, normalVector_1);
        Line perpendicularBisector_2 = new Line(midpoint_2, normalVector_2);
        Point center = Line.getIntersection(perpendicularBisector_1,
                perpendicularBisector_2);
        double radius = Point.distance(center, a);
        return new Circle(center, radius);
    }

    @Override
    public SVGElement toSVGElement() {
        return new SVGElement(
                "<circle cx=\"" + center.x + "\" cy=\"" + (-1) * center.y
                        + "\" r=\"" + radius + "\"" + SVG.getStyle() + "/>\n",
                new SVGBounds(center.x - radius, center.x + radius,
                        center.y - radius, center.y + radius));
    }

    @Override
    public String toString() {
        return "Circle [center=" + center + ", radius=" + radius + "]";
    }

}
