package nesting.tuple;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nesting.geometry.*;
import nesting.geometry.elements.*;
import nesting.geometry.elements.Vector;
import nesting.irregular.*;
import nesting.irregular.NestingCriterion.Result;
import nesting.svg.*;
import nesting.svg.SVG.Style;

/**
 * Implementation of hybrid nesting strategy. Polygons are first nested into pairs and quadruples.
 * Quadruples are nested into an larger arrangement of polygons on which a two-dimensional search
 * using the table area is performed. Different nesting criteria can be used for pairs, quadruples
 * and the larger arrangement.
 */
public class TupleNesting implements Drawable {

    public static int RASTERIZATION = 10;
    private static boolean useSmallestEnclosingCircleCriterionForPairs = true;
    private static boolean useSmallestEnclosingCircleCriterionForQuadruples = true;
    private static boolean useSmallestEnclosingCircleCriterionForNesting = false;
    private static boolean showOffsetCurves = true;
    private static boolean showPolygons = true;

    private NoFitSpace noFitSpace;
    private Polygon offsetCurve;
    private Polygon convexHullPolygon;

    private EnumMap<PositionType, IntegerVector> positions = new EnumMap<>(
            PositionType.class);
    private EnumMap<PolygonSetType, PolygonSet> polygonSets = new EnumMap<>(
            PolygonSetType.class);
    private EnumMap<SpaceType, Space> spaces = new EnumMap<>(SpaceType.class);

    private List<Polygon> bestResult;

    /**
     * Constructs and runs an instance of the tuple nesting algorithm for the given polygon.
     * 
     * @param polygon the polygon to be nested
     */
    public TupleNesting(Polygon polygon) {
        this.offsetCurve = new OffsetCurvePolygon(OffsetCurve.of(polygon),
                polygon);
        convexHullPolygon = ConvexHull.of(offsetCurve);

        constructSingles();

        construct1To1Spaces();

        calculatePairs();

        construct2To1Spaces();
        convertTo1To2Spaces();
        construct2To2Spaces();

        calculateQuadruples();

        construct4To2Spaces();
        convertTo2To4Spaces();
        construct4To4Spaces();

        nest();
        search();
    }

    private void search() {
        for (NestingType nestingType : NestingType.values()) {

            final Circle searchArea = new Circle(Point.ORIGIN,
                    nestingType.sec.radius / 2);

            // Generate search space
            int n = (int) (searchArea.radius / RASTERIZATION) + 1;
            List<IntegerVector> points = IntStream.rangeClosed(-n, n)
                    .mapToObj(i_x -> IntStream.rangeClosed(-n, n)
                            .mapToObj(i_y -> new IntegerVector(
                                    i_x * RASTERIZATION, i_y * RASTERIZATION)))
                    .flatMap(Function.identity()).collect(Collectors.toList());
            List<IntegerVector> filtered = points.stream()
                    .filter(point -> searchArea.contains(point.toPoint()))
                    .collect(Collectors.toList());
            List<SearchResult> results = new ArrayList<>();
            for (IntegerVector center : filtered) {
                Circle table = new Circle(center.toPoint(),
                        NestingParameters.tableRadius);
                results.add(new SearchResult(center,
                        nestingType.nesting.stream()
                                .filter(p -> table.contains(p))
                                .collect(Collectors.toList())));
            }
            int max_n = results.stream().mapToInt(r -> r.polygons.size()).max()
                    .getAsInt();
            List<SearchResult> maximalResults = results.stream()
                    .filter(r -> r.polygons.size() == max_n)
                    .collect(Collectors.toList());
            SearchResult mostCentered = maximalResults.stream()
                    .min((a, b) -> Double.compare(maximalDistanceFromCenter(a),
                            maximalDistanceFromCenter(b)))
                    .get();
            nestingType.result = mostCentered;
        }

        int max_n_overall = Arrays.stream(NestingType.values())
                .mapToInt(type -> type.result.polygons.size()).max().getAsInt();
        List<SearchResult> maximalResults_overall = Arrays
                .stream(NestingType.values()).map(type -> type.result)
                .filter(result -> result.polygons.size() == max_n_overall)
                .collect(Collectors.toList());
        SearchResult mostCentered_overall = maximalResults_overall.stream()
                .min((a, b) -> Double.compare(maximalDistanceFromCenter(a),
                        maximalDistanceFromCenter(b)))
                .get();

        // Transform minimum bounding box corners into polygon reference points
        Vector toOrigin = new Vector(
                mostCentered_overall.searchPosition.toPoint(), Point.ORIGIN);
        bestResult = mostCentered_overall.polygons.stream()
                .map(p -> p.translate(toOrigin)).collect(Collectors.toList());
    }

