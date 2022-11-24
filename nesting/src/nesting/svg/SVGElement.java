package nesting.svg;

/**
 * An svg element contains an svg string representation of a geometric object and an svg bounds
 * object encoding the bounds of the object.
 */
public class SVGElement {

    /**
     * An empty <code>SVGElement</code>.
     */
    public static final SVGElement EMPTY = new SVGElement("", SVGBounds.EMPTY);

    /**
     * A string containing an svg representation of some geometric object.
     */
    public final String string;

    /**
     * The bounds of the geometric object.
     */
    public final SVGBounds bounds;

    /**
     * Constructs an svg element from the given values.
     * 
     * @param svgString string containing the svg representation of some geometric object
     * @param svgBounds the bounds of the geometric object
     */
    public SVGElement(String svgString, SVGBounds svgBounds) {
        this.string = svgString;
        this.bounds = svgBounds;
    }
}
