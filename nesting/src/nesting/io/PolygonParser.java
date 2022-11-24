package nesting.io;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.csv.*;

import nesting.geometry.elements.Point;
import nesting.geometry.elements.Polygon;

/**
 * Parses polygons from CSV file. Assumes that polygon is given as list of line segments. Each line
 * segment is given by a pair of coordinates. Each coordinate is followed by a geometry number.
 * Verifies counter-clockwise order of vertices in each polygon.
 *
 */
public class PolygonParser {

    private PolygonParser() {
    };

    /**
     * Returns a list of polygons parsed from the given csv file.
     * 
     * @param csvFile a CSV file.
     * @return a list of polygons
     */
    public static List<Polygon> parsePolygonsFromCSVFile(File csvFile) {
        List<Polygon> polygons = new ArrayList<>();

        try {
            CSVParser parser = CSVParser.parse(csvFile,
                    Charset.defaultCharset(), CSVFormat.DEFAULT);
            List<CSVRecord> records = parser.getRecords();

            // Remove header
            records.remove(0);

            // Get list of distinct geometry numbers
            List<Integer> geometryNumbers = records.stream()
                    .map(r -> parseInt(r.get(2))).distinct().collect(toList());

            // Parse polygon vertices by geometry number
            for (Integer geometryNumber : geometryNumbers) {
                List<Point> vertices = records.stream()
                        .filter(r -> parseInt(r.get(2)) == geometryNumber)
                        .map(r -> new Point(parseDouble(r.get(0)),
                                parseDouble(r.get(1))))
                        .collect(toCollection(ArrayList::new));

                // Create polygon and verify order of vertices
                Polygon polygon = new Polygon(vertices);
                polygon.ensureCounterclockwiseOrderOfVertices();

                polygons.add(polygon);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return polygons;
    }

    /**
     * Parses polygons from a csv file and returns the polygon with the maximum diameter. In a set of
     * polygons representing a preform, this polygon should be the outline polygon.
     * 
     * @param csvFile a CSV file
     * @return a polygon
     */
    public static Polygon parseOutlineFromCSVFile(File csvFile) {
        List<Polygon> polygons = parsePolygonsFromCSVFile(csvFile);
        List<Double> maximumDistancesBetweenVerticesInPolygon = polygons
                .stream()
                .map(polygon -> polygon.vertices.stream()
                        .mapToDouble(firstVertex -> polygon.vertices.stream()
                                .mapToDouble(secondVertex -> Point
                                        .distance(firstVertex, secondVertex))
                                .max().getAsDouble())
                        .max().getAsDouble())
                .collect(Collectors.toList());
        int indexOfOutline = maximumDistancesBetweenVerticesInPolygon.indexOf(
                Collections.max(maximumDistancesBetweenVerticesInPolygon));
        return polygons.get(indexOfOutline);
    }
}
