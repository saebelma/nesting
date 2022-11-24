package nesting.tuple;

import java.util.*;
import java.util.stream.Collectors;

import nesting.geometry.elements.*;
import nesting.geometry.elements.Vector;
import nesting.svg.*;

/**
 * A set of polygons forming a unit for the purposes of nesting. Offers methods for translating the
 * set and for testing sets for intersection.
 */
public class PolygonSet implements Drawable {

    private List<Polygon> polygons;

    /**
     * Constructs an empty polygon set.
     */
    public PolygonSet() {
        polygons = new ArrayList<>();
    }

    /**
     * Constructs a polygon set containing the given polygon.
     */
    public PolygonSet(Polygon polygon) {
        polygons = new ArrayList<>();
        polygons.add(polygon);
    }

    /**
     * Constructs a polygon set containing the given polygons.
     * 
     * @param polygons polygons
     */
    public PolygonSet(Polygon... polygons) {
        this.polygons = new ArrayList<>(Arrays.asList(polygons));
    }

    /**
     * Constructs a polygon set from the union of two polygon sets.
     * 
     * @param set_1 a polygon set
     * @param set_2 another polygon set
     */
    public PolygonSet(PolygonSet set_1, PolygonSet set_2) {
        polygons = new ArrayList<>(set_1.getPolygons());
        polygons.addAll(set_2.getPolygons());
    }

    /**
     * Constructs a polygon set from the given polygon sets.
     * 
     * @param sets polygon sets
     */
    public PolygonSet(PolygonSet... sets) {
        polygons = new ArrayList<>();
        Arrays.stream(sets).forEach(set -> polygons.addAll(set.getPolygons()));
    }

    /**
     * Constructs a polygon set containing the given polygons.
     * 
     * @param polygons a list of polygons
     */
    public PolygonSet(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    /**
     * Adds a polygon to the set.
     * 
     * @param polygon a polygon
     */
    public void add(Polygon polygon) {
        polygons.add(polygon);
    }

    /**
     * Translates all polygons in this set by the given vector.
     * 
     * @param vector a vector
     * @return a polygon set with the translated polygons
     */
    public PolygonSet translate(Vector vector) {
        return new PolygonSet(
                polygons.stream().map(polygon -> polygon.translate(vector))
                        .collect(Collectors.toList()));
    }

    /**
     * Translates all polygons in this set by the given integer vector.
     * 
     * @param vector an integer vector
     * @return a polygon set with the translated polygons
     */
    public PolygonSet translate(IntegerVector vector) {
        return new PolygonSet(
                polygons.stream().map(polygon -> polygon.translate(vector))
                        .collect(Collectors.toList()));
    }

    /**
     * Rotates all polygons in this set by the angle around the given origin.
     * 
     * @param origin the origin of the rotation
     * @param angle  an angle in radians
     * @return a polygon set with the rotated polygons
     */
    public PolygonSet rotate(Point origin, double angle) {
        return new PolygonSet(
                polygons.stream().map(polygon -> polygon.rotate(origin, angle))
                        .collect(Collectors.toList()));
    }

    /**
     * Returns <code>true</code> if any polygon in the first set intersects any polygon in the second
     * set.
     * 
     * @param set_1 a polygon set
     * @param set_2 another polygon set
     * @return <code>true</code> if any polygon in the first set intersects any polygon in the second
     *         set
     */
    public static boolean doIntersect(PolygonSet set_1, PolygonSet set_2) {
        return LineSegment.doIntersect(
                set_1.getPolygons().stream()
                        .flatMap(polygon -> polygon.getEdges().stream())
                        .collect(Collectors.toList()),
                set_2.getPolygons().stream()
                        .flatMap(polygon -> polygon.getEdges().stream())
                        .collect(Collectors.toList()));
    }

    /**
     * Returns a list of the polygons in this set.
     * 
     * @return a list of the polygons in this set
     */
    public List<Polygon> getPolygons() {
        return polygons;
    }

    @Override
    public SVGElement toSVGElement() {
        return SVG.collectionToSVGElement(polygons);
    }

    @Override
    public PolygonSet clone() {
        return new PolygonSet(new ArrayList<>(polygons));

    }

}
