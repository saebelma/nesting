package nesting.packing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nesting.geometry.RandomizedIncrementalConstruction;
import nesting.geometry.elements.*;
import nesting.irregular.NestingParameters;
import nesting.svg.*;
import nesting.svg.SVG.Style;

/**
 * An algorithm for packing circles into a larger circle. The basic idea of the algorithm is to
 * generate packings of the plane and then do a search over these packings with the shape of the
 * larger circle. The two plane packings used are the optimal hexagonal packing and the regular
 * packing (circle centers on a square grid), which is better for some small numbers.
 */
public class CirclePacking implements Drawable {

    private final static double sqrt_3 = Math.sqrt(3);
    private final static double epsilon = 1.001;
    private static boolean showCircles = true;
    private static boolean showPolygons = true;

    private static double raster = 1.0; // mm

    Polygon polygon;
    Circle smallestEnclosingCircle;
    private double bigRadius;
    private double smallRadius;
    private List<Point> hexagonalPlanePacking;
    private List<Point> regularPlanePacking;
    private List<List<Point>> allPackings;
    private List<Point> circleCenters;
    private List<Point> polygonReferencePoints;

    /**
     * Constructs a new algorithm for packing a polygon. Computes the smallest enclosing circle of the
     * polygon using randomized incremental construction. Then calculates packing with parameters for
     * table radius and part clearance taken from the <code>Nesting</code> class.
     * 
     * @param polygon the polygon to be packed
     */
    public CirclePacking(Polygon polygon) {
        this.polygon = polygon;
        smallestEnclosingCircle = (new RandomizedIncrementalConstruction(
                polygon)).getSmallestEnclosingCircle();
        bigRadius = NestingParameters.tableRadius;
        smallRadius = smallestEnclosingCircle.radius + NestingParameters.partClearance;

        run();
    }

    /**
     * Constructs a new algorithm and generates packings of the smaller circle inside the larger circle.
     * 
     * @param bigRadius   the radius of the larger circle
     * @param smallRadius the radius of the smaller circle
     */
    public CirclePacking(double bigRadius, double smallRadius) {
        this.bigRadius = bigRadius;
        this.smallRadius = smallRadius;

        run();
    }

    private void run() {
        allPackings = new ArrayList<>();

        // Generate plane packings
        generatePlanePackings();

        // Filter packings
        filterPlanePackings();

        // Get maximal, most centered packing
        filterBestPacking();

        // If polygon was set, place polygons
        if (polygon != null) {
            Vector toReferencePoint = new Vector(smallestEnclosingCircle.center,
                    polygon.getReferencePoint());
            polygonReferencePoints = circleCenters.stream().map(
                    circleCenter -> circleCenter.translate(toReferencePoint))
                    .collect(Collectors.toList());
        }
    }

    private void filterPlanePackings() {
        filterX(hexagonalPlanePacking, -smallRadius, smallRadius);
        filterY(hexagonalPlanePacking, -sqrt_3 * smallRadius,
                sqrt_3 * smallRadius);
        filterX(regularPlanePacking, -smallRadius, smallRadius);
        filterY(regularPlanePacking, -smallRadius, smallRadius);
    }

    private void filterBestPacking() {
        int max_n = allPackings.stream().mapToInt(p -> p.size()).max()
                .getAsInt();
        circleCenters = allPackings.stream().filter(pk -> pk.size() == max_n)
                .min((a, b) -> Double.compare(getSkew(a), getSkew(b))).get();
    }

    private void filterX(List<Point> planePacking, double startX, double endX) {
        for (double x = startX; x <= endX; x += raster)
            filter(planePacking, new Point(x, 0));
    }

    private void filterY(List<Point> planePacking, double startY, double endY) {
        for (double y = startY; y <= endY; y += raster)
            filter(planePacking, new Point(0, y));
    }

    private void filter(List<Point> planePacking, Point point) {
        Circle c_1 = new Circle(point, bigRadius - smallRadius);
        List<Point> filtered = planePacking.stream()
                .filter(p -> c_1.contains(p)).collect(Collectors.toList());
        List<Point> translated = filtered.stream()
                .map(p -> p.translate(new Vector(point, Point.ORIGIN)))
                .collect(Collectors.toList());
        allPackings.add(translated);
    }

    private double getSkew(List<Point> packing) {
        return packing.stream().map(p -> Point.distance(Point.ORIGIN, p))
                .max((d_1, d_2) -> Double.compare(d_1, d_2)).get();
    }

