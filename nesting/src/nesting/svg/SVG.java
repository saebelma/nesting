package nesting.svg;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;

import nesting.geometry.elements.LineSegment;
import nesting.geometry.elements.Point;

/**
 * Utility functions for creating SVG representations of geometric objects.
 */
public class SVG {

    private static String path = "C:\\Users\\Markus Säbel\\Dropbox\\MASTER OF SCIENCE\\Abschlussmodul\\Implementierung\\";

    private static Style style = Style.SOLID;
    private static StrokeWidth strokeWidth = new StrokeWidth(1, "px");
    private static String strokeColor = "black";
    private static int viewBoxPadding = 10;

    private SVG() {
    };

    /**
     * Returns the string value of the current svg stroke color, e.g. "black".
     * 
     * @return the current svg stroke color as string
     */
    public static String getStrokeColor() {
        return strokeColor;
    }

    /**
     * Sets the string value of the current svg stroke color, e.g. "black".
     * 
     * @param color svg stroke color as string
     */
    public static void setStrokeColor(String color) {
        SVG.strokeColor = color;
    }

    /**
     * Opens an html file with the appropriate header and returns a buffered writer for writing into
     * this file.
     * 
     * @param fileName a file name string (must include file type suffix)
     * @return a buffered writer for writing in this file
     * @throws IOException if something goes wrong with the html file
     */
    public static BufferedWriter openHTMLFile(String fileName)
            throws IOException {
        File htmlFile = new File(path + "/" + fileName);
        BufferedWriter writer = Files.newBufferedWriter(htmlFile.toPath());
        writer.write("<!DOCTYPE html>\n");
        writer.write("<html>\n");
        writer.write("<body>\n");
        return writer;
    }

    /**
     * Convenience method for writing a single svg element to an html file.
     * 
     * @param element  an svg element
     * @param fileName a file name (should include file extension ".html")
     * @throws IOException when something goes wrong with the file
     */
    public static void writeToHTMLFile(SVGElement element, String fileName)
            throws IOException {
        BufferedWriter writer = openHTMLFile(fileName);
        openSVGViewBox(writer, element.bounds);
        writer.write(element.string);
        closeSVGViewBox(writer);
        closeHTMLFile(writer);
    }

    /**
     * Opens an svg file and returns a buffered writer for writing into this file.
     * 
     * @param fileName file name string (must include file type suffix)
     * @return a buffered writer for writing in this file
     * @throws IOException if something goes wrong with the svg file
     */
    public static BufferedWriter openSVGFile(String fileName)
            throws IOException {
        File svgFile = new File(path + "/" + fileName);
        BufferedWriter writer = Files.newBufferedWriter(svgFile.toPath());
        return writer;
    }

    /**
     * Convenience method for writing a single svg element to an svg file.
     * 
     * @param element  an svg element
     * @param fileName a file name (should include file extension ".svg")
     * @throws IOException when something goes wrong with the file
     */
    public static void writeToSVGFile(SVGElement element, String fileName)
            throws IOException {
        BufferedWriter writer = openSVGFile(fileName);
        openSVGViewBox(writer, element.bounds);
        writer.write(element.string);
        closeSVGViewBox(writer);
        closeSVGFile(writer);
    }

    /**
     * Convenience method for writing a single svg element to an svg view box.
     * 
     * @param writer  the buffered writer for the file
     * @param element an svg element
     * @throws IOException when something goes wrong with the file
     */
    public static void writeToSVGViewBox(BufferedWriter writer,
            SVGElement element) throws IOException {
        openSVGViewBox(writer, element.bounds);
        writer.write(element.string);
        closeSVGViewBox(writer);
    }

    /**
     * Closes an html file.
     * 
     * @param writer the buffered writer for the file
     * @throws IOException if something goes wrong with the html file
     */
    public static void closeHTMLFile(BufferedWriter writer) throws IOException {
        writer.write("</body>\n");
        writer.write("</html>\n");
        writer.close();
    }

    /**
     * Closes an svg file
     * 
     * @param writer the buffered writer for the file
     * @throws IOException if something goes wrong with the svg file
     */
    public static void closeSVGFile(BufferedWriter writer) throws IOException {
        writer.close();
    }