    private double maximalDistanceFromCenter(SearchResult result) {
        return result.polygons.stream()
                .mapToDouble(p -> Polygon.maximumDistanceOfVertexToPoint
                        .apply(p, result.searchPosition.toPoint()))
                .max().getAsDouble();
    }

    private void nest() {

        for (NestingType nestingType : NestingType.values()) {

            // Initialize
            PolygonSet polygonSet = polygonSets.get(nestingType.polygonSetType);
            Space space = spaces.get(nestingType.spaceType);
            SearchSpace searchSpace = new SearchSpace();
            NestingCriterion criterion = useSmallestEnclosingCircleCriterionForNesting
                    ? new SmallestEnclosingCircleCriterion()
                    : new ConvexHullCriterion();
            Polygon convexHullPolygonSet = ConvexHull.of(polygonSet);

            // Define filter
            Circle sec = (new RandomizedIncrementalConstruction(
                    convexHullPolygonSet)).getSmallestEnclosingCircle();
            nestingType.sec = sec;
            final IntegerVector initialPlacement = new IntegerVector(
                    -(int) sec.center.x, -(int) sec.center.y);
            final double nestingRadius = NestingParameters.tableRadius
                    + 1.0 * sec.radius;
            Predicate<IntegerVector> nestingFilter = point -> Math.hypot(
                    point.x - initialPlacement.x,
                    point.y - initialPlacement.y) < nestingRadius;

            // Initial placement
            List<IntegerVector> placements = new ArrayList<>();
            placements.add(initialPlacement);
            searchSpace.addPolygon(initialPlacement, space.fitPoints,
                    space.noFitPoints, nestingFilter);
            criterion.addPolygon(initialPlacement, convexHullPolygonSet);

            Result result;
            // Additional placements
            do {

                // Evaluate search space
                result = criterion.evaluate(searchSpace.getFitPointsTotal(),
                        convexHullPolygonSet);

                if (result != null) {

                    // Add result
                    placements.add(result.position);
                    searchSpace.addPolygon(result.position, space.fitPoints,
                            space.noFitPoints, nestingFilter);
                    criterion.addPolygon(result.position, convexHullPolygonSet);
                }
            } while (result != null);

            nestingType.nesting = placements.stream()
                    .flatMap(placement -> polygonSet.translate(placement)
                            .getPolygons().stream())
                    .collect(Collectors.toList());
        }
    }

    private void construct4To4Spaces() {

        // Construct space NNNN-NNNN
        spaces.put(SpaceType.SPACE_NNNN_NNNN,
                new Space(spaces.get(SpaceType.SPACE_NN_NNNN),
                        spaces.get(SpaceType.SPACE_NN_NNNN),
                        positions.get(PositionType.POSITION_NN_NN)));

        // Construct space NRNR-NRNR
        spaces.put(SpaceType.SPACE_NRNR_NRNR,
                new Space(spaces.get(SpaceType.SPACE_NR_NRNR),
                        spaces.get(SpaceType.SPACE_NR_NRNR),
                        positions.get(PositionType.POSITION_NR_NR)));

        // Construct space NNRR-NNRR
        spaces.put(SpaceType.SPACE_NNRR_NNRR,
                new Space(spaces.get(SpaceType.SPACE_NN_NNRR),
                        spaces.get(SpaceType.SPACE_RR_NNRR),
                        positions.get(PositionType.POSITION_NN_RR)));
    }

