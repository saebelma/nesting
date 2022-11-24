package nesting.geometry.elements;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import nesting.svg.*;
import nesting.util.CircularArrayList;
import nesting.util.CircularList;

/**
 * A plane figure that is described by a finite number of straight line segments connected to form a
 * closed polygonal chain. For the purposes of nesting, polygons should be simple and the vertices
 * should be in counter-clockwise order. For performance reasons, neither of these properties is
 * checked in the constructor, although there is an optional method for ensuring the
 * counter-clockwise order of vertices.
 */
public class Polygon implements Drawable {

    /**
     * The polygon's vertices.
     */
    public final CircularList<Point> vertices;

    private CircularList<DirectedLineSegment> edges;
    private CircularList<Double> angles;
    private AxisAlignedRectangle boundingBox;

    public final static BiFunction<Polygon, Point, Double> maximumDistanceOfVertexToPoint = (
            polygon, point) -> polygon.vertices.stream()
                    .mapToDouble(vertex -> Point.distance(vertex, point)).max()
                    .getAsDouble();

    /**
     * Constructs a polygon from the given vertices.
     * 
     * @param vertices the polygon's vertices
     */
    public Polygon(List<Point> vertices) {
        this.vertices = new CircularArrayList<>(vertices);
    }

    private void setEdges() {
        edges = new CircularArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            edges.add(new DirectedLineSegment(vertices.get(i),
                    vertices.get(i + 1)));
        }
    }

    private void setAngles() {
        angles = new CircularArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            Vector vector1 = getEdges().get(i).getVector();
            Vector vector2 = getEdges().get(i - 1).getReverseVector();
            double angle = Vector.getAngle(vector1, vector2);
            angles.add(new Double(angle));
        }
    }

    /**
     * Checks if the vertices are in counter-clockwise order by computing the sum of inside angles. If
     * not, the order of vertices is reversed.
     */
    public void ensureCounterclockwiseOrderOfVertices() {
        double sumOfAngles = getAngles().stream().mapToDouble(a -> a).sum();
        if (sumOfAngles > vertices.size() * Math.PI) {
            Collections.reverse(vertices);
            edges = null; // Have to reset edges and angles
            angles = null;
        }
    }

    /**
     * Returns a copy of this polygon rotated by 180 degrees around the center of its bounding box.
     * 
     * @return a copy of this polygon rotated by 180 degrees
     */
    public Polygon rotate() {
        return rotate(getBoundingBox().getCenter());
    }

    /**
     * Returns a copy of this polygon rotated by 180 degrees around the given point.
     * 
     * @param origin the vertex of the rotation
     * @return a copy of this polygon rotated by 180 degrees
     */
    public Polygon rotate(Point origin) {
        return new Polygon(vertices.stream()
                .map(p -> new Point(origin.x - (p.x - origin.x),
                        origin.y - (p.y - origin.y)))
                .collect(Collectors.toList()));
    }

    /**
     * Returns a copy of this polygon rotated by the given angle around the given point.
     * 
     * @param origin the vertex of the rotation
     * @param angle  the angle of rotation in counter-clockwise direction in radians
     * @return a copy of this polygon rotated by 180 degrees
     */
    public Polygon rotate(Point origin, double angle) {
        return new Polygon(vertices.stream().map(p -> p.rotate(origin, angle))
                .collect(Collectors.toList()));
    }

    /**
     * Returns a copy of this polygon translated by the given vector.
     * 
     * @param vector a vector
     * @return a copy of this polygon translated by the given vector
     */
    public Polygon translate(Vector vector) {
        return new Polygon(vertices.stream()
                .map(p -> new Point(p.x + vector.x, p.y + vector.y))
                .collect(Collectors.toList()));
    }

    /**
     * Returns a copy of this polygon translated by the given integer vector.
     * 
     * @param vector an integer vector
     * @return a copy of this polygon translated by the given vector
     */
    public Polygon translate(IntegerVector vector) {
        return new Polygon(vertices.stream()
                .map(p -> new Point(p.x + vector.x, p.y + vector.y))
                .collect(Collectors.toList()));
    }

    /**
     * Checks if this polygon intersects another polygon by a brute-force algorithm. See
     * {@link nesting.geometry.elements.LineSegment#doIntersect(List, List)}.
     * 
     * @param polygon a polygon
     * @return <code>true</code> if this polygon intersects the given polygon
     */
    public boolean intersects(Polygon polygon) {
        return LineSegment.doIntersect(this.getEdges(), polygon.getEdges());
    }

    /**
     * Returns the area of this polygon, which is computed by the cross product around all vertices.
     * 
     * @return the area of this polygon
     */
    public double calculateArea() {
        double total = 0;
        for (int i = 0; i < vertices.size(); i++) {
            total += vertices.get(i).x * vertices.get(i + 1).y;
            total -= vertices.get(i + 1).x * vertices.get(i).y;
        }
        return Math.abs(total) / 2;
    }

    /**
     * Checks if this polygon contains the given integer point.
     * 
     * @param point an integer point
     * @return <code>true</code> if this polygon contains the given integer point
     */
    public boolean contains(IntegerVector point) {
        return contains(new Point(point.x, point.y));
    }

    /**
     * Checks if this polygon contains the given point by checking whether it's to left of all edges of
     * the polygon. See
     * {@link nesting.geometry.elements.DirectedLineSegment#isLeftOf(Point, DirectedLineSegment)}.
     * 
     * @param point a point
     * @return <code>true</code> if this polygon contains the given integer point
     */
    public boolean contains(Point point) {
        for (DirectedLineSegment edge : getEdges())
            if (!DirectedLineSegment.isLeftOf(point, edge)) return false;
        return true;
    }

    /**
     * Returns the polygon's edges as a circular list of directed line segments oriented in
     * counter-clockwise direction.
     * 
     * @return the polygon's edges
     */
    public CircularList<DirectedLineSegment> getEdges() {
        if (edges == null) setEdges();
        return edges;
    }

    /**
     * Returns the polygon's inside angles in radians.
     * 
     * @return the polygon's inside angles in radians
     */
    public CircularList<Double> getAngles() {
        if (angles == null) setAngles();
        return angles;
    }

    /**
     * Returns the polygon's axis-aligned bounding box.
     * 
     * @return the polygon's axis-aligned bounding box.
     */
    public AxisAlignedRectangle getBoundingBox() {
        if (boundingBox == null)
            boundingBox = AxisAlignedRectangle.getBoundingBox(this);
        return boundingBox;
    }

    /**
     * Returns the polygon's reference point, which is the bottom-left corner of its axis-aligned
     * bounding box.
     * 
     * @return the polygon's reference point.
     */
    public Point getReferencePoint() {
        return getBoundingBox().bottomLeftCorner;
    }

    public Polygon normalize() {
        Vector centerToOrigin = new Vector(getBoundingBox().getCenter(),
                Point.ORIGIN);
        return this.translate(centerToOrigin);
    }

    @Override
    public String toString() {
        return "Polygon(" + vertices.stream().map(v -> v.toString())
                .collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public SVGElement toSVGElement() {
        String string = "<polygon points=\""
                + vertices.stream().map(v -> v.toSVGCoordinates())
                        .collect(Collectors.joining(" "))
                + "\" " + SVG.getStyle() + "/>\n";
        SVGBounds bounds = AxisAlignedRectangle.getBoundingBox(this)
                .getSVGBounds();
        return new SVGElement(string, bounds);
    }
}
