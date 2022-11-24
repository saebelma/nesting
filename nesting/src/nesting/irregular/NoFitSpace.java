package nesting.irregular;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nesting.geometry.*;
import nesting.geometry.elements.*;

/**
 * Calculates and stores the fit / no-fit spaces for a polygon. Each space is a list of points at
 * which polygons can / cannot be placed without intersecting the given polygon. There are four
 * spaces of each kind, representing the four possibilities of placing a normal / rotated polygon
 * next to a normal / rotated polygons. Only two of these have to calculated from scratch, the other
 * two are gotten by point reflection.
 * 
 * The algorithm first generates a rasterized search space based on the bounding box of the polygon.
 * It then uses the no-fit polygon for the convex hull of the polygon in order to create a better
 * upper bound for the search space (and the resulting fit space). For the remaining points,
 * brute-force intersection checks are performed to separate them into fit and no-fit points.
 */
public class NoFitSpace {

    /**
     * The rasterization of the fit / no-fit spaces. Default value is 10 mm.
     */
    public static final int RASTERIZATION = 10;

    /**
     * Index of search space for placing a normal polygon next to a normal polygon in the list of
     * spaces.
     */
    public static final int CASE_NORMAL_NORMAL = 0;

    /**
     * Index of search space for placing a rotated polygon next to a rotated polygon in the list of
     * spaces.
     */
    public static final int CASE_ROTATED_ROTATED = 1;

    /**
     * Index of search space for placing a rotated polygon next to a normal polygon in the list of
     * spaces.
     */
    public static final int CASE_NORMAL_ROTATED = 2;

    /**
     * Index of search space for placing a normal polygon next to a rotated polygon in the list of
     * spaces.
     */
    public static final int CASE_ROTATED_NORMAL = 3;

    private List<List<IntegerVector>> fitPoints = new ArrayList<>();
    private List<List<IntegerVector>> noFitPoints = new ArrayList<>();

    /**
     * Constructs the fit / no-fit spaces for this polygon.
     * 
     * @param polygon a polygon
     */
    public NoFitSpace(Polygon polygon) {

        Polygon convexHull = ConvexHull.of(polygon);
        Polygon polygon_rotated = polygon.rotate();
        Polygon convexHull_rotated = ConvexHull.of(polygon_rotated);

        for (int i = 0; i < 2; i++) {

            // Get no-fit polygon
            NoFitPolygon noFitPolygonAlgorithm = new NoFitPolygon(convexHull,
                    (i == 0) ? convexHull : convexHull_rotated);
            Polygon noFitPolygon = noFitPolygonAlgorithm.getNoFitPolygon();

            // Generate search space around fixed polygon
            int n_x = (int) (polygon.getBoundingBox().width / RASTERIZATION)
                    + 2;
            int n_y = (int) (polygon.getBoundingBox().height / RASTERIZATION)
                    + 2;
            List<IntegerVector> points = IntStream.rangeClosed(-n_x, n_x)
                    .mapToObj(i_x -> IntStream.rangeClosed(-n_y, n_y)
                            .mapToObj(i_y -> new IntegerVector(
                                    i_x * RASTERIZATION, i_y * RASTERIZATION)))
                    .flatMap(Function.identity()).collect(Collectors.toList());

            // Get extended no-fit polygon
            Polygon noFitPolygon_extended = PolygonizedParallelCurve
                    .of(noFitPolygon, Math.sqrt(2) * RASTERIZATION);

            // Filter search space by extended no-fit polygon
            Predicate<IntegerVector> nfp_filter = vector -> noFitPolygon_extended
                    .contains(noFitPolygonAlgorithm.getFixedPolygon()
                            .getReferencePoint().translate(vector)
                            .translate(new Vector(
                                    noFitPolygonAlgorithm.getOrbitingPolygon()
                                            .getReferencePoint(),
                                    noFitPolygonAlgorithm
                                            .getReferencePointOrbitingPolygon())));
            List<IntegerVector> filteredVectors = points.stream()
                    .filter(vector -> nfp_filter.test(vector))
                    .collect(Collectors.toList());

            Polygon polygon_fixed = polygon;
            Polygon polygon_orbiting = (i == 0) ? polygon : polygon_rotated;

            // Calculate fit and no-fit spaces (polygon translation probably why this is slow)
            List<IntegerVector> newNoFitPoints = new ArrayList<>();
            List<IntegerVector> newFitPoints = new ArrayList<>();
            for (IntegerVector vector : filteredVectors) {
                if (polygon_orbiting.translate(vector)
                        .intersects(polygon_fixed))
                    newNoFitPoints.add(vector);
                else
                    newFitPoints.add(vector);
            }
            noFitPoints.add(newNoFitPoints);
            fitPoints.add(newFitPoints);

            // Get fit and no-fit points of rotated-rotated case by point reflection
            noFitPoints.add(newNoFitPoints.stream()
                    .map(point -> new IntegerVector(-point.x, -point.y))
                    .collect(Collectors.toList()));
            fitPoints.add(newFitPoints.stream()
                    .map(point -> new IntegerVector(-point.x, -point.y))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Returns the list of fit spaces.
     * 
     * @return the list of fit spaces
     */
    public List<List<IntegerVector>> getFitPoints() {
        return fitPoints;
    }

    /**
     * Returns the list of no-fit spaces.
     * 
     * @return the list of no-fit spaces.
     */
    public List<List<IntegerVector>> getNoFitPoints() {
        return noFitPoints;
    }
}