    private void convertTo2To4Spaces() {

        // Convert spaces NNNN-NN to NN-NNNN etc.
        spaces.put(SpaceType.SPACE_NN_NNNN,
                spaces.get(SpaceType.SPACE_NNNN_NN).reflect());
        spaces.put(SpaceType.SPACE_NR_NRNR,
                spaces.get(SpaceType.SPACE_NRNR_NR).reflect());
        spaces.put(SpaceType.SPACE_NN_NNRR,
                spaces.get(SpaceType.SPACE_NNRR_NN).reflect());
        spaces.put(SpaceType.SPACE_RR_NNRR,
                spaces.get(SpaceType.SPACE_NNRR_RR).reflect());

    }

    private void construct4To2Spaces() {

        // Construct NNNN-NN
        spaces.put(SpaceType.SPACE_NNNN_NN,
                new Space(spaces.get(SpaceType.SPACE_NN_NN),
                        spaces.get(SpaceType.SPACE_NN_NN),
                        positions.get(PositionType.POSITION_NN_NN)));

        // Construct NRNR-NR
        spaces.put(SpaceType.SPACE_NRNR_NR,
                new Space(spaces.get(SpaceType.SPACE_NR_NR),
                        spaces.get(SpaceType.SPACE_NR_NR),
                        positions.get(PositionType.POSITION_NR_NR)));

        // Construct NNRR-NN
        spaces.put(SpaceType.SPACE_NNRR_NN,
                new Space(spaces.get(SpaceType.SPACE_NN_NN),
                        spaces.get(SpaceType.SPACE_RR_NN),
                        positions.get(PositionType.POSITION_NN_RR)));

        // Construct NNRR-RR
        spaces.put(SpaceType.SPACE_NNRR_RR,
                new Space(spaces.get(SpaceType.SPACE_NN_RR),
                        spaces.get(SpaceType.SPACE_RR_RR),
                        positions.get(PositionType.POSITION_NN_RR)));
    }

    private void calculateQuadruples() {

        // Calculate quadruple NN-NN
        Polygon convexHullPolygon_NN = ConvexHull
                .of(polygonSets.get(PolygonSetType.PAIR_N_N));
        NestingCriterion criterion_NN_NN = useSmallestEnclosingCircleCriterionForQuadruples
                ? new SmallestEnclosingCircleCriterion()
                : new ConvexHullCriterion();
        criterion_NN_NN.addPolygon(IntegerVector.ORIGIN, convexHullPolygon_NN);
        positions.put(PositionType.POSITION_NN_NN,
                criterion_NN_NN.getBestPosition(
                        spaces.get(SpaceType.SPACE_NN_NN),
                        convexHullPolygon_NN));
        polygonSets.put(PolygonSetType.QUADRUPLE_NN_NN,
                new PolygonSet(polygonSets.get(PolygonSetType.PAIR_N_N),
                        polygonSets.get(PolygonSetType.PAIR_N_N).translate(
                                positions.get(PositionType.POSITION_NN_NN))));

        // Calculate quadruple NR-NR
        Polygon convexHullPolygon_NR = ConvexHull
                .of(polygonSets.get(PolygonSetType.PAIR_N_R));
        NestingCriterion criterion_NR_NR = useSmallestEnclosingCircleCriterionForQuadruples
                ? new SmallestEnclosingCircleCriterion()
                : new ConvexHullCriterion();
        criterion_NR_NR.addPolygon(IntegerVector.ORIGIN, convexHullPolygon_NR);
        positions.put(PositionType.POSITION_NR_NR,
                criterion_NR_NR.getBestPosition(
                        spaces.get(SpaceType.SPACE_NR_NR),
                        convexHullPolygon_NR));
        polygonSets.put(PolygonSetType.QUADRUPLE_NR_NR,
                new PolygonSet(polygonSets.get(PolygonSetType.PAIR_N_R),
                        polygonSets.get(PolygonSetType.PAIR_N_R).translate(
                                positions.get(PositionType.POSITION_NR_NR))));

        // Calculate quadruple NN-RR
        Polygon convexHullPolygon_RR = ConvexHull
                .of(polygonSets.get(PolygonSetType.PAIR_R_R));
        NestingCriterion criterion_NN_RR = useSmallestEnclosingCircleCriterionForQuadruples
                ? new SmallestEnclosingCircleCriterion()
                : new ConvexHullCriterion();
        criterion_NN_RR.addPolygon(IntegerVector.ORIGIN, convexHullPolygon_NN);
        positions.put(PositionType.POSITION_NN_RR,
                criterion_NN_RR.getBestPosition(
                        spaces.get(SpaceType.SPACE_NN_RR),
                        convexHullPolygon_RR));
        polygonSets.put(PolygonSetType.QUADRUPLE_NN_RR,
                new PolygonSet(polygonSets.get(PolygonSetType.PAIR_N_N),
                        polygonSets.get(PolygonSetType.PAIR_R_R).translate(
                                positions.get(PositionType.POSITION_NN_RR))));
    }

