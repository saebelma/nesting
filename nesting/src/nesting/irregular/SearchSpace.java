package nesting.irregular;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import nesting.geometry.elements.*;

/**
 * Implements a search space for irregular shape nesting based on the concept of fit / no-fit
 * spaces.
 */
public class SearchSpace {

    private TreeSet<IntegerVector> fitPointsTotal = new TreeSet<>();
    private TreeSet<IntegerVector> noFitPointsTotal = new TreeSet<>();

    /**
     * Constructs a new search space.
     */
    public SearchSpace() {
    };

    /**
     * Modifies the search space so that it reflects the addition of a polygon at a given position.
     * Performs the following operations:
     * <ul>
     * <li>translates fit points to the positions of the newly placed polygon</li>
     * <li>filters them according to whether a polygon placed at that position would fit on the
     * table</li>
     * <li>excludes all fit points that are in the set of total no-fit points</li>
     * <li>adds them to the fit point total</li>
     * <li>translates no-fit points to the positions of the newly placed polygon</li>
     * <li>removes them from the fit point total</li>
     * <li>adds them to the no-fit point total</li>
     * </ul>
     * 
     * @param position    the position of a newly placed polygon
     * @param fitPoints   the list of fit points for this placement
     * @param noFitPoints the list of no-fit points for this placement
     * @param polygon     the polygon that was placed
     */
    public void addPolygon(IntegerVector position,
            List<IntegerVector> fitPoints, List<IntegerVector> noFitPoints,
            Polygon polygon) {

        // Add all fit points that are on table and not in the no-fit points total
        List<IntegerVector> fitPoints_translated = fitPoints.stream()
                .map(fitPoint -> fitPoint.plus(position))
                .collect(Collectors.toList());
        List<IntegerVector> fitPoints_onTable = fitPoints_translated.stream()
                .filter(fitPoint -> polygon.vertices.stream()
                        .map(v -> v.translate(fitPoint))
                        .allMatch(v_translated -> pointOnTable(v_translated)))
                .collect(Collectors.toList());
        fitPointsTotal.addAll(fitPoints_onTable.stream()
                .filter(point -> !noFitPointsTotal.contains(point))
                .collect(Collectors.toList()));

        // Remove no-fit points from search space, add them to no-fit points total
        List<IntegerVector> noFitPoints_translated = noFitPoints.stream()
                .map(noFitPoint -> new IntegerVector(position.x + noFitPoint.x,
                        position.y + noFitPoint.y))
                .collect(Collectors.toList());
        noFitPoints_translated.stream()
                .forEach(noFitPoint -> fitPointsTotal.remove(noFitPoint));
        noFitPointsTotal.addAll(noFitPoints_translated);
    }

    /**
     * Version of <code>addPolygon</code> that doesn't filter according to table area.
     * 
     * @param position    the position of a newly placed polygon
     * @param fitPoints   the list of fit points for this placement
     * @param noFitPoints the list of no-fit points for this placement
     */
    public void addPolygon(IntegerVector position,
            List<IntegerVector> fitPoints, List<IntegerVector> noFitPoints) {

        // Add all fit points that are not in the no-fit points total
        List<IntegerVector> fitPoints_translated = fitPoints.stream()
                .map(fitPoint -> fitPoint.plus(position))
                .collect(Collectors.toList());
        fitPointsTotal.addAll(fitPoints_translated.stream()
                .filter(point -> !noFitPointsTotal.contains(point))
                .collect(Collectors.toList()));

        // Remove no-fit points from search space, add them to no-fit points total
        List<IntegerVector> noFitPoints_translated = noFitPoints.stream()
                .map(noFitPoint -> new IntegerVector(position.x + noFitPoint.x,
                        position.y + noFitPoint.y))
                .collect(Collectors.toList());
        noFitPoints_translated.stream()
                .forEach(noFitPoint -> fitPointsTotal.remove(noFitPoint));
        noFitPointsTotal.addAll(noFitPoints_translated);
    }

    /**
     * Version of <code>addPolygon</code> that filters not according to table area, but according to a
     * predicate provided as an argument.
     * 
     * @param position    the position of a newly placed polygon
     * @param fitPoints   the list of fit points for this placement
     * @param noFitPoints the list of no-fit points for this placement
     * @param filter      a predicate for filtering search positions
     */
    public void addPolygon(IntegerVector position,
            Collection<IntegerVector> fitPoints,
            Collection<IntegerVector> noFitPoints,
            Predicate<IntegerVector> filter) {

        // Add all fit points that are on table and not in the no-fit points total
        List<IntegerVector> fitPoints_translated = fitPoints.stream()
                .map(fitPoint -> fitPoint.plus(position))
                .collect(Collectors.toList());
        List<IntegerVector> fitPoints_filtered = fitPoints_translated.stream()
                .filter(filter).collect(Collectors.toList());
        fitPointsTotal.addAll(fitPoints_filtered.stream()
                .filter(point -> !noFitPointsTotal.contains(point))
                .collect(Collectors.toList()));

        // Remove no-fit points from search space, add them to no-fit points total
        List<IntegerVector> noFitPoints_translated = noFitPoints.stream()
                .map(noFitPoint -> new IntegerVector(position.x + noFitPoint.x,
                        position.y + noFitPoint.y))
                .collect(Collectors.toList());
        noFitPoints_translated.stream()
                .forEach(noFitPoint -> fitPointsTotal.remove(noFitPoint));
        noFitPointsTotal.addAll(noFitPoints_translated);
    }

    private boolean pointOnTable(Point point) {
        return point.x * point.x
                + point.y * point.y < NestingParameters.tableRadius
                        * NestingParameters.tableRadius;
    }

    /**
     * Returns a lexically sorted tree set of the total set of fit points (i.e. the search space).
     * 
     * @return tree set of fit point total
     */
    public TreeSet<IntegerVector> getFitPointsTotal() {
        return fitPointsTotal;
    }

    /**
     * Returns a lexically sorted tree set of the total set of no-fit points
     * 
     * @return tree set of no-fit point total
     */
    public TreeSet<IntegerVector> getNoFitPointsTotal() {
        return noFitPointsTotal;
    }
}
