package nesting.application;

import java.io.*;

import nesting.geometry.*;
import nesting.geometry.elements.*;
import nesting.io.PolygonParser;
import nesting.irregular.IrregularShapeNesting;
import nesting.packing.BoxPacking;
import nesting.packing.CirclePacking;
import nesting.svg.SVG;
import nesting.svg.SVG.StrokeWidth;
import nesting.svg.SVG.Style;
import nesting.svg.SVGGroup;
import nesting.tuple.TupleNesting;

/**
 * A simple command line application demonstrating various packing and nesting algorithms. By
 * calling the <code>main</code> method with the name of a csv file containing a preform polygon
 * (omit the file extension), the application generates an html file containing graphical
 * representations of various properties of the polygon (convex hull, minimum bounding box, smallest
 * enclosing circle) and of several packing and nesting results.
 */
public class Nest {

    public static void main(String[] args) throws IOException {
        String preformName = args[0];
        String fileName = preformName + ".csv";
        File file = new File(fileName);
        Polygon polygon = PolygonParser.parseOutlineFromCSVFile(file);

        SVG.setStrokeWidth(new StrokeWidth(3, "px"));
        SVG.setPath("");
        BufferedWriter writer = SVG.openHTMLFile(preformName + ".html");

        long startTime = System.currentTimeMillis();
        Polygon convexHull = ConvexHull.of(polygon);
        System.out.println("Convex hull in "
                + (System.currentTimeMillis() - startTime) + " ms");

        SVGGroup group = new SVGGroup();
        SVG.setStyle(Style.SOLID);
        group.add(polygon.toSVGElement());
        SVG.setStyle(Style.DASHED);
        group.add(convexHull.toSVGElement());
        SVG.writeToSVGViewBox(writer, group.toSVGElement());

        startTime = System.currentTimeMillis();
        Rectangle minimumBoundingBox = (new RotatingCalipers(convexHull))
                .getMinimumBoundingBox();
        System.out.println("Minimum bounding box in "
                + (System.currentTimeMillis() - startTime) + " ms");

        group = new SVGGroup();
        SVG.setStyle(Style.SOLID);
        group.add(polygon.toSVGElement());
        SVG.setStyle(Style.DASHED);
        group.add(minimumBoundingBox.toSVGElement());
        SVG.writeToSVGViewBox(writer, group.toSVGElement());

        startTime = System.currentTimeMillis();
        Circle smallestEnclosingCircle = (new RandomizedIncrementalConstruction(
                convexHull)).getSmallestEnclosingCircle();
        System.out.println("Smallest enclosing circle in "
                + (System.currentTimeMillis() - startTime) + " ms");

        group = new SVGGroup();
        SVG.setStyle(Style.SOLID);
        group.add(polygon.toSVGElement());
        SVG.setStyle(Style.DASHED);
        group.add(smallestEnclosingCircle.toSVGElement());
        SVG.writeToSVGViewBox(writer, group.toSVGElement());

        startTime = System.currentTimeMillis();
        Polygon offsetCurve = OffsetCurve.of(polygon);
        System.out.println("Offset curve in "
                + (System.currentTimeMillis() - startTime) + " ms");

        group = new SVGGroup();
        SVG.setStyle(Style.SOLID);
        group.add(polygon.toSVGElement());
        SVG.setStyle(Style.DASHED);
        group.add(offsetCurve.toSVGElement());
        SVG.writeToSVGViewBox(writer, group.toSVGElement());

        startTime = System.currentTimeMillis();
        BoxPacking boxPacking = new BoxPacking(polygon);
        System.out.println(
                "Box packing in " + (System.currentTimeMillis() - startTime)
                        + " ms (n = " + boxPacking.getN() + ")");

        SVG.writeToSVGViewBox(writer, boxPacking.toSVGElement());

        startTime = System.currentTimeMillis();
        CirclePacking circlePacking = new CirclePacking(polygon);
        System.out.println(
                "Circle packing in " + (System.currentTimeMillis() - startTime)
                        + " ms (n = " + circlePacking.getN() + ")");

        SVG.writeToSVGViewBox(writer, circlePacking.toSVGElement());

        startTime = System.currentTimeMillis();
        IrregularShapeNesting irregularShapeNesting = new IrregularShapeNesting(
                polygon);
        System.out.println(
                "Irregular shape nesting with convex hull criterion in "
                        + (System.currentTimeMillis() - startTime) + " ms (n = "
                        + irregularShapeNesting.getN() + ")");

        SVG.writeToSVGViewBox(writer, irregularShapeNesting.toSVGElement());

        startTime = System.currentTimeMillis();
        IrregularShapeNesting.setUseSmallestEnclosingCircleCriterion(true);
        irregularShapeNesting = new IrregularShapeNesting(polygon);
        System.out.println(
                "Irregular shape nesting with smallest enclosing circle criterion in "
                        + (System.currentTimeMillis() - startTime) + " ms (n = "
                        + irregularShapeNesting.getN() + ")");

        SVG.writeToSVGViewBox(writer, irregularShapeNesting.toSVGElement());

        startTime = System.currentTimeMillis();
        TupleNesting.setUseSmallestEnclosingCircleCriterionForPairs(false);
        TupleNesting.setUseSmallestEnclosingCircleCriterionForQuadruples(false);
        TupleNesting.setUseSmallestEnclosingCircleCriterionForNesting(false);
        TupleNesting tupleNesting = new TupleNesting(offsetCurve);
        System.out.println("Tuple nesting type 1 (convex hull) in "
                + (System.currentTimeMillis() - startTime) + " ms (n = "
                + tupleNesting.getN() + ")");

        SVG.writeToSVGViewBox(writer, tupleNesting.toSVGElement());

        startTime = System.currentTimeMillis();
        TupleNesting.setUseSmallestEnclosingCircleCriterionForPairs(true);
        TupleNesting.setUseSmallestEnclosingCircleCriterionForQuadruples(true);
        TupleNesting.setUseSmallestEnclosingCircleCriterionForNesting(false);
        tupleNesting = new TupleNesting(offsetCurve);
        System.out
                .println("Tuple nesting type 2 (smallest enclosing circle) in "
                        + (System.currentTimeMillis() - startTime) + " ms (n = "
                        + tupleNesting.getN() + ")");

        SVG.writeToSVGViewBox(writer, tupleNesting.toSVGElement());

        SVG.closeHTMLFile(writer);

    }

}
