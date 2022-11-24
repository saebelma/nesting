package nesting.irregular;

import java.util.*;
import java.util.stream.Collectors;

import nesting.geometry.ConvexHull;
import nesting.geometry.elements.*;
import nesting.util.CircularList;

/**
 * Calculates and updates the total convex hull of polygons placed on the table and evaluates search
 * positions based on the area of the resulting total convex hull polygon. Evaluating search
 * positions is the most time-critical step of the nesting process.
 */
public class ConvexHullCriterion extends NestingCriterion {

    /**
     * Constructs a blank convex hull criterion.
     */
    public ConvexHullCriterion() {
    };

    private TreeSet<Point> convexHull_total = new TreeSet<>();
    private List<Result> allResults;

    /**
     * Updates the criterion by adding a polygon at a certain position. Performs the following
     * operations:
     * <ul>
     * <li>adds the vertices of the new polygon to the tree set of convex hull points - O(m log n)
     * time</li>
     * <li>recalculates the total convex hull - O(n)</li>
     * <li>reverts the total convex hull into a tree set - O(n log n)</li>
     * </ul>
     * 
     * @param position position of a newly placed polygon
     * @param polygon  the polygon
     */
    @Override
    public void addPolygon(IntegerVector position, Polygon polygon) {

        // Add convex hull points at position: O(m log n)
        convexHull_total
                .addAll(polygon.vertices.stream()
                        .map(vertex -> new Point(position.x + vertex.x,
                                position.y + vertex.y))
                        .collect(Collectors.toList()));

        // Recalculate total convex hull: O(n)
        List<Point> recalculated = ConvexHull
                .ofPresorted(new ArrayList<>(convexHull_total));

        // Convert to tree set: O(n log n)
        convexHull_total = new TreeSet<>(recalculated);

    }

    /**
     * Evaluates a collection of search positions at which a certain polygon is to be placed based on
     * the area of the resulting convex hull polygon. Returns the minimal result. For each position, the
     * following operations are performed:
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

            // Clone the total convex hull: O(n) according to TreeMap constructor comment
            @SuppressWarnings("unchecked")
            TreeSet<Point> clone = (TreeSet<Point>) convexHull_total.clone();

            // Add convex hull at search position: O(m log n)
            clone.addAll(polygon.vertices.stream()
                    .map(vertex -> vertex.translate(position))
                    .collect(Collectors.toList()));

            // Recalculate convex hull: O(n)
            CircularList<Point> convexHullPoints = ConvexHull.of(clone);

            // Evaluate: O(n)
            double evaluation = (new Polygon(convexHullPoints)).calculateArea();

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
    public Collection<Point> getConvexHull_total() {
        return convexHull_total;
    }
}