    private void construct2To2Spaces() {

        // Construct spaces NN-NN, NR-NR and NN-RR
        spaces.put(SpaceType.SPACE_NN_NN,
                new Space(spaces.get(SpaceType.SPACE_N_NN),
                        spaces.get(SpaceType.SPACE_N_NN),
                        positions.get(PositionType.POSITION_N_N)));
        spaces.put(SpaceType.SPACE_NR_NR,
                new Space(spaces.get(SpaceType.SPACE_N_NR),
                        spaces.get(SpaceType.SPACE_R_NR),
                        positions.get(PositionType.POSITION_N_R)));
        spaces.put(SpaceType.SPACE_NN_RR,
                new Space(spaces.get(SpaceType.SPACE_N_RR),
                        spaces.get(SpaceType.SPACE_N_RR),
                        positions.get(PositionType.POSITION_N_N)));

        // Assign spaces RR-RR and RR-NN
        spaces.put(SpaceType.SPACE_RR_RR,
                spaces.get(SpaceType.SPACE_NN_NN).reflect());
        spaces.put(SpaceType.SPACE_RR_NN,
                spaces.get(SpaceType.SPACE_NN_RR).reflect());
    }

    private void convertTo1To2Spaces() {

        // Convert spaces NN-N to N-NN, NN-R to R-NN, NR-N to N-NR and NR-R to R-NR
        spaces.put(SpaceType.SPACE_N_NN,
                spaces.get(SpaceType.SPACE_NN_N).reflect());
        spaces.put(SpaceType.SPACE_R_NN,
                spaces.get(SpaceType.SPACE_NN_R).reflect());
        spaces.put(SpaceType.SPACE_N_NR,
                spaces.get(SpaceType.SPACE_NR_N).reflect());
        spaces.put(SpaceType.SPACE_R_NR,
                spaces.get(SpaceType.SPACE_NR_R).reflect());

        // Assign spaces RR_R, RR_N, RN_R and RN_N
        spaces.put(SpaceType.SPACE_RR_R, spaces.get(SpaceType.SPACE_N_NN));
        spaces.put(SpaceType.SPACE_RR_N, spaces.get(SpaceType.SPACE_R_NN));
    }

    private void construct2To1Spaces() {

        // Construct spaces NN-N, NN-R, NR-N and NR-R
        spaces.put(SpaceType.SPACE_NN_N,
                new Space(spaces.get(SpaceType.SPACE_N_N),
                        spaces.get(SpaceType.SPACE_N_N),
                        positions.get(PositionType.POSITION_N_N)));
        spaces.put(SpaceType.SPACE_NN_R,
                new Space(spaces.get(SpaceType.SPACE_N_R),
                        spaces.get(SpaceType.SPACE_N_R),
                        positions.get(PositionType.POSITION_N_N)));
        spaces.put(SpaceType.SPACE_NR_N,
                new Space(spaces.get(SpaceType.SPACE_N_N),
                        spaces.get(SpaceType.SPACE_R_N),
                        positions.get(PositionType.POSITION_N_R)));
        spaces.put(SpaceType.SPACE_NR_R,
                new Space(spaces.get(SpaceType.SPACE_N_R),
                        spaces.get(SpaceType.SPACE_R_R),
                        positions.get(PositionType.POSITION_N_R)));

        // Assign spaces R_RR and N_RR
        spaces.put(SpaceType.SPACE_R_RR, spaces.get(SpaceType.SPACE_NN_N));
        spaces.put(SpaceType.SPACE_N_RR, spaces.get(SpaceType.SPACE_NN_R));

    }

