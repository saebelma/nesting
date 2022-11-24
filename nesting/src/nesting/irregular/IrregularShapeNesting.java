package nesting.irregular;

import static nesting.irregular.NoFitSpace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nesting.geometry.OffsetCurve;
import nesting.geometry.elements.*;
import nesting.svg.*;
import nesting.svg.SVG.Style;

/**
 * A simple implementation of irregular shape nesting in a circular area. Instances of a polygon are
 * placed one-by-one starting from a placement at the center of the circle. Polygons can be placed
 * in normal orientation or rotated by 180 degrees. A placement is chosen according to the specified
 * nesting criterion. The algorithm works by initializing and updating two instances of
 * <code>SearchSpace</code>, one for normal placements, one for rotated placements. Both search
 * spaces are evaluated using an implementation of <code>NestingCriterion</code>.
 */
public class IrregularShapeNesting implements Drawable {

    private static boolean useSmallestEnclosingCircleCriterion = false;
    private static boolean showOffsetCurves = true;
    private static boolean showPolygons = true;

    private Polygon polygon_normal;
    private Polygon polygon_rotated;
    private Polygon offsetCurve_normal;
    private Polygon offsetCurve_rotated;
    private SearchSpace searchSpace_normalPolygons;
    private SearchSpace searchSpace_rotatedPolygons;
    private NestingCriterion nestingCriterion;
    private NoFitSpace noFitSpace;
    private List<IntegerVector> placements_normal = new ArrayList<>();;
    private List<IntegerVector> placments_rotated = new ArrayList<>();

    /**
     * Constructs a simple irregular shape nesting algorithm for the given polygon.
     * 
     * @param polygon the polygon to be nested
     * @throws IOException if something goes wrong with the csv file
     */
    public IrregularShapeNesting(Polygon polygon) {

        // We normalize the polygon so that a polygon placed with vector of (0,0) is at the center of the
        // table
        polygon_normal = polygon.normalize();
        polygon_rotated = polygon_normal.rotate();

        // Get offset curves
        offsetCurve_normal = OffsetCurve.of(polygon_normal);
        offsetCurve_rotated = offsetCurve_normal.rotate();

        // Get search spaces
        searchSpace_normalPolygons = new SearchSpace();
        searchSpace_rotatedPolygons = new SearchSpace();

        // Get nesting criterion
        nestingCriterion = useSmallestEnclosingCircleCriterion
                ? new SmallestEnclosingCircleCriterion()
                : new ConvexHullCriterion();

        // Get fit and no-fit spaces
        noFitSpace = new NoFitSpace(offsetCurve_normal);

        // Nesting loop
        boolean placementSuccessful = true;
        while (placementSuccessful)
            placementSuccessful = nestOne();
    }

    private boolean nestOne() {

        if (placements_normal.isEmpty()) {
            addNormalPlacement(IntegerVector.ORIGIN);
            return true;
        } else {
            ConvexHullCriterion.Result normalResult = nestingCriterion.evaluate(
                    searchSpace_normalPolygons.getFitPointsTotal(),
                    offsetCurve_normal);
            ConvexHullCriterion.Result rotatedResult = nestingCriterion
                    .evaluate(searchSpace_rotatedPolygons.getFitPointsTotal(),
                            offsetCurve_rotated);

            if (normalResult == null && rotatedResult == null)
                return false;
            else if (normalResult != null && (rotatedResult == null
                    || normalResult.evaluation < rotatedResult.evaluation)) {
                addNormalPlacement(normalResult.position);
                return true;
            } else {
                addRotatedPlacement(rotatedResult.position);
                return true;
            }
        }
    }

    private void addNormalPlacement(IntegerVector position) {
        placements_normal.add(position);
        searchSpace_normalPolygons.addPolygon(position,
                noFitSpace.getFitPoints().get(CASE_NORMAL_NORMAL),
                noFitSpace.getNoFitPoints().get(CASE_NORMAL_NORMAL),
                offsetCurve_normal);
        searchSpace_rotatedPolygons.addPolygon(position,
                noFitSpace.getFitPoints().get(CASE_NORMAL_ROTATED),
                noFitSpace.getNoFitPoints().get(CASE_NORMAL_ROTATED),
                offsetCurve_rotated);
        nestingCriterion.addPolygon(position, offsetCurve_normal);
    }