    /**
     * Opens a view box with dimensions given by an svg bounds object. A default padding of 10 pixels is
     * added on all sides. Display widht is set at 1000 pixels.
     * 
     * @param writer the buffered writer for the html or svg file
     * @param bounds bounds of the svg objects to be displayed in the view box
     * @throws IOException if something goes wrong with writing into the file
     */
    public static void openSVGViewBox(BufferedWriter writer, SVGBounds bounds)
            throws IOException {
        writer.write("<svg width=\"" + (bounds.getWidth() + 2 * viewBoxPadding)
                + "\" viewBox=\"" + (bounds.minX - viewBoxPadding) + " "
                + (-1) * (bounds.maxY + viewBoxPadding) + " "
                + (bounds.getWidth() + 2 * viewBoxPadding) + " "
                + (bounds.getHeight() + 2 * viewBoxPadding) + "\">\n");
    }

    /**
     * Closes a view box.
     * 
     * @param writer the buffered writer for the html or svg file
     * @throws IOException if something goes wrong with writing into the file
     */
    public static void closeSVGViewBox(BufferedWriter writer)
            throws IOException {
        writer.write("</svg>\n");
    }

    /**
     * Convenience method for collecting the svg representations of a collection of drawable element
     * into an svg element.
     * 
     * @param collection a collection of drawable elements
     * @return an svg element
     */
    public static SVGElement collectionToSVGElement(
            Collection<? extends Drawable> collection) {
        SVGGroup group = new SVGGroup();
        collection.stream()
                .forEach(element -> group.add(element.toSVGElement()));
        return group.toSVGElement();
    }

    /**
     * Creates a coordinate grid with the given dimensions. Lines every 100 mm. Also creates a legend.
     * 
     * @param bounds an svg bounds object giving the dimensions of the required coordinate grid.
     * @return an svg representation of the coordinate grid
     */
    public static SVGElement getCoordinateGrid(SVGBounds bounds) {

        final int unit = 50;

        SVGGroup group = new SVGGroup();
        setStrokeColor("darkgrey");

        // lines every 100 mm
        double ceil_x = Math.ceil(bounds.minX / unit) * unit;
        double ceil_y = Math.ceil(bounds.minY / unit) * unit;
        double floor_x = Math.floor(bounds.maxX / unit) * unit;
        double floor_y = Math.floor(bounds.maxY / unit) * unit;

        // Add y-axis lines and legend
        for (double x = ceil_x; x <= floor_x; x += unit) {

            // line
            Point a = new Point(x, bounds.minY);
            Point b = new Point(x, bounds.maxY);
            LineSegment line = new LineSegment(a, b);
            if (x == 0.0) {
                setStrokeWidth(new StrokeWidth(3, "px"));
                group.add(line.toSVGElement());
                setStrokeWidth(new StrokeWidth(1, "px"));
            } else {
                group.add(line.toSVGElement());
            }

            // legend
            group.add(new SVGElement(getLegend(a, (int) x), bounds));
            group.add(new SVGElement(getLegend(b, (int) x), bounds));
        }

        // Add x-axis lines and legend
        for (double y = ceil_y; y <= floor_y; y += unit) {

            // line
            Point a = new Point(bounds.minX, y);
            Point b = new Point(bounds.maxX, y);
            LineSegment line = new LineSegment(a, b);
            if (y == 0.0) {
                setStrokeWidth(new StrokeWidth(3, "px"));
                group.add(line.toSVGElement());
                setStrokeWidth(new StrokeWidth(1, "px"));
            } else {
                group.add(line.toSVGElement());
            }
            // legend
            group.add(new SVGElement(getLegend(a, (int) y), bounds));
            group.add(new SVGElement(getLegend(b, (int) y), bounds));
        }

        setStrokeColor("black");
        return group.toSVGElement();
    }