    private void calculatePairs() {

        NestingCriterion criterion = useSmallestEnclosingCircleCriterionForPairs
                ? new SmallestEnclosingCircleCriterion()
                : new ConvexHullCriterion();
        criterion.addPolygon(IntegerVector.ORIGIN, offsetCurve);

        // Calculate N-N and N-R
        positions.put(PositionType.POSITION_N_N, criterion.getBestPosition(
                spaces.get(SpaceType.SPACE_N_N), convexHullPolygon));
        positions.put(PositionType.POSITION_N_R, criterion.getBestPosition(
                spaces.get(SpaceType.SPACE_N_R), convexHullPolygon));

        // Derive R-R
        positions.put(PositionType.POSITION_R_R,
                positions.get(PositionType.POSITION_N_N).reflect());

        // Construct pairs
        polygonSets.put(PolygonSetType.PAIR_N_N,
                new PolygonSet(offsetCurve, offsetCurve
                        .translate(positions.get(PositionType.POSITION_N_N))));
        polygonSets.put(PolygonSetType.PAIR_N_R,
                new PolygonSet(offsetCurve, offsetCurve.rotate()
                        .translate(positions.get(PositionType.POSITION_N_R))));
        polygonSets.put(PolygonSetType.PAIR_R_R,
                new PolygonSet(offsetCurve.rotate(), offsetCurve.rotate()
                        .translate(positions.get(PositionType.POSITION_R_R))));
    }

    private void construct1To1Spaces() {
        noFitSpace = new NoFitSpace(offsetCurve);
        spaces.put(SpaceType.SPACE_N_N,
                new Space(
                        noFitSpace.getFitPoints()
                                .get(NoFitSpace.CASE_NORMAL_NORMAL),
                        noFitSpace.getNoFitPoints()
                                .get(NoFitSpace.CASE_NORMAL_NORMAL)));
        spaces.put(SpaceType.SPACE_R_R,
                new Space(
                        noFitSpace.getFitPoints()
                                .get(NoFitSpace.CASE_ROTATED_ROTATED),
                        noFitSpace.getNoFitPoints()
                                .get(NoFitSpace.CASE_ROTATED_ROTATED)));
        spaces.put(SpaceType.SPACE_N_R,
                new Space(
                        noFitSpace.getFitPoints()
                                .get(NoFitSpace.CASE_NORMAL_ROTATED),
                        noFitSpace.getNoFitPoints()
                                .get(NoFitSpace.CASE_NORMAL_ROTATED)));
        spaces.put(SpaceType.SPACE_R_N,
                new Space(
                        noFitSpace.getFitPoints()
                                .get(NoFitSpace.CASE_ROTATED_NORMAL),
                        noFitSpace.getNoFitPoints()
                                .get(NoFitSpace.CASE_ROTATED_NORMAL)));
    }

    private void constructSingles() {
        polygonSets.put(PolygonSetType.SINGLE_N, new PolygonSet(offsetCurve));
        polygonSets.put(PolygonSetType.SINGLE_R,
                new PolygonSet(offsetCurve.rotate()));
    }

    /**
     * Returns the no-fit space for the given polygon.
     * 
     * @return the no-fit space for the given polygon
     */
    public NoFitSpace getNoFitSpace() {
        return noFitSpace;
    }

    /**
     * Returns an <code>EnumMap</code> of the polygon sets in this nesting, indexed by polygon set type.
     * 
     * @return an <code>EnumMap</code> of the polygon sets in this nesting
     */
    public EnumMap<PolygonSetType, PolygonSet> getPolygonSets() {
        return polygonSets;
    }

    /**
     * Returns an <code>EnumMap</code> of the search spaces in this nesting, indexed by search space
     * type.
     * 
     * @return an <code>EnumMap</code> of the search spaces in this nesting
     */
    public EnumMap<SpaceType, Space> getSpaces() {
        return spaces;
    }

    /**
     * Returns the number of polygons in the best nesting result.
     * 
     * @return the number of polygons in the best nesting result
     */
    public int getN() {
        return bestResult.size();
    }

