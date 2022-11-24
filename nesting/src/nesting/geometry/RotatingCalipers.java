package nesting.geometry;

import java.util.*;
import java.util.stream.Collectors;

import nesting.geometry.elements.*;

/**
 * Constructs the minimum-area bounding box of convex polygon based on the rotating calipers
 * paradigm. Runtime is <i>O</i>(<i>n</i>).
 */
public class RotatingCalipers {

    private final static double oneHalfPI = 0.5 * Math.PI;
    private final static double oneAndAHalfPI = 1.5 * Math.PI;
    private final static double[] baseAngles = { 0.0, oneHalfPI, Math.PI,
            oneAndAHalfPI };

    private Polygon polygon;
    private Calipers calipers;
    private double minAngle;
    private Line[] line = new Line[4];
    private List<Rectangle> boundingBoxes;
    private Rectangle minimumBoundingBox;

    /**
     * Constructs a rotating calipers algorithm which computes the minimum bounding box of the given
     * convex polygon.
     * 
     * @param polygon a convex polygon
     */
    public RotatingCalipers(Polygon polygon) {
        this.polygon = polygon;
        this.boundingBoxes = new ArrayList<>();

        // Construct initial calipers
        this.calipers = initialCalipers();

        // If any angle is zero, add bounding box
        minAngle = Arrays.stream(calipers.angleAtAntipode).min().getAsDouble();
        if (epsilonEqualToZero(minAngle)) {
            boundingBoxes.add(boundingBox());
        }

        while (!(calipers.totalRotation > oneHalfPI)) {

            // Update antipodal points
            for (int i = 0; i < 4; i++)
                while (epsilonEqualToZero(calipers.angleAtAntipode[i])) {
                    calipers.indexOfAntipode[i] = polygon.vertices
                            .modulo(calipers.indexOfAntipode[i] + 1);
                    calipers.angleAtAntipode[i] = angleAtAntipode(
                            calipers.totalRotation, calipers.indexOfAntipode[i],
                            i);
                }

            // Get minimum angle
            minAngle = Arrays.stream(calipers.angleAtAntipode).min()
                    .getAsDouble();

            // Rotate by minimum angle
            calipers.totalRotation += minAngle;
            calipers.angleAtAntipode = anglesAtAntipodes(calipers.totalRotation,
                    calipers.indexOfAntipode);

            // Add bounding box
            boundingBoxes.add(boundingBox());
        }

        minimumBoundingBox = boundingBoxes.stream()
                .min((a, b) -> Double.compare(a.getArea(), b.getArea())).get();
    }

    private boolean epsilonEqualToZero(double a) {
        return Math.abs(a) < 0.001;
    }

    private Calipers initialCalipers() {

        // Find the extremal points with ties sorted lexically
        Point p_minY = polygon.vertices.stream()
                .min((p_1, p_2) -> Double.compare(p_1.y, p_2.y) != 0
                        ? Double.compare(p_1.y, p_2.y)
                        : Double.compare(p_1.x, p_2.x))
                .get();
        Point p_maxX = polygon.vertices.stream()
                .max((p_1, p_2) -> Double.compare(p_1.x, p_2.x) != 0
                        ? Double.compare(p_1.x, p_2.x)
                        : (-1) * Double.compare(p_1.y, p_2.y))
                .get();
        Point p_maxY = polygon.vertices.stream()
                .max((p_1, p_2) -> Double.compare(p_1.y, p_2.y) != 0
                        ? Double.compare(p_1.y, p_2.y)
                        : Double.compare(p_1.x, p_2.x))
                .get();
        Point p_minX = polygon.vertices.stream()
                .min((p_1, p_2) -> Double.compare(p_1.x, p_2.x) != 0
                        ? Double.compare(p_1.x, p_2.x)
                        : (-1) * Double.compare(p_1.y, p_2.y))
                .get();

        int[] indices = new int[] { polygon.vertices.indexOf(p_minY),
                polygon.vertices.indexOf(p_maxX),
                polygon.vertices.indexOf(p_maxY),
                polygon.vertices.indexOf(p_minX) };

        // Calculate angles at extremal points
        double[] angles = anglesAtAntipodes(0, indices);

        return new Calipers(0, indices, angles);
    }

    private double[] anglesAtAntipodes(double rotation, int[] indices) {
        double[] angles = new double[4];
        for (int i = 0; i < 4; i++) {
            angles[i] = angleAtAntipode(rotation, indices[i], i);
        }
        return angles;
    }

    private double angleAtAntipode(double rotation, int indexOfAntipode,
            int indexOfCaliper) {
        double angleOfEdge = polygon.getEdges().get(indexOfAntipode).getVector()
                .getAngle();
        double angleOfCaliper = rotation + baseAngles[indexOfCaliper];
        double angleAtAntipode = angleOfEdge - angleOfCaliper;
        return (angleAtAntipode < 0 && !epsilonEqualToZero(angleAtAntipode))
                ? 2 * Math.PI + angleAtAntipode
                : angleAtAntipode;
    }

    private Rectangle boundingBox() {
        for (int i = 0; i < 4; i++)
            line[i] = new Line(
                    polygon.vertices.get(calipers.indexOfAntipode[i]),
                    baseAngles[i] + calipers.totalRotation);
        Point bottomLeftCorner = Line.getIntersection(line[3], line[0]);
        Point bottomRightCorner = Line.getIntersection(line[0], line[1]);
        Point topLeftCorner = Line.getIntersection(line[2], line[3]);
        double width = Point.distance(bottomLeftCorner, bottomRightCorner);
        double height = Point.distance(bottomLeftCorner, topLeftCorner);
        return new Rectangle(bottomLeftCorner, width, height,
                calipers.totalRotation);
    }

    /**
     * Returns a list of all bounding boxes that were computed in the course of the algorithm. These are
     * all bounding boxes collinear with one edge of the convex polygon.
     * 
     * @return a list of computed bounding boxes
     */
    public List<Rectangle> getBoundingBoxes() {
        return boundingBoxes;
    }

    /**
     * Returns the minimum-area bounding box of the given convex polygon.
     * 
     * @return the minimum-area bounding box of the given convex polygon
     */
    public Rectangle getMinimumBoundingBox() {
        return minimumBoundingBox;
    }

    /**
     * Datastructure for storing and updating a set of calipers.
     */
    public class Calipers {

        /**
         * The total rotation of this set of calipers relative to the intial calipers.
         */
        public double totalRotation;

        /**
         * The indices of the current antipodal points in the list of vertices of the given convex polygon.
         */
        public int[] indexOfAntipode;

        /**
         * The angles between caliper and edge originating at an antipodal point.
         */
        public double[] angleAtAntipode;

        /**
         * Constructs a set of calipers with the given values.
         * 
         * @param totalRotationAngle initial total angle of rotation of this set of calipers (zero)
         * @param indexOfAntipode    initial indices of antipodal points
         * @param angleAtAntipode    initial angles at antipodal points
         */
        public Calipers(double totalRotationAngle, int[] indexOfAntipode,
                double[] angleAtAntipode) {
            this.totalRotation = totalRotationAngle;
            this.indexOfAntipode = indexOfAntipode;
            this.angleAtAntipode = angleAtAntipode;
        }

        @Override
        public String toString() {
            String string = "Calipers(" + totalRotation + ", {";
            string += Arrays.stream(indexOfAntipode).boxed()
                    .map(i -> i.toString()).collect(Collectors.joining(", "))
                    + "}, {";
            string += Arrays.stream(angleAtAntipode).boxed()
                    .map(d -> d.toString()).collect(Collectors.joining(", "))
                    + "})";
            return string;
        }
    }
}
