package nesting.irregular;

import java.util.*;
import java.util.stream.Collectors;

import nesting.geometry.ConvexHull;
import nesting.geometry.RandomizedIncrementalConstruction;
import nesting.geometry.elements.*;

/**
 * Calculates and updates the total convex hull of polygons placed on the table and evaluates search
 * positions based on the area of the smallest enclosing circle of the total convex hull points. The
 * convex hull is used only in order to keep the number of points as low as possible. Evaluating
 * search positions is the most time-critical step of the nesting process.
 */
public class SmallestEnclosingCircleCriterion extends NestingCriterion {

    /**
     * Constructs a blank smallest enclosing circle criterion.
     */
    public SmallestEnclosingCircleCriterion() {
    };

    private List<Point> convexHull_total = new ArrayList<>();
    private List<Result> allResults;

    /**
     * Updates the criterion by adding a polygon at a certain position. Performs the following
     * operations:
     * <ul>
     * <li>adds the vertices of the new polygon to the list of convex hull points - O(m) time</li>
     * <li>recalculates the total convex hull - O(n log n)</li>
     * </ul>
     * 
     * @param position position of a newly placed polygon
     * @param polygon  the polygon
     */
    @Override
    public void addPolygon(IntegerVector position, Polygon polygon) {

        // Add convex hull points at position: O(m)
        convexHull_total
                .addAll(polygon.vertices.stream()
                        .map(vertex -> new Point(position.x + vertex.x,
                                position.y + vertex.y))
                        .collect(Collectors.toList()));

        // Recalculate total convex hull: O(n log n)
        convexHull_total = ConvexHull.of(convexHull_total);
    }

    /**
     * Evaluates a collection of search positions at which a certain polygon is to be placed based on
     * the area of the smallest enclosing circle of the total arrangement of polygons. Returns the
     * minimal result. For each position, the following operations are performed:
     * <ul>
     * <li>clone the total convex hull tree set - O(n) according to Java SE TreeMap constructor
     * source</li>
     * <li>add new convex hull points at search position - O(m log n)</li>
     * <li>recalculate convex hull - O(n)</li>
     * <li>evaluate the position by calculating the area of the convex hull polygon - O(n)</li>
     * </ul>
     * 
     * @param positions a collection of positions to evaluate
     * @param polygon   the polygon to be placed at those positions
     * @return the best result
     */
    @Override
    public Result evaluate(Collection<IntegerVector> positions,
            Polygon polygon) {

        allResults = new ArrayList<>();
        for (IntegerVector position : positions) {

            // Copy the total convex hull: O(n)
            List<Point> clone = new ArrayList<>(convexHull_total);

            // Add convex hull at search position: O(m)
            clone.addAll(polygon.vertices.stream()
                    .map(vertex -> vertex.translate(position))
                    .collect(Collectors.toList()));

            // Evaluate: O(n)
            Circle sec = (new RandomizedIncrementalConstruction(clone))
                    .getSmallestEnclosingCircle();
            double evaluation = sec.getArea();

            // Add result
            allResults.add(new Result(position, evaluation));

        }

        // Get result with with smallest area
        Result bestResult = allResults.isEmpty() ? null
                : allResults.stream().min((r_1, r_2) -> Double
                        .compare(r_1.evaluation, r_2.evaluation)).get();

        return bestResult;
    }

    /**
     * Returns a list of all results of the last call to the <code>evaluate</code> method.
     * 
     * @return a list of all results
     */
    @Override
    public List<Result> getAllResults() {
        return (allResults == null || allResults.isEmpty()) ? null
                : mapEvaluationsToRGB(allResults);
    }

    /**
     * Returns the tree set containing the vertices of the current total convex hull.
     * 
     * @return the current total convex hull
     */
    @Override
    public List<Point> getConvexHull_total() {
        return convexHull_total;
    }
}