    /**
     * If set to <code>true</code> an instance of <code>SmallestEnclosingCircleCriterion</code> is used
     * for nesting pairs; otherwise an instance of <code>ConvexHullCriterion</code> is used. The default
     * value is <code>false</code>, i.e. the convex hull criterion is used.
     * 
     * @param useSmallestEnclosingCircleCriterionForPairs if <code>true</code> an instance of
     *                                                    <code>SmallestEnclosingCircleCriterion</code>
     *                                                    is used for nesting pairs; otherwise an
     *                                                    instance of <code>ConvexHullCriterion</code>
     *                                                    is used
     */
    public static void setUseSmallestEnclosingCircleCriterionForPairs(
            boolean useSmallestEnclosingCircleCriterionForPairs) {
        TupleNesting.useSmallestEnclosingCircleCriterionForPairs = useSmallestEnclosingCircleCriterionForPairs;
    }

    /**
     * If set to <code>true</code> an instance of <code>SmallestEnclosingCircleCriterion</code> is used
     * for nesting quadruples; otherwise an instance of <code>ConvexHullCriterion</code> is used. The
     * default value is <code>false</code>, i.e. the convex hull criterion is used.
     * 
     * @param useSmallestEnclosingCircleCriterionForQuadruples if <code>true</code> an instance of
     *                                                         <code>SmallestEnclosingCircleCriterion</code>
     *                                                         is used for nesting quadruples; otherwise
     *                                                         an instance of
     *                                                         <code>ConvexHullCriterion</code> is used
     */
    public static void setUseSmallestEnclosingCircleCriterionForQuadruples(
            boolean useSmallestEnclosingCircleCriterionForQuadruples) {
        TupleNesting.useSmallestEnclosingCircleCriterionForQuadruples = useSmallestEnclosingCircleCriterionForQuadruples;
    }

    /**
     * If set to <code>true</code> an instance of <code>SmallestEnclosingCircleCriterion</code> is used
     * for generating the larger arrangements of quadruples; otherwise an instance of
     * <code>ConvexHullCriterion</code> is used. The default value is <code>false</code>, i.e. the
     * convex hull criterion is used.
     * 
     * @param useSmallestEnclosingCircleCriterionForNesting if <code>true</code> an instance of
     *                                                      <code>SmallestEnclosingCircleCriterion</code>
     *                                                      is used for generating the larger
     *                                                      arrangements of quadruples; otherwise an
     *                                                      instance of <code>ConvexHullCriterion</code>
     *                                                      is used
     */
    public static void setUseSmallestEnclosingCircleCriterionForNesting(
            boolean useSmallestEnclosingCircleCriterionForNesting) {
        TupleNesting.useSmallestEnclosingCircleCriterionForNesting = useSmallestEnclosingCircleCriterionForNesting;
    }

    /**
     * If <code>true</code>, polygons are drawn in the <code>toSVGElement</code> method.
     * 
     * @param showPolygons if <code>true</code>, polygons are drawn
     */
    public static void setShowPolygons(boolean showPolygons) {
        TupleNesting.showPolygons = showPolygons;
    }

    /**
     * If <code>true</code>, offset curves are drawn in the <code>toSVGElement</code> method.
     * 
     * @param setShowOffsetCurves if <code>true</code>, offset curves are drawn
     */
    public static void setShowOffsetCurves(boolean showOffsetCurves) {
        TupleNesting.showOffsetCurves = showOffsetCurves;
    }

    /**
     * Implements a search space for nesting polygons or polygon sets.
     */
    public static class Space {

        private TreeSet<IntegerVector> fitPoints = new TreeSet<>();
        private TreeSet<IntegerVector> noFitPoints = new TreeSet<>();

        /**
         * Constructs an empty search space.
         */
        public Space() {
        }

        /**
         * Constructs a new search space from the given space.
         * 
         * @param space a given search space
         */
        @SuppressWarnings("unchecked")
        public Space(Space space) {
            fitPoints = (TreeSet<IntegerVector>) space.fitPoints.clone();
            noFitPoints = (TreeSet<IntegerVector>) space.noFitPoints.clone();
        }

        /**
         * Constructs a new search space from the given fit points and no-fit points.
         * 
         * @param fitPoints   a list of fit points
         * @param noFitPoints a list of no-fit points
         */
        public Space(List<IntegerVector> fitPoints,
                List<IntegerVector> noFitPoints) {
            this.fitPoints = new TreeSet<>(fitPoints);
            this.noFitPoints = new TreeSet<>(noFitPoints);
        }

