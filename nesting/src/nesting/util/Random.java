package nesting.util;

import nesting.geometry.elements.AxisAlignedRectangle;
import nesting.geometry.elements.Point;

/**
 * Utility class for generating random geometric elements.
 */
public class Random {

    private Random() {
    };

    /**
     * Returns a random point in the area specified by an axis-aligned rectangle.
     * 
     * @param area an area specified by an axis-aligned rectangle
     * @return a random point in that area
     */
    public static Point point(AxisAlignedRectangle area) {
        double x = area.bottomLeftCorner.x + Math.random() * area.width;
        double y = area.bottomLeftCorner.y + Math.random() * area.height;
        return new Point(x, y);
    }
}
