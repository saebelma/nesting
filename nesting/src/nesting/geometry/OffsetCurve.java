package nesting.geometry;

import nesting.geometry.elements.*;
import nesting.irregular.NestingParameters;

/**
 * For nesting, the parallel curve to a polygon is not exactly what we want because at concave
 * corners of the outline there are points in the parallel curve which don't have the minimum
 * distance to <i>all</i> points of the polygon. In order to get this object, which I have termed
 * the <i>offset curve</i> we have to eliminate self-intersections of the curve.
 * 
 * The algorithm first computes the polygonized parallel curve and then removes self-intersections
 * one-by-one by the following process: We take a point on the convex hull of the polygon and check
 * the edge originating at this vertex against all following line segments (excepting consecutive
 * segments). If there is a self-intersection, we remove all line segments between the intersecting
 * segments and replace the end or start points of the intersecting segments with the point of
 * intersection. Then the process is started over. If there is no self-intersection, we go to the
 * next vertex.
 * 
 * In theory, this brute-force algorithm has a run time of <i>O</i>(<i>n</i><sup>3</sup>), but in
 * practice there aren't that many self-intersections and the run time is negligible.
 */
public class OffsetCurve {

    private OffsetCurve() {
    };

    /**
     * Returns the offset curve of the given polygon. The offset is equal to the part clearance
     * parameter.
     * 
     * @param polygon a polygon
     * @return the offset curve of the polygon
     */
    public static Polygon of(Polygon polygon) {
        return of(polygon, NestingParameters.partClearance);
    }

    /**
     * Returns an offset curve of the given polygon with the given offset.
     * 
     * @param polygon a polygon
     * @param r       the offset in mm
     * @return an offset curve of the polygon
     */
    public static Polygon of(Polygon polygon, double r) {

        Polygon offsetCurve = PolygonizedParallelCurve.of(polygon, r);

        // Pick a point on convex hull (guaranteed to be on outside)
        Point pointOnConvexHull = ConvexHull.of(offsetCurve.vertices).get(0);

        // Remove self-intersections one-by-one
        while (true) {
            Polygon result = removeSelfIntersection(offsetCurve,
                    pointOnConvexHull);
            if (result != null)
                offsetCurve = result;
            else
                break;
        }

        return offsetCurve;
    }

    private static Polygon removeSelfIntersection(Polygon polygon,
            Point startPoint) {

        // Loop over points starting from extreme point
        int i_start = polygon.vertices.indexOf(startPoint);
        int i = 0;
        while (i < polygon.vertices.size()) {

            // Loop over remaining points
            int j = i;
            while (j < polygon.vertices.size()) {

                // Don't check consecutive segments
                if (!polygon.vertices.consecutive(i, j)) {
                    Point intersection = LineSegment.getIntersection(
                            polygon.getEdges().get(i_start + i),
                            polygon.getEdges().get(i_start + j));
                    if (intersection != null) {

                        // Replace one vertex with intersection, delete the rest
                        polygon.vertices.set(i_start + i + 1, intersection);
                        polygon.vertices.removeRange(i_start + i + 2,
                                i_start + j + 1);

                        // Return new polygon
                        return new Polygon(polygon.vertices);
                    }
                }
                j++;
            }
            i++;
        }

        return null;
    }
}