        /**
         * Constructs a new space from the first space (which is assumed to be positioned at the origin) and
         * the second space, which is translated to the given integer point position.
         * 
         * @param space_1  first space (at the origin)
         * @param space_2  second space (translated to position)
         * @param position position of the second space
         */
        public Space(Space space_1, Space space_2, IntegerVector position) {
            this(space_1);
            addSubtract(space_2, position);
        }

        /**
         * Modifies the search space so that it reflects the addition of another search space at a given
         * position. Performs the following operations:
         * <ul>
         * <li>translates fit points in the added search space to the given position</li>
         * <li>excludes all fit points that are in the set of no-fit points of the search space</li>
         * <li>adds the rest to the fit point total</li>
         * <li>translates no-fit points in the added search space to the given position</li>
         * <li>removes these points from the search space</li>
         * <li>adds them to the no-fit point total</li>
         * </ul>
         * 
         * @param space    a given search space
         * @param position a given position
         */
        public void addSubtract(Space space, IntegerVector position) {
            fitPoints.addAll(space.fitPoints.stream()
                    .map(fitPoint -> fitPoint.plus(position))
                    .filter(fitPoint -> !noFitPoints.contains(fitPoint))
                    .collect(Collectors.toList()));
            List<IntegerVector> noFitPoints_translated = space.noFitPoints
                    .stream().map(noFitPoint -> noFitPoint.plus(position))
                    .collect(Collectors.toList());
            noFitPoints_translated.stream()
                    .forEach(noFitPoint -> fitPoints.remove(noFitPoint));
            noFitPoints.addAll(noFitPoints_translated);
        }

        /**
         * Returns the search space reflected around the point of origin.
         * 
         * @return the search space reflected around the point of origin
         */
        public Space reflect() {
            return new Space(
                    fitPoints.stream()
                            .map(integerPoint -> integerPoint.reflect())
                            .collect(Collectors.toList()),
                    noFitPoints.stream()
                            .map(integerPoint -> integerPoint.reflect())
                            .collect(Collectors.toList()));
        }

        /**
         * Returns the fit points in the search space.
         * 
         * @return the fit points in the search space
         */
        public TreeSet<IntegerVector> getFitPoints() {
            return fitPoints;
        }

        /**
         * Returns the no-fit points in the search space.
         * 
         * @return the no-fit points in the search space
         */
        public TreeSet<IntegerVector> getNoFitPoints() {
            return noFitPoints;
        }
    }

    /**
     * The types of search spaces used in tuple nesting.
     */
    public enum SpaceType {

        SPACE_N_N, SPACE_R_R, SPACE_N_R, SPACE_R_N, SPACE_NN_N, SPACE_NN_R,
        SPACE_NR_N, SPACE_NR_R, SPACE_N_NN, SPACE_R_NN, SPACE_N_NR, SPACE_R_NR,
        SPACE_RR_R, SPACE_RR_N, SPACE_R_RR, SPACE_N_RR, SPACE_NN_NN,
        SPACE_NR_NR, SPACE_NN_RR, SPACE_RR_RR, SPACE_RR_NN, SPACE_NNNN_NN,
        SPACE_NRNR_NR, SPACE_NNRR_NN, SPACE_NNRR_RR, SPACE_NN_NNNN,
        SPACE_NR_NRNR, SPACE_NN_NNRR, SPACE_RR_NNRR, SPACE_NNNN_NNNN,
        SPACE_NRNR_NRNR, SPACE_NNRR_NNRR;

    }

    /**
     * The types of relative positions of oriented polygons and polygon pairs used in tuple nesting.
     */
    public enum PositionType {

        POSITION_N_N, POSITION_N_R, POSITION_R_R, POSITION_R_N,

        POSITION_NN_NN, POSITION_NR_NR, POSITION_NN_RR,

    }

    /**
     * The types of polygon sets (singles, pairs, quadruples) used in tuple nesting.
     */
    public enum PolygonSetType {

        SINGLE_N, SINGLE_R,

        PAIR_N_N, PAIR_N_R, PAIR_R_R,