    /**
     * Creates a fine coordinate grid with the given dimensions. Lines every 10 mm.
     * 
     * @param bounds an svg bounds object giving the dimensions of the required coordinate grid.
     * @return an svg representation of the coordinate grid
     */
    public static SVGElement getFineGrid(SVGBounds bounds) {

        final int unit = 10;

        SVGGroup group = new SVGGroup();
        setStrokeColor("lightgrey");

        // lines every 100 mm
        double ceil_x = Math.ceil(bounds.minX / unit) * unit;
        double ceil_y = Math.ceil(bounds.minY / unit) * unit;
        double floor_x = Math.floor(bounds.maxX / unit) * unit;
        double floor_y = Math.floor(bounds.maxY / unit) * unit;

        // Add y-axis lines
        for (double x = ceil_x; x <= floor_x; x += unit) {

            // line
            Point a = new Point(x, bounds.minY);
            Point b = new Point(x, bounds.maxY);
            LineSegment line = new LineSegment(a, b);
            if (x == 0.0) {
                setStrokeWidth(new StrokeWidth(3, "px"));
                group.add(line.toSVGElement());
                setStrokeWidth(new StrokeWidth(1, "px"));
            } else {
                group.add(line.toSVGElement());
            }
        }

        // Add x-axis lines
        for (double y = ceil_y; y <= floor_y; y += unit) {

            // line
            Point a = new Point(bounds.minX, y);
            Point b = new Point(bounds.maxX, y);
            LineSegment line = new LineSegment(a, b);
            if (y == 0.0) {
                setStrokeWidth(new StrokeWidth(3, "px"));
                group.add(line.toSVGElement());
                setStrokeWidth(new StrokeWidth(1, "px"));
            } else {
                group.add(line.toSVGElement());
            }
        }

        setStrokeColor("black");
        return group.toSVGElement();
    }

    private static String getLegend(Point position, int number) {
        return "<text x=\"" + (position.x + 10) + "\" y=\""
                + (-1) * (position.y - 10) + "\" stroke=\"" + strokeColor
                + "\">\n" + number + "\n</text>\n";
    }

    /**
     * Writes an html paragraph with the given text as caption.
     * 
     * @param writer  the buffered writer for the html file
     * @param caption a string
     * @throws IOException if something goes wrong with writing into the file
     */
    public static void caption(BufferedWriter writer, String caption)
            throws IOException {
        writer.write("<h1>" + caption + "</h1>");
    }

    /**
     * Set the stroke width parameter.
     * 
     * @param strokeWidth stroke width in pixels
     */
    public static void setStrokeWidth(StrokeWidth strokeWidth) {
        SVG.strokeWidth = strokeWidth;
    }

    /**
     * A set of three standard styles. Each style fixes stroke color (black), stroke width, fill.
     */
    public enum Style {

        /**
         * Solid stroke, no fill.
         */
        SOLID {
            @Override
            String toSVG() {
                return " stroke=\"" + strokeColor + "\" stroke-width=\""
                        + strokeWidth + "\" fill=\"none\"";
            }
        },

        /**
         * Dashed stroke, no fill.
         */
        DASHED {
            @Override
            String toSVG() {
                return " stroke=\"" + strokeColor + "\" stroke-width=\""
                        + strokeWidth + "\" fill=\"none\" stroke-dasharray=\""
                        + strokeWidth.toDashArray() + "\"";
            }
        },

        /**
         * Solid stroke, dark grey fill.
         */
        FILLED {
            @Override
            String toSVG() {
                return " stroke=\"" + strokeColor + "\" stroke-width=\""
                        + strokeWidth + "\" fill=\"darkgrey\"";
            }
        };

        /**
         * Returns an svg string expressing the chosen style.
         * 
         * @return an svg string expressing the chosen style.
         */
        abstract String toSVG();
    }

    /**
     * Sets the value of the style enum parameter.
     * 
     * @param style a style
     */
    public static void setStyle(Style style) {
        SVG.style = style;
    }

    /**
     * Returns an svg string representing the current style.
     * 
     * @return the svg string for the current style
     */
    public static String getStyle() {
        return style.toSVG();
    }

    public static void setPath(String path) {
        SVG.path = path;
    }

    /**
     * Class encapsulating stroke width value and unit.
     */
    public static class StrokeWidth {

        /**
         * Numerical part of stroke width, e.g. 1.
         */
        public final double value;

        /**
         * Stroke width unit, e.g. "px".
         */
        public final String unit;

        /**
         * Constructs a new stroke width with the given value and unit.
         * 
         * @param value stroke width numerical value
         * @param unit  stroke witdth unit
         */
        public StrokeWidth(double value, String unit) {
            this.value = value;
            this.unit = unit;
        }

        @Override
        public String toString() {
            return value + unit;
        }

        /**
         * Returns the string needed to configure a dashed stroke depending on stroke width.
         * 
         * @return dash array string
         */
        public String toDashArray() {
            return (4 * value) + unit;
        }
    }
}
