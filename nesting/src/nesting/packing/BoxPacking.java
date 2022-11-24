package nesting.packing;

import static nesting.irregular.NestingParameters.partClearance;
import static nesting.irregular.NestingParameters.tableRadius;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nesting.geometry.*;
import nesting.geometry.elements.*;
import nesting.irregular.NestingParameters;
import nesting.svg.*;
import nesting.svg.SVG.Style;

/**
 * A simple algorithm for packing rectangles into a circular area while maintaining a minimum
 * distance between rectangles and to the circumference of the circle. The algorithm first tries to
 * arrange rectangles in horizontal rows, starting at the bottom of the circle. The starting
 * position of the bottom row is varied systematically on a raster of 10 mm. At each row position,
 * the algorithm calculates the available length and inserts the appropriate number of rectangles.
 * The process is repeated for a pattern of vertical columns.
 * 
 * Finally, the packing with the maximum number of rectangles is selected. If there are multiple
 * packings with the same number, the most centered one is selected for aesthetic reasons.
 * 
 * The run-time complexity is roughly <i>O</i>(<i>m</i> <i>n</i><sup>2</sup>), where <i>m</i> is the
 * quotient between box height / width and rasterization and <i>n</i> is the number of rows /
 * columns. In practice, the run-time is negligible because the involved <i>n</i> are very small.
 */
public class BoxPacking implements Drawable {

    private final static double raster = 10.0; // mm
    private static boolean showBoxes = true;
    private static boolean showPolygons = true;

    private Polygon polygon;
    private Rectangle minimumBoundingBox;
    private AxisAlignedRectangle axisAlignedBoundingBox;
    private double totalBoxHeight;
    private double totalBoxWidth;

    private List<Point> packing;
    private List<Point> minimumBoundingBoxCorners;
    private List<Point> polygonReferencePoints;

    /**
     * Constructs a box packing algorithm. Computes the minimum bounding box of the given polygon and
     * then computes box packings in a rows-or-columns pattern. Takes parameters for table radius and
     * part clearance from the <code>Nesting</code> class.
     * 
     * @param polygon a polygon
     */
    public BoxPacking(Polygon polygon) {
        this.polygon = polygon;

        RotatingCalipers rc = new RotatingCalipers(ConvexHull.of(polygon));
        minimumBoundingBox = rc.getMinimumBoundingBox();
        axisAlignedBoundingBox = minimumBoundingBox.align();

        totalBoxHeight = axisAlignedBoundingBox.height + 2 * partClearance;
        totalBoxWidth = axisAlignedBoundingBox.width + 2 * partClearance;

        List<List<Point>> packings = new ArrayList<>();

        // Add row packings
        for (int i = 0; i * raster < totalBoxHeight; i++)
            packings.add(rowPacking(-tableRadius + i * raster));

        // Add column packings
        for (int i = 0; i * raster < totalBoxWidth; i++)
            packings.add(columnPacking(-tableRadius + i * raster));

        // Get maximal, most centered packing
        int max_n = packings.stream().mapToInt(pk -> pk.size()).max()
                .getAsInt();
        packing = packings.stream().filter(pk -> pk.size() == max_n)
                .min((a, b) -> Double.compare(offCenter(a), offCenter(b)))
                .get();

        // Transform axis-aligned box corners into minimum bounding box corners
        minimumBoundingBoxCorners = packing.stream()
                .map(boxCorner -> boxCorner.rotate(Point.ORIGIN,
                        minimumBoundingBox.angleToXAxis))
                .collect(Collectors.toList());

        // Transform minimum bounding box corners into polygon reference points
        Vector toReferencePoint = new Vector(
                minimumBoundingBox.bottomLeftCorner,
                polygon.getReferencePoint());
        polygonReferencePoints = minimumBoundingBoxCorners.stream()
                .map(mbbCorner -> mbbCorner.translate(toReferencePoint))
                .collect(Collectors.toList());
    }

    // Returns a row packing starting from the given y-position
    private List<Point> rowPacking(double position) {
        List<Point> rowPacking = new ArrayList<>();
        while (position + totalBoxHeight <= tableRadius) {
            rowPacking.addAll(rowAt(position));
            position += totalBoxHeight;
        }
        return rowPacking;
    }

    // Returns a row at the given y-position
    private List<Point> rowAt(double position) {
        double availableLength = availableLength(position, totalBoxHeight);
        int n = (int) (availableLength / totalBoxWidth);
        double x_start = -(n * totalBoxWidth) / 2;
        return IntStream.range(0, n)
                .mapToDouble(i -> x_start + totalBoxWidth * i)
                .mapToObj(x -> new Point(x + partClearance,
                        position + partClearance))
                .collect(Collectors.toList());
    }

