package nesting.geometry.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nesting.svg.*;

/**
 * A connected series of line segments.
 */
public class PolygonalChain implements Drawable {

    /**
     * The vertices of the polygonal chain.
     */
    public final List<Point> vertices;

    private List<LineSegment> lineSegments;

    /**
     * Constructs a polygonal chain from the given points, first to last.
     * 
     * @param vertices the vertices of the polygonal chain
     */
    public PolygonalChain(List<Point> vertices) {
        this.vertices = vertices;
    }

    private void setLineSegments() {
        lineSegments = new ArrayList<>();
        for (int i = 0; i < vertices.size() - 1; i++)
            lineSegments
                    .add(new LineSegment(vertices.get(i), vertices.get(i + 1)));
    }

    /**
     * Returns the line segments forming the polygonal chain.
     * 
     * @return the line segments forming the polygonal chain
     */
    public List<LineSegment> getLineSegments() {
        if (lineSegments == null) setLineSegments();
        return lineSegments;
    }

    @Override
    public String toString() {
        return "vertices = " + vertices.stream().map(v -> v.toString())
                .collect(Collectors.joining(" "));
    }

    @Override
    public SVGElement toSVGElement() {
        String string = "<polyline points=\""
                + vertices.stream().map(p -> p.toSVGCoordinates())
                        .collect(Collectors.joining(" "))
                + "\" " + SVG.getStyle() + "/>\n";
        SVGBounds bounds = AxisAlignedRectangle.getBoundingBox(this)
                .getSVGBounds();
        return new SVGElement(string, bounds);
    }

}
