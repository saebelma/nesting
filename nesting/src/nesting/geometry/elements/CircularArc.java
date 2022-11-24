package nesting.geometry.elements;

import nesting.svg.*;

/**
 * The arc of a circle between a pair of distinct points. Point a is the start point, point b the
 * end point (in counter-clockwise order).
 */
public class CircularArc implements Drawable {

    /**
     * The center of the circle.
     */
    public final Point center;

    /**
     * The starting point of the arc on the circumference of the circle.
     */

    public final Point a;
    /**
     * The end point of the arc on the circumference of the circle.
     */
    public final Point b;

    private Vector vector_a;
    private Vector vector_b;

    /**
     * @param center the center of the circle
     * @param a      the start point
     * @param b      the end point
     */
    public CircularArc(Point center, Point a, Point b) {
        this.center = center;
        this.a = a;
        this.b = b;

        vector_a = new Vector(center, a);
        vector_b = new Vector(center, b);
    }

    /**
     * Gets the angle between positive x-axis and the ray from circle center through point
     * <code>a</code>.
     * 
     * @return the angle to x-axis of a ray through point <code>a</code> in radians
     */
    public double getAngleA() {
        return vector_a.getAngle();
    }

    /**
     * Gets the angle between positive x-axis and the ray from circle center through point
     * <code>b</code>.
     * 
     * @return the angle to x-axis of a ray through point <code>b</code> in radians
     */
    public double getAngleB() {
        return vector_b.getAngle();
    }

    /**
     * Gets the angle between <code>a</code> and <code>b</code> in radians
     * 
     * @return the angle between <code>a</code> and <code>b</code> in radians
     */
    public double getCentralAngle() {
        return Vector.getAngle(vector_a, vector_b);
    }

    /**
     * Gets the radius of the arc.
     * 
     * @return the radius of the arc
     */
    public double getRadius() {
        return vector_a.getLength();
    }

    @Override
    public SVGElement toSVGElement() {
        return new SVGElement(
                "<path d=\"M " + a.x + " " + (-1) * a.y + " A " + getRadius()
                        + " " + getRadius() + " 0 0 0 " + b.x + " " + (-1) * b.y
                        + "\"" + SVG.getStyle() + "\"/>\n",
                new SVGBounds(center.x - getRadius(), center.x + getRadius(),
                        center.y - getRadius(), center.y + getRadius()));
    }
}
