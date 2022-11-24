package nesting.geometry.elements;

import nesting.svg.*;
import nesting.util.CircularArrayList;
import nesting.util.CircularList;

/**
 * A quadrilateral with four right angles. Rectangles can be oriented arbitrarily in the plane. They
 * are defined by the position of the bottom left corner, a width, a height and the angle between
 * its base (the edge originating from the bottom-left corner in counter-clockwise order) and the
 * positive x-axis.
 */
public class Rectangle implements Drawable {

    /**
     * The bottom left corner of the rectangle. (Perhaps somewhat counter-intuitively, "bottom" and
     * "left" are used relative to the axis-aligned version of the rectangle. Thus, the bottom left
     * corner may not actually be bottom left if the rectangle is rotated more than 90 degrees.)
     */
    public final Point bottomLeftCorner;

    /**
     * The width of the rectangle.
     */
    public final double width;

    /**
     * The height of the rectangle.
     */
    public final double height;

    /**
     * The angle between the base of the rectangle and the positive x-axis in radians.
     */
    public final double angleToXAxis;

    private CircularList<Point> vertices;

    /**
     * Constructs a rectangle with the given position, dimensions and orientation.
     * 
     * @param bottomLeftCorner the bottom left corner of the rectangle
     * @param width            the width of the rectangle
     * @param height           the height of the rectangle
     * @param angleToXAxis     the angle between the base of the rectangle and the positive x-axis in
     *                         radians
     */
    public Rectangle(Point bottomLeftCorner, double width, double height,
            double angleToXAxis) {
        this.bottomLeftCorner = bottomLeftCorner;
        this.width = width;
        this.height = height;
        this.angleToXAxis = angleToXAxis;
    }

    private void setVertices() {
        CircularList<Point> list = new CircularArrayList<>();
        list.add(bottomLeftCorner); // = vertices.get(0)
        Point bottomRightCorner = Point.rotate(
                new Point(bottomLeftCorner.x + width, bottomLeftCorner.y),
                bottomLeftCorner, angleToXAxis);
        list.add(bottomRightCorner); // = vertices.get(1)
        Point topRightCorner = Point.rotate(
                new Point(bottomLeftCorner.x + width,
                        bottomLeftCorner.y + height),
                bottomLeftCorner, angleToXAxis);
        list.add(topRightCorner); // = vertices.get(2)
        Point topLeftCorner = Point.rotate(
                new Point(bottomLeftCorner.x, bottomLeftCorner.y + height),
                bottomLeftCorner, angleToXAxis);
        list.add(topLeftCorner); // = vertices.get(3)
        vertices = list;
    }

    /**
     * Returns the area of the rectangle, i.e. the product of width and height.
     * 
     * @return the area of the rectangle
     */
    public double getArea() {
        return width * height;
    }

    /**
     * Returns a list of the vertices (corners) of the rectangle, starting with the bottom-left corner,
     * in counter-clockwise order. This is sometimes useful when we want to treat the rectangle as a
     * polygon.
     * 
     * @return a list of the vertices of the rectangle in counter-clockwise order
     */
    public CircularList<Point> getVertices() {
        if (vertices == null) setVertices();
        return vertices;
    }

    /**
     * Creates an axis-aligned version of this rectangle from its bottom-left corner, width and height.
     * 
     * @return the axis-aligned version of the given rectangle
     */
    public AxisAlignedRectangle align() {
        return new AxisAlignedRectangle(bottomLeftCorner, width, height);
    }

    @Override
    public SVGElement toSVGElement() {
        Point topLeftCorner = new Point(bottomLeftCorner.x,
                bottomLeftCorner.y + height);
        String string = "<rect x=\"" + topLeftCorner.x + "\" y=\""
                + (-1) * topLeftCorner.y + "\" width=\"" + width
                + "\" height=\"" + height + "\" transform=\"rotate("
                + (-1) * Math.toDegrees(angleToXAxis) + ","
                + bottomLeftCorner.toSVGCoordinates() + ")\"" + SVG.getStyle()
                + "/>\n";
        AxisAlignedRectangle box = AxisAlignedRectangle
                .getBoundingBox(getVertices());
        return new SVGElement(string, box.getSVGBounds());
    }

    @Override
    public String toString() {
        return "OrientedRectangle(" + bottomLeftCorner.toString() + ", " + width
                + ", " + height + ", " + angleToXAxis + ")";
    }
}