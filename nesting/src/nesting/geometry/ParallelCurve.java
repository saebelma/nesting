package nesting.geometry;

import java.util.stream.Collectors;

import nesting.geometry.elements.*;
import nesting.irregular.NestingParameters;
import nesting.svg.*;
import nesting.util.CircularArrayList;
import nesting.util.CircularList;

/**
 * Calculates the parallel curve to a simple polygon. The parallel curve consists of two kinds of
 * elements:
 * <ul>
 * <li>for each edge of the polygon, there is a parallel edge with a normal distance equivalent to
 * the part clearance;</li>
 * <li>for each vertex of the polygon, there is a circular arc with a radius equivalent to the
 * normal distance and angles corresponding to the normal of the edges connected by the vertex.</li>
 * </ul>
 * Since there is always a parallel segment alternating with a circular arc, the parallel curve is
 * given as two separate lists of directed line segments and circular arcs. The arc you get with
 * <code> getCircularArcs().get(i)</code> is the arc connecting the segments you get with
 * <code>getParallelSegments().get(i)</code> and <code>getParallelSegments().get(i + 1)</code>,
 * respectively.
 */
public class ParallelCurve implements Drawable {

    private CircularList<DirectedLineSegment> parallelSegments;
    private CircularList<CircularArc> circularArcs;

    /**
     * Constructs the parallel curve with a normal distance equivalent to the part clearance parameter
     * for the given simple polygon.
     * 
     * @param polygon a simple polygon
     */
    public ParallelCurve(Polygon polygon) {
        this(polygon, NestingParameters.partClearance);
    }

    /**
     * Constructs the parallel curve with a given normal distance for the given simple polygon.
     * 
     * @param polygon        a simple polygon
     * @param normalDistance the normal distance from the polygon edge to the parallel curve in
     *                       millimeters
     */
    public ParallelCurve(Polygon polygon, double normalDistance) {

        parallelSegments = polygon.getEdges().stream()
                .map(dls -> dls.getParallelSegment(normalDistance))
                .collect(Collectors.toCollection(CircularArrayList::new));

        circularArcs = new CircularArrayList<>();

        for (int i = 0; i < polygon.getEdges().size(); i++) {

            // Calculate angle between current and next segment
            double angle = Vector.getAngle(
                    polygon.getEdges().get(i + 1).getVector(),
                    polygon.getEdges().get(i).getReverseVector());

            // If vertex is convex (or a straight angle), add circular arc
            if (angle <= Math.PI)
                circularArcs.add(new CircularArc(polygon.getEdges().get(i).b,
                        parallelSegments.get(i).b,
                        parallelSegments.get(i + 1).a));

            // Otherwise, add reverse arc
            else
                circularArcs.add(new CircularArc(polygon.getEdges().get(i).b,
                        parallelSegments.get(i + 1).a,
                        parallelSegments.get(i).b));
        }
    }

    /**
     * Gets the parallel segments of this parallel curve.
     * 
     * @return a circular list of the parallel segments in the parallel curve
     */
    public CircularList<DirectedLineSegment> getParallelSegments() {
        return parallelSegments;
    }

    /**
     * Gets the circular arcs of this parallel curve.
     * 
     * @return a circular list of the circular arcs in the parallel curve
     */
    public CircularList<CircularArc> getCircularArcs() {
        return circularArcs;
    }

    @Override
    public SVGElement toSVGElement() {
        SVGGroup group = new SVGGroup();
        parallelSegments.stream().forEach(ls -> group.add(ls.toSVGElement()));
        circularArcs.stream().forEach(ls -> group.add(ls.toSVGElement()));
        return group.toSVGElement();
    }
}
