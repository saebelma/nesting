package nesting.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nesting.geometry.elements.*;
import nesting.irregular.NestingParameters;
import nesting.util.CircularArrayList;
import nesting.util.CircularList;

/**
 * This is a polygonized version of {@link nesting.geometry.ParallelCurve}. Many packages simply
 * choose points on the curve for polygonization, but in order to maintain the required minimum
 * distance for nesting, we have be careful to calculate <i>tangents</i> to the arcs.
 * 
 * The grain of the polygonization can be controlled by choosing a value for the maximum normal
 * distance between a point on the arc and the tangent edge. In practice, the default value of 1 mm
 * gives a good balance between fit and complexity.
 */
public class PolygonizedParallelCurve {

    private PolygonizedParallelCurve() {
    };

    /**
     * Returns a polygon containing a polygonization of the parallel curve to a given polygon. By
     * default, the distance of the parallel curve to the polygon is equal to the part clearance
     * parameter.
     * 
     * @param polygon a polygon
     * @return the default polygonized parallel curve to the given polygon
     */
    public static Polygon of(Polygon polygon) {
        return of(polygon, NestingParameters.partClearance);
    }

    /**
     * Returns a polygon containing a polygonization of the parallel curve with the given distance to a
     * given polygon.
     * 
     * @param polygon a polygon
     * @param r       the distance between parallel curve and polygon
     * @return the polygonized parallel curve with the given distance to the given polygon
     */
    public static Polygon of(Polygon polygon, double r) {

        // Get line segments parallel to polygon edges
        CircularList<DirectedLineSegment> parallelSegments = polygon.getEdges()
                .stream().map(dls -> dls.getParallelSegment(r))
                .collect(Collectors.toCollection(CircularArrayList::new));

        // Calculate angle that can be covered by introducing one new vertex
        double delta = NestingParameters.maximumNormalDistanceForPolygonization;
        double alpha = 2
                * Math.asin(Math.sqrt(Math.pow(r + delta, 2) - Math.pow(r, 2))
                        / (r + delta));

        List<Point> polygonVertices = new ArrayList<Point>();

        for (int i = 0; i < polygon.vertices.size(); i++) {

            // Construct circular arc at polygon vertex
            double polygonAngle = Vector.getAngle(
                    polygon.getEdges().get(i + 1).getVector(),
                    polygon.getEdges().get(i).getReverseVector());
            CircularArc arc = polygonAngle <= Math.PI
                    ? new CircularArc(polygon.getEdges().get(i).b,
                            parallelSegments.get(i).b,
                            parallelSegments.get(i + 1).a)
                    : new CircularArc(polygon.getEdges().get(i).b,
                            parallelSegments.get(i + 1).a,
                            parallelSegments.get(i).b);

            // Calculate number of vertices, angle at vertices and delta relative to radius
            int n_vertices = (int) Math.ceil(arc.getCentralAngle() / alpha);
            double alpha_prime = arc.getCentralAngle() / n_vertices;
            double delta_prime = (r * (1 - Math.cos(alpha_prime / 2)))
                    / Math.cos(alpha_prime / 2);

            // Calculate vertices in counter-clockwise order
            for (int j = 0; j < n_vertices; j++) {
                double vertexAngle = polygonAngle <= Math.PI
                        ? arc.getAngleA() + (2 * j + 1) * (alpha_prime / 2)
                        : arc.getAngleB() - (2 * j + 1) * (alpha_prime / 2);
                polygonVertices.add(new Point(
                        Math.cos(vertexAngle) * (r + delta_prime)
                                + arc.center.x,
                        Math.sin(vertexAngle) * (r + delta_prime)
                                + arc.center.y));
            }
        }

        return new Polygon(polygonVertices);
    }
}