    // The hexagonal packing is generated in two steps in order to account for the offset between rows.
    private void generatePlanePackings() {
        hexagonalPlanePacking = new ArrayList<>();
        regularPlanePacking = new ArrayList<>();

        // Add a-rows to hexagonal plane packing
        int n_y = (int) ((bigRadius + (sqrt_3 - 1.0) * smallRadius)
                / (2 * sqrt_3 * smallRadius)); // number of additional a-rows on each side of first row
        int n_x = (int) ((bigRadius + smallRadius) / (2 * smallRadius)); // number of circles on left and right
        for (double y = -(2 * n_y) * sqrt_3 * smallRadius; y <= n_y * 2 * sqrt_3
                * smallRadius * epsilon; y += 2 * sqrt_3 * smallRadius) {
            for (double x = -(2 * n_x - 1) * smallRadius; x <= (2 * n_x - 1)
                    * smallRadius * epsilon; x += 2 * smallRadius) {
                hexagonalPlanePacking.add(new Point(x, y));
            }
        }

        // Add b-rows to hexagonal plane packing
        n_y = (int) ((bigRadius + 2 * sqrt_3 * smallRadius)
                / (2 * sqrt_3 * smallRadius)); // number of b-rows above origin
        n_x = (int) (bigRadius / (2 * smallRadius)); // number of additional circles left of origin in b-row
        for (double y = -(2 * n_y - 1) * sqrt_3
                * smallRadius; y <= (2 * n_y - 1) * sqrt_3 * smallRadius
                        * epsilon; y += 2 * sqrt_3 * smallRadius) {
            for (double x = -(2 * n_x) * smallRadius; x <= (2 * n_x)
                    * smallRadius * epsilon; x += 2 * smallRadius) {
                hexagonalPlanePacking.add(new Point(x, y));
            }
        }

        // Generate rows of regular plane packing
        n_y = (int) (bigRadius / (2 * smallRadius)); // number of additional circles above origin
        n_x = (int) ((bigRadius + smallRadius) / (2 * smallRadius)); // number of circles left of origin
        for (double y = -2 * n_y * smallRadius; y <= 2 * n_y * smallRadius
                * epsilon; y += 2 * smallRadius) {
            for (double x = -(2 * n_x - 1) * smallRadius; x <= (2 * n_x - 1)
                    * smallRadius * epsilon; x += 2 * smallRadius) {
                regularPlanePacking.add(new Point(x, y));
            }
        }
    }

    /**
     * Returns a list of point representing the centers of the packed circles.
     * 
     * @return a list of point representing the centers of the packed circles
     */
    public List<Point> getCircleCenters() {
        return circleCenters;
    }

    /**
     * Returns a list of the polygon reference points of the packed polygons. Returns <code>null</code>
     * if no polygon was set.
     * 
     * @return a list of the polygon reference points of the packed polygons; <code>null</code> if no
     *         polygon was set
     */
    public List<Point> getPolygonReferencePoints() {
        return polygonReferencePoints;
    }

    /**
     * Returns the number of circles in the best packing.
     * 
     * @return the number of circles in the best packing
     */
    public int getN() {
        return circleCenters.size();
    }

    /**
     * @param showCircles if <code>true</code> the <code>toSVGElement</code> method draws the outlines
     *                    of the circles in the packing.
     */
    public static void setShowCircles(boolean showCircles) {
        CirclePacking.showCircles = showCircles;
    }

    /**
     * @param showPolygons if <code>true</code> the <code>toSVGElement</code> method draws the outlines
     *                     of the polygons in the packing.
     */
    public static void setShowPolygons(boolean showPolygons) {
        CirclePacking.showPolygons = showPolygons;
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
        group.add((new Circle(Point.ORIGIN, bigRadius)).toSVGElement());
        if (polygon != null) {
            if (showPolygons) {
                polygonReferencePoints.stream()
                        .map(referencePoint -> polygon.translate(new Vector(
                                polygon.getReferencePoint(), referencePoint)))
                        .forEach(placedPolygon -> group
                                .add(placedPolygon.toSVGElement()));
            }
            if (showCircles) {
                SVG.setStyle(showPolygons ? Style.DASHED : Style.SOLID);
                circleCenters.stream()
                        .map(center -> new Circle(center, smallRadius))
                        .forEach(circle -> group.add(circle.toSVGElement()));
            }
        } else {
            SVG.setStyle(Style.FILLED);
            circleCenters.stream()
                    .map(center -> new Circle(center, smallRadius))
                    .forEach(circle -> group.add(circle.toSVGElement()));
        }
        return group.toSVGElement();
    }
}