    private void addRotatedPlacement(IntegerVector position) {
        placments_rotated.add(position);
        searchSpace_normalPolygons.addPolygon(position,
                noFitSpace.getFitPoints().get(CASE_ROTATED_NORMAL),
                noFitSpace.getNoFitPoints().get(CASE_ROTATED_NORMAL),
                offsetCurve_normal);
        searchSpace_rotatedPolygons.addPolygon(position,
                noFitSpace.getFitPoints().get(CASE_ROTATED_ROTATED),
                noFitSpace.getNoFitPoints().get(CASE_ROTATED_ROTATED),
                offsetCurve_rotated);
        nestingCriterion.addPolygon(position, offsetCurve_rotated);
    }

    /**
     * Draws the table edge and the polygons and/or circles in the packing. If only one is selected, the
     * elements are drawn with solid lines. If both polygons and circles are selected, the circles are
     * drawn with dashed lines.
     */
    @Override
    public SVGElement toSVGElement() {
        SVGGroup group = new SVGGroup();
        SVG.setStyle(Style.SOLID);
        group.add(Circle.TABLE.toSVGElement());
        if (showPolygons) {
            placements_normal.stream()
                    .map(vector -> polygon_normal.translate(vector))
                    .forEach(polygon -> group.add(polygon.toSVGElement()));
            placments_rotated.stream()
                    .map(vector -> polygon_rotated.translate(vector))
                    .forEach(polygon -> group.add(polygon.toSVGElement()));
        }
        if (showOffsetCurves) {
            SVG.setStyle(showPolygons ? Style.DASHED : Style.SOLID);
            placements_normal.stream()
                    .map(vector -> offsetCurve_normal.translate(vector))
                    .forEach(polygon -> group.add(polygon.toSVGElement()));
            placments_rotated.stream()
                    .map(vector -> offsetCurve_rotated.translate(vector))
                    .forEach(polygon -> group.add(polygon.toSVGElement()));
        }
        return group.toSVGElement();
    }

    /**
     * If set to <code>true</code> an instance of <code>SmallestEnclosingCircleCriterion</code> is used
     * for evaluation; otherwise an instance of <code>ConvexHullCriterion</code>. The default value is
     * <code>false</code>, i.e. the convex hull criterion is used.
     * 
     * @param useSmallestEnclosingCircleCriterion if <code>true</code> an instance of
     *                                            <code>SmallestEnclosingCircleCriterion</code> is used
     *                                            for evaluation; otherwise an instance of
     *                                            <code>ConvexHullCriterion</code>
     */
    public static void setUseSmallestEnclosingCircleCriterion(
            boolean useSmallestEnclosingCircleCriterion) {
        IrregularShapeNesting.useSmallestEnclosingCircleCriterion = useSmallestEnclosingCircleCriterion;
    }

    /**
     * If <code>true</code>, polygons are drawn in the <code>toSVGElement</code> method.
     * 
     * @param showPolygons if <code>true</code>, polygons are drawn
     */
    public static void setShowPolygons(boolean showPolygons) {
        IrregularShapeNesting.showPolygons = showPolygons;
    }

    /**
     * If <code>true</code>, offset curves are drawn in the <code>toSVGElement</code> method.
     * 
     * @param setShowOffsetCurves if <code>true</code>, offset curves are drawn
     */
    public static void setShowOffsetCurves(boolean showOffsetCurves) {
        IrregularShapeNesting.showOffsetCurves = showOffsetCurves;
    }

    /**
     * Returns the number of polygons that were placed.
     * 
     * @return the number of polygons placed
     */
    public int getN() {
        return placements_normal.size() + placments_rotated.size();
    }

}
