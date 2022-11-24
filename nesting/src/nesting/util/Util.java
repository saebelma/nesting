package nesting.util;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Collection of various utility functions.
 */
public class Util {

    private Util() {
    };

    /**
     * Rounds a double to two decimals.
     * 
     * @param x a floating-point number
     * @return the number rounded to two decimals
     */
    public static double roundToTwoDecimals(double x) {
        return Math.round(x * 100) / 100;
    }

    /**
     * Returns the concatenation of all the <code>toString</code> results of the elements in a
     * collection separated by line feeds.
     * 
     * @param <E>        element type in collection
     * @param collection some collection
     * @return result of printing the elements in this collection
     */
    public static <E> String printCollection(Collection<E> collection) {
        return collection.stream().map(e -> e.toString())
                .collect(Collectors.joining("\n"));
    }

}
