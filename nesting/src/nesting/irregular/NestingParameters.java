package nesting.irregular;

/**
 * Collection of nesting parameters.
 */
public class NestingParameters {

    private NestingParameters() {
    };

    /**
     * The radius of the production table in mm.
     */
    public static double tableRadius = 120 * 22.0 / 2;

    /**
     * The minimum distance between preforms on the table (part clearance).
     */
    public static double partClearance = 22.0;

    /**
     * The maximum normal distance between a point on the arc of a parallel curve and the tangent edge
     * of its polygonization in mm.
     */
    public static double maximumNormalDistanceForPolygonization = 1.0; // mm
}
