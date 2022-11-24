package nesting.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nesting.geometry.elements.*;
import nesting.svg.*;
import nesting.svg.SVG.StrokeWidth;
import nesting.svg.SVG.Style;

/**
 * This class is intended to estimate the minimum radius of a circle enclosing n = 1 to 35 unit
 * squares arranged in a pattern of rows. Table center is at position (0.0, 0.0). Unit of length is
 * based on the unit square having a side length of 1.0.
 */
public class SquaresInCircle {

    /**
     * The radii of the optimal solutions for cases n = 1 to n = 35.
     */
    public final static double[] radii = { Math.sqrt(2) / 2, Math.sqrt(5) / 2,
            5 * Math.sqrt(17) / 16, Math.sqrt(2), Math.sqrt(10) / 2, 1.688,
            Math.sqrt(13) / 2, 1.978, Math.sqrt(1105) / 16,
            3 * Math.sqrt(2) / 2, 2.214, Math.sqrt(5), 2.3607, 2.5, 2.533,
            Math.sqrt(11009) / 40, 2.677, Math.sqrt(481) / 8, 2.807, 2.893,
            Math.sqrt(34) / 2, 3.036, 3.073, 3.110, Math.sqrt(5809) / 24,
            Math.sqrt(41) / 2, 13 * Math.sqrt(145) / 48, 3.3471, 3.412,
            Math.sqrt(27985) / 48, 5 * Math.sqrt(2) / 2, Math.sqrt(40385) / 56,
            Math.sqrt(13), 3.629, Math.sqrt(53) / 2 };
    static String imagePath = "C:\\Users\\Markus Säbel\\Dropbox\\MASTER OF SCIENCE\\Abschlussmodul\\Nesting-Saebel\\Latex\\images\\";

    private SquaresInCircle() {
    };

    /**
     * Executes the simulation.
     * 
     * @param args (not used)
     * @throws IOException if something goes wrong with writing into the output file
     */
    public static void main(String[] args) throws IOException {

        // Run algorithm
        SVG.setPath(imagePath);
        SVG.setStrokeWidth(new StrokeWidth(0.1, "%"));
        SquaresInCircle simulation = new SquaresInCircle();
        int index = 1;
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            double radius = radii[i];
            radius *= 1000;
            radius *= 1.01;
            Circle table = new Circle(new Point(0, 0), radius);
            List<Point> positions = simulation.getMaximalPacking(radius);
            sum += positions.size();
            System.out.println("case " + index + ", r = " + (radius / 1000)
                    + ", max_n = " + positions.size());
            List<AxisAlignedRectangle> squares = positions.stream()
                    .map(p -> new AxisAlignedRectangle(p, 1000, 1000))
                    .collect(Collectors.toList());

            // Write svg
            SVGGroup group = new SVGGroup();
            SVG.setStyle(Style.SOLID);
            SVG.setStrokeWidth(new StrokeWidth(0.3, "%"));
            group.add(table.toSVGElement());
            SVG.setStyle(Style.FILLED);
            squares.stream()
                    .forEach(square -> group.add(square.toSVGElement()));
            SVGElement svg = group.toSVGElement();
            SVG.writeToSVGFile(svg, "scc" + i + ".svg");
            index++;
        }
        System.out.println("average_max_n = " + (sum / 35.0));

    }

    private List<Point> getMaximalPacking(double radius) {
        List<List<Point>> packings = new ArrayList<>();
        double startingPosition = -radius;

        // Rasterized starting position loop
        while (startingPosition < -radius + 1000 && startingPosition < 0) {
            List<Point> packing = layOutRowsStartingAt(startingPosition,
                    radius);
            packings.add(packing);
            startingPosition++;
        }

        // Get maximal, most centered packing
        int max_n = packings.stream().mapToInt(pk -> pk.size()).max()
                .getAsInt();
        return packings.stream().filter(pk -> pk.size() == max_n)
                .min((a, b) -> Double.compare(offCenter(a), offCenter(b)))
                .get();
    }

    // Calculates a value for the "off-centeredness" of a packing
    private double offCenter(List<Point> pk) {
        return pk.stream().map(p -> p.translate(new Vector(500, 500)))
                .mapToDouble(p -> Point.distance(p, Point.ORIGIN)).max()
                .getAsDouble();
    }

    private List<Point> layOutRowsStartingAt(double position, double radius) {
        List<Point> squares = new ArrayList<>();
        while (position + 1000 < radius) {
            squares.addAll(layOutRowAt(position, radius));
            position += 1000;
        }
        return squares;
    }

    // Lay out one row at a given position
    private List<Point> layOutRowAt(double position, double radius) {
        double lengthOfChord = Math.min(lengthOfChordAt(position, radius),
                lengthOfChordAt(position + 1000, radius));
        int n = (int) lengthOfChord / 1000;
        double startX = -(n * 1000) / 2;
        return IntStream.range(0, n).mapToDouble(i -> startX + 1000 * i)
                .mapToObj(x -> new Point(x, position))
                .collect(Collectors.toList());
    }

    private double lengthOfChordAt(double p, double r) {
        double d = Math.abs(p);
        return 2 * Math.sqrt(r * r - d * d);
    }
}
