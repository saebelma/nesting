package nesting.svg;

/**
 * The <code>Drawable</code> interface should be implemented by any class whose instances have a
 * useful graphical representation. They must define a <code>toSVGElement</code> method with no
 * arguments that returns an <code>SVGElement</code> representing the object.
 */
public interface Drawable {

    /**
     * @return an SVG element representing the object
     */
    public SVGElement toSVGElement();
}
