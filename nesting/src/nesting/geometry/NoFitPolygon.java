package nesting.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nesting.geometry.elements.*;
import nesting.svg.*;
import nesting.svg.SVG.Style;

/**
 * Implementation of the no-fit polygon for two convex polygons.
 */
public class NoFitPolygon implements Drawable {

    private Polygon fixedPolygon;
    private Polygon orbitingPolygon;
    private Point referencePointFixedPolygon;
    private Point referencePointOrbitingPolygon;
    private Polygon noFitPolygon;

    /**
     * Constructs the no-fit polygon for the specified polygons.
     * 
     * @param fixedPolygon    the fixed polygon
     * @param orbitingPolygon the orbiting polygon
     */
    public NoFitPolygon(Polygon fixedPolygon, Polygon orbitingPolygon) {

        this.fixedPolygon = fixedPolygon;
        this.orbitingPolygon = orbitingPolygon;

        // The reference point of the fixed polygon is its the lowest, left-most vertex. The reference point
        // of the orbiting polygon is its upper-most, right-most vertex.
        referencePointFixedPolygon = fixedPolygon.vertices.stream()
                .min((p_1, p_2) -> (Double.compare(p_1.y, p_2.y) != 0)
                        ? Double.compare(p_1.y, p_2.y)
                        : Double.compare(p_1.x, p_2.x))
                .get();
        referencePointOrbitingPolygon = orbitingPolygon.vertices.stream()
                .max((p_1, p_2) -> (Double.compare(p_1.y, p_2.y) != 0)
                        ? Double.compare(p_1.y, p_2.y)
                        : Double.compare(p_1.x, p_2.x))
                .get();

        // Make sorted edge list
        List<DirectedLineSegment> sortedEdgeList = new ArrayList<>();
        sortedEdgeList.addAll(fixedPolygon.getEdges());
        sortedEdgeList.addAll(orbitingPolygon.getEdges().stream()
                .map(e -> e.getReverseSegment()).collect(Collectors.toList()));
        sortedEdgeList
                .sort((e_1, e_2) -> Double.compare(e_1.getVector().getAngle(),
                        e_2.getVector().getAngle()));

        // Make no fit polygon vertices
        List<Point> noFitPolygonVertices = new ArrayList<>();
        noFitPolygonVertices.add(referencePointFixedPolygon);
        for (int i = 0; i < sortedEdgeList.size() - 1; i++)
            noFitPolygonVertices.add(
                    noFitPolygonVertices.get(noFitPolygonVertices.size() - 1)
                            .translate(sortedEdgeList.get(i).getVector()));

        // Make no fit polygon
        noFitPolygon = new Polygon(noFitPolygonVertices); // Why convex hull?
    }

    /**
     * Returns the fixed polygon.
     * 
     * @return the fixed polygon
     */
    public Polygon getFixedPolygon() {
        return fixedPolygon;
    }

    /**
     * Returns the orbiting polygon.
     * 
     * @return the orbiting polygon
     */
    public Polygon getOrbitingPolygon() {
        return orbitingPolygon;
    }

    /**
     * Returns the reference point of the fixed polygon, which is its lowest, leftmost vertex. This is
     * the reference point specific to the calculation of the no-fit polygon, not to be confused with
     * the polygon's general reference point (which is the bottom-left corner of its bounding box).
     * 
     * @return the reference point of the fixed polygon
     */
    public Point getReferencePointFixedPolygon() {
        return referencePointFixedPolygon;
    }

    /**
     * Returns the reference point of the orbiting polygon, which is its uppermost, rightmost vertex.
     * This is the reference point specific to the calculation of the no-fit polygon, not to be confused
     * with the polygon's general reference point (which is the bottom-left corner of its bounding box).
     * 
     * @return the reference point of the orbiting polygon
     */
    public Point getReferencePointOrbitingPolygon() {
        return referencePointOrbitingPolygon;
    }

    /**
     * Returns the no-fit polygon of the two polygons.
     * 
     * @return the no-fit polygon
     */
    public Polygon getNoFitPolygon() {
        return noFitPolygon;
    }

    /**
     * Returns a graphical representation of the two polygons (solid lines), the corresponding no-fit
     * polygon (dashed lines) and the reference points. The orbiting polygon is shown in its "starting
     * position", i.e. with its reference point superimposed on the fixed polygon's reference point.
     */
    @Override
    public SVGElement toSVGElement() {
        SVGGroup group = new SVGGroup();
        SVG.setStyle(Style.SOLID);
        group.add(fixedPolygon.toSVGElement());
        group.add(orbitingPolygon
                .translate(new Vector(referencePointOrbitingPolygon,
                        referencePointFixedPolygon))
                .toSVGElement());
        group.add(referencePointFixedPolygon.toSVGElement());
        SVG.setStyle(Style.DASHED);
        group.add(noFitPolygon.toSVGElement());
        return group.toSVGElement();
    }
}