        QUADRUPLE_NN_NN, QUADRUPLE_NR_NR, QUADRUPLE_NN_RR;

    }

    /**
     * The different types of larger arrangements used in tuple nesting.
     */
    public enum NestingType {

        NESTING_NNNN(PolygonSetType.QUADRUPLE_NN_NN, SpaceType.SPACE_NNNN_NNNN),
        NESTING_NRNR(PolygonSetType.QUADRUPLE_NR_NR, SpaceType.SPACE_NRNR_NRNR),
        NESTING_NNRR(PolygonSetType.QUADRUPLE_NN_RR, SpaceType.SPACE_NNRR_NNRR);

        /**
         * The type of polygon set used in the arrangement.
         */
        public PolygonSetType polygonSetType;

        /**
         * The type of seach space used in the arrangement.
         */
        public SpaceType spaceType;

        /**
         * The nesting result for the given type of arrangement.
         */
        public List<Polygon> nesting;

        /**
         * The search result for the given type of arrangement.
         */
        public SearchResult result;

        /**
         * The smallest enclosing circle of the polygon set used in the arrangement. This is used to center
         * the search area and determine its dimensions.
         */
        public Circle sec;

        /**
         * Constructs a type of arrangement used in tuple nesting.
         * 
         * @param polygonSetType the polygon set type used for this arrangement
         * @param spaceType      the search space type used for this arrangement
         */
        NestingType(PolygonSetType polygonSetType, SpaceType spaceType) {
            this.polygonSetType = polygonSetType;
            this.spaceType = spaceType;
            nesting = new ArrayList<>();
        }
    }

    /**
     * A search result of the tuple nesting algorithm.
     */
    public static class SearchResult {

        /**
         * The center of the search area that yielded the result.
         */
        public final IntegerVector searchPosition;

        /**
         * The list of polygons in this search result.
         */
        public final List<Polygon> polygons;

        /**
         * Constructs a new search result from the given search position and list of polygons.
         * 
         * @param searchPosition a search position
         * @param polygons       a list of polygons
         */
        public SearchResult(IntegerVector searchPosition,
                List<Polygon> polygons) {
            this.searchPosition = searchPosition;
            this.polygons = polygons;
        }

    }

    /**
     * The polygon representing an offset curve of a polygon.
     */
    public static class OffsetCurvePolygon extends Polygon {

        /**
         * The polygon whose offset curve is represented.
         */
        public final Polygon polygon;

        /**
         * Constructs a new offset polygon from an offset curve and a polygon.
         * 
         * @param offsetCurve the offset curve of the given polygon
         * @param polygon     a given polygon
         */
        public OffsetCurvePolygon(Polygon offsetCurve, Polygon polygon) {
            super(offsetCurve.vertices);
            this.polygon = polygon;
        }

        /**
         * Returns an offset curve translated according to the given integer vector.
         */
        @Override
        public Polygon translate(IntegerVector vector) {
            return new OffsetCurvePolygon(super.translate(vector),
                    polygon.translate(vector));
        }

        /**
         * Returns an offset curve polygon translated according to the given vector.
         */
        @Override
        public Polygon translate(Vector vector) {
            return new OffsetCurvePolygon(super.translate(vector),
                    polygon.translate(vector));
        }

        /**
         * Returns an offset curve polygon rotated around the center of its bounding box.
         */
        @Override
        public Polygon rotate() {
            return new OffsetCurvePolygon(super.rotate(),
                    polygon.rotate(getBoundingBox().getCenter()));
        }

    }

    /**
     * Returns an svg element representing the best tuple nesting result graphically.
     */
    @Override
    public SVGElement toSVGElement() {

        SVGGroup group = new SVGGroup();
        SVG.setStyle(Style.SOLID);
        group.add(Circle.TABLE.toSVGElement());
        if (showPolygons) {
            bestResult.stream().forEach(oc -> group
                    .add(((OffsetCurvePolygon) oc).polygon.toSVGElement()));
        }
        if (showOffsetCurves) {
            SVG.setStyle(showPolygons ? Style.DASHED : Style.SOLID);
            bestResult.stream().forEach(oc -> group.add(oc.toSVGElement()));
        }
        return group.toSVGElement();
    }
}
