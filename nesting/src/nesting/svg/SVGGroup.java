package nesting.svg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Essentially a list of svg elements which can be transformed into a single svg element.
 */
public class SVGGroup implements Drawable {

    private List<SVGElement> elements = new ArrayList<>();

    /**
     * Adds an svg element to this svg group.
     * 
     * @param svgElement an svg element to be added to the group
     */
    public void add(SVGElement svgElement) {
        elements.add(svgElement);
    }

    /**
     * Returns the concatenation of the svg strings of all the elements in the group.
     * 
     * @return concatenation of the svg strings of all elements in the group
     */
    public String getString() {
        return elements.stream().map(e -> e.string)
                .collect(Collectors.joining());
    }

    /**
     * Calculates and returns the collective bounds of all elements in this group.
     * 
     * @return the collective bounds of the group
     */
    public SVGBounds getBounds() {
        return new SVGBounds(
                elements.stream().mapToDouble(e -> e.bounds.minX).min()
                        .getAsDouble(),
                elements.stream().mapToDouble(e -> e.bounds.maxX).max()
                        .getAsDouble(),
                elements.stream().mapToDouble(e -> e.bounds.minY).min()
                        .getAsDouble(),
                elements.stream().mapToDouble(e -> e.bounds.maxY).max()
                        .getAsDouble());
    }

    /**
     * CReturns an svg element constructed from the svg strings and collective bounds of all the
     * elements in the group.
     * 
     * @return an svg element containing all the elements in the group
     */
    @Override
    public SVGElement toSVGElement() {
        return (elements.isEmpty()) ? SVGElement.EMPTY: new SVGElement(getString(), getBounds());
    }
}