    // Returns a column packing starting from the given x-position
    private List<Point> columnPacking(double position) {
        List<Point> columnPacking = new ArrayList<>();
        while (position + totalBoxWidth <= tableRadius) {
            columnPacking.addAll(columnAt(position));
            position += totalBoxWidth;
        }
        return columnPacking;
    }

    // Returns a column at the given x-position
    private List<Point> columnAt(double position) {
        double availableLength = availableLength(position, totalBoxWidth);
        int n = (int) (availableLength / totalBoxHeight);
        double y_start = -(n * totalBoxHeight) / 2;
        return IntStream.range(0, n)
                .mapToDouble(i -> y_start + totalBoxHeight * i)
                .mapToObj(y -> new Point(position + partClearance,
                        y + partClearance))
                .collect(Collectors.toList());
    }

    // Calculates the available length at a given position for a given box dimension
    private double availableLength(double position, double totalBoxLength) {
        return Math.min(
                chordLength(position + partClearance,
                        tableRadius - partClearance) + 2 * partClearance,
                chordLength(position + totalBoxLength - partClearance,
                        tableRadius - partClearance) + 2 * partClearance);
    }

    // Calculates the chord length at a distance d from circle center for a circle of radius r
    private static double chordLength(double d, double r) {
        return 2 * Math.sqrt(r * r - d * d);
    }

    // Calculates a value for the "off-centeredness" of a packing
    private double offCenter(List<Point> pk) {
        double max_x = pk.stream()
                .mapToDouble(p -> p.x + axisAlignedBoundingBox.width).max()
                .getAsDouble();
        double max_y = pk.stream()
                .mapToDouble(p -> p.y + axisAlignedBoundingBox.height).max()
                .getAsDouble();
        double min_x = pk.stream().mapToDouble(p -> p.x).min().getAsDouble();
        double min_y = pk.stream().mapToDouble(p -> p.y).min().getAsDouble();
        return Math.abs(max_x + min_x) + Math.abs(max_y + min_y); // One of these terms = 0
    }

    /**
     * Returns a list of the positions of the corner of the minimum bounding box of the polygon in the
     * calculated box packing.
     * 
     * @return a list of the positions of the corner of the minimum bounding box of the polygon in the
     *         packing
     */
    public List<Point> getMinimumBoundingBoxCorners() {
        return minimumBoundingBoxCorners;
    }

    /**
     * Returns a list of positions of the reference points of the polygons in the packing.
     * 
     * @return a list of positions of the reference points of the polygons in the packing
     */
    public List<Point> getPolygonReferencePoints() {
        return polygonReferencePoints;
    }

    /**
     * Returns the number of boxes in the best packing.
     * 
     * @return the number of boxes in the best packing
     */
    public int getN() {
        return minimumBoundingBoxCorners.size();
    }

    /**
     * @param showBoxes if <code>true</code> the <code>toSVGElement</code> method draws the outlines of
     *                  the minimum bounding boxes in the packing.
     */
    public static void setShowBoxes(boolean showBoxes) {
        BoxPacking.showBoxes = showBoxes;
    }

    /**
     * @param showPolygons if <code>true</code> the <code>toSVGElement</code> method draws the outlines
     *                     of the polygons in the packing.
     */
    public static void setShowPolygons(boolean showPolygons) {
        BoxPacking.showPolygons = showPolygons;
    }

    /**
     * Draws the table edge and the polygons and/or minimum bounding boxes in the packing. If only one
     * is selected, the elements are drawn with solid lines. If both polygons and boxes are selected,
     * the boxes are drawn with dashed lines.
     */
    @Override
    public SVGElement toSVGElement() {
        SVGGroup group = new SVGGroup();
        SVG.setStyle(Style.SOLID);
        group.add(
                (new Circle(Point.ORIGIN, NestingParameters.tableRadius)).toSVGElement());
        if (showPolygons) {
            polygonReferencePoints.stream()
                    .map(referencePoint -> polygon.translate(new Vector(
                            polygon.getReferencePoint(), referencePoint)))
                    .forEach(placedPolygon -> group
                            .add(placedPolygon.toSVGElement()));
        }
        if (showBoxes) {
            SVG.setStyle(showPolygons ? Style.DASHED : Style.SOLID);
            minimumBoundingBoxCorners.stream()
                    .map(corner -> new Rectangle(corner,
                            minimumBoundingBox.width, minimumBoundingBox.height,
                            minimumBoundingBox.angleToXAxis))
                    .map(rectangle -> new ParallelCurve(
                            new Polygon(rectangle.getVertices())))
                    .forEach(parallelCurve -> group
                            .add(parallelCurve.toSVGElement()));
        }
        return group.toSVGElement();
    }
}
