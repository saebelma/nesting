package nesting.irregular;

import java.util.Collection;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

import nesting.geometry.elements.*;
import nesting.svg.*;
import nesting.tuple.TupleNesting;

public abstract class NestingCriterion {

    abstract public void addPolygon(IntegerVector position, Polygon polygon);

    abstract public Result evaluate(Collection<IntegerVector> positions,
            Polygon polygon);

    abstract public List<Result> getAllResults();

    abstract public Collection<Point> getConvexHull_total();

    public IntegerVector getBestPosition(TupleNesting.Space space,
            Polygon polygon) {
        return evaluate(space.getFitPoints(), polygon).position;
    }

    /**
     * The result of an evaluation. Contains the evaluated position and the area of the resulting convex
     * hull polygon.
     */
    public static class Result implements Drawable {
        public final IntegerVector position;
        public final double evaluation;

        /**
         * Constructs a new result with the given parameters.
         * 
         * @param position   the evaluated position
         * @param evaluation the area of the resulting convex hull polygon
         */
        public Result(IntegerVector position, double evaluation) {
            this.position = position;
            this.evaluation = evaluation;
        }

        @Override
        public String toString() {
            return "Result(" + position.toString() + ", " + evaluation + ")";
        }

        /**
         * Returns an svg representation of the search position color-coded on a green-yellow-red scale
         * depending on the quality of the result.
         */
        @Override
        public SVGElement toSVGElement() {
            SVG.setStrokeColor("rgb(" + Math.min(255, (int) evaluation) + ", "
                    + Math.min(255, (511 - (int) evaluation)) + ", 0)");
            SVGElement svg = position.toSVGElement();
            SVG.setStrokeColor("black");
            return svg;
        }
    }

    protected List<Result> mapEvaluationsToRGB(List<Result> results) {
        final double min = results.stream().mapToDouble(r -> r.evaluation).min()
                .getAsDouble();
        final double max = results.stream().mapToDouble(r -> r.evaluation).max()
                .getAsDouble();
        DoubleUnaryOperator mapToColorValue = d -> (d - min) / (max - min)
                * 511;
        return results.stream()
                .map(r -> new Result(r.position,
                        mapToColorValue.applyAsDouble(r.evaluation)))
                .collect(Collectors.toList());
    }
}
