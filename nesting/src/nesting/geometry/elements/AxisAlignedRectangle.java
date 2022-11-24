package nesting.geometry.elements;

import java.util.List;
import java.util.stream.Collectors;

import nesting.svg.SVGBounds;
import nesting.tuple.PolygonSet;

/**
 * A rectangle whose sides are aligned with the x- and y-axes of the coordinate system. The main
 * purpose of this class is to get a quick and easy bounding box of a polygon.
 */
public class AxisAlignedRectangle extends Rectangle {

    /**
     * Constructs an axis-aligned rectangle with the given position and dimensions.
     * 
     * @param bottomLeftCorner the bottom left corner of the rectangle
     * @param width            the width of the rectangle
     * @param height           the height of the rectangle
     */
    public AxisAlignedRectangle(Point bottomLeftCorner, double width,
            double height) {
        super(bottomLeftCorner, width, height, 0.0);
    }

    /**
     * Returns the axis-aligned bounding box for the polygon by determining minimum and maximum x- and
     * y-coordinates.
     * 
     * @param polygon a polygon
     * @return the axis-aligned bounding box for the polygon
     */
    public static AxisAlignedRectangle getBoundingBox(Polygon polygon) {
        return getBoundingBox(polygon.vertices);
    }

    /**
     * Returns the axis-aligned bounding box for the polygon set by determining minimum and maximum x-
     * and y-coordinates.
     * 
     * @param set a polygon set
     * @return the axis-aligned bounding box for the polygon set
     */
    public static AxisAlignedRectangle getBoundingBox(PolygonSet set) {
        return getBoundingBox(set.getPolygons().stream()
                .flatMap(polygon -> polygon.vertices.stream())
                .collect(Collectors.toList()));
    }

    /**
     * Returns the axis-aligned bounding box for the polygonal chain by determining minimum and maximum
     * x- and y-coordinates.
     * 
     * @param polygonalChain a polygonal chain
     * @return the axis-aligned bounding box for the polygonal chain
     */
    public static AxisAlignedRectangle getBoundingBox(
            PolygonalChain polygonalChain) {
        return getBoundingBox(polygonalChain.vertices);
    }

    /**
     * Returns the axis-aligned bounding box for the set of points by determining minimum and maximum x-
     * and y-coordinates.
     * 
     * @param points a list of points
     * @return the axis-aligned bounding box for the set of points
     */
    public static AxisAlignedRectangle getBoundingBox(List<Point> points) {
        double minX = points.stream().mapToDouble(v -> v.x).min().getAsDouble();
        double maxX = points.stream().mapToDouble(v -> v.x).max().getAsDouble();
        double minY = points.stream().mapToDouble(v -> v.y).min().getAsDouble();
        double maxY = points.stream().mapToDouble(v -> v.y).max().getAsDouble();
        return new AxisAlignedRectangle(new Point(minX, minY), maxX - minX,
                maxY - minY);
    }

    /**
     * Returns the center of the axis-aligned rectangle. Mainly used to get the vertex for point
     * reflection.
     * 
     * @return the center of this axis-aligned rectangle
     */
    public Point getCenter() {
        return new Point(bottomLeftCorner.x + (width / 2),
                bottomLeftCorner.y + (height / 2));
    }

    public SVGBounds getSVGBounds() {
        return new SVGBounds(bottomLeftCorner.x, bottomLeftCorner.x + width,
                bottomLeftCorner.y, bottomLeftCorner.y + height);
    }

}
