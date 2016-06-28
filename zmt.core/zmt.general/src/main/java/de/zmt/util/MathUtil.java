package de.zmt.util;

/**
 * Methods for various math operations.
 * 
 * @author mey
 *
 */
public final class MathUtil {
    private static final double PI_TIMES_2 = Math.PI * 2;

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

    /**
     * @param value
     * @param min
     * @param max
     * @return {@code value} clamped between {@code min} and {@code max}.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(Math.min(value, max), min);
    }

    /**
     * Normalizes a radian angle between PI and -PI.
     * 
     * @param angle
     *            in radians
     * @return normalized angle
     */
    public static double normalizeAngle(double angle) {
        if (angle > Math.PI) {
            return angle - MathUtil.PI_TIMES_2;
        }
        if (angle < -Math.PI) {
            return angle + MathUtil.PI_TIMES_2;
        }
        return angle;
    }

}
