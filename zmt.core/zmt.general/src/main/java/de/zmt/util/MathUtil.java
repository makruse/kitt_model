package de.zmt.util;

/**
 * Methods for various math operations.
 * 
 * @author mey
 *
 */
public final class MathUtil {
    private MathUtil() {

    }

    /**
     * @param value
     * @param min
     * @param max
     * @return {@code value} clamped between {@code min} and {@code max}.
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

}
