package sim.util.gui;

import java.awt.Color;

import de.zmt.pathfinding.PotentialMap;

/**
 * Factory to create {@link ColorMap} objects for various purposes.
 * 
 * @author mey
 *
 */
public final class ColorMapFactory {
    private static final Color COLOR_REPULSIVE = Color.RED;
    private static final Color COLOR_ATTRACTIVE = Color.BLUE;

    private ColorMapFactory() {

    }

    /**
     * Create a {@code ColorMap} with a constant color that changes in
     * transparency.
     * 
     * @param minLevel
     *            minimum level in range
     * @param maxLevel
     *            maximum level in range
     * @param minAlpha
     *            alpha at {@code minLevel}
     * @param maxAlpha
     *            alpha at {@code maxLevel}
     * @param color
     *            the constant color
     * @return resulting {@code ColorMap}
     */
    public static ColorMap createWithAlpha(double minLevel, double maxLevel, int minAlpha, int maxAlpha, Color color) {
	Color minColor = createColorWithAlpha(minAlpha, color);
	Color maxColor = createColorWithAlpha(maxAlpha, color);
	return new SimpleColorMap(minLevel, maxLevel, minColor, maxColor);
    }

    /**
     * Create a {@code ColorMap} for displaying a {@link PotentialMap}. Negative
     * values are in red, positive ones in blue. Maximum values are displayed
     * with {@code alpha} while zero is displayed fully transparent.
     * 
     * @param maxRepulsiveValue
     *            negative value for the maximum repulsive potential displayed
     * @param maxAttractiveValue
     *            positive value for the maximum attractive potential displayed
     * @param alpha
     * @return resulting {@code ColorMap} to display a {@link PotentialMap}
     */
    public static ColorMap createForPotentials(double maxRepulsiveValue, double maxAttractiveValue, int alpha) {
	ColorMap repulsiveMap = createForRepulsivePotentials(maxRepulsiveValue, alpha);
	ColorMap attractiveMap = createForAttractivePotentials(maxAttractiveValue, alpha);
	return new CompositeColorMap(repulsiveMap, attractiveMap);
    }

    /**
     * Create a {@code ColorMap} for displaying a {@link PotentialMap} using
     * default maximum values. Negative values are in red, positive ones in
     * blue. Maximum are displayed with {@code alpha} while zero is displayed
     * fully transparent.
     * 
     * @see PotentialMap#MAX_REPULSIVE_VALUE
     * @see PotentialMap#MAX_ATTRACTIVE_VALUE
     * @param alpha
     * @return resulting {@code ColorMap} to display a {@link PotentialMap}
     */
    public static ColorMap createForPotentials(int alpha) {
	return createForPotentials(PotentialMap.MAX_REPULSIVE_VALUE, PotentialMap.MAX_ATTRACTIVE_VALUE, alpha);
    }

    /**
     * Create a {@code ColorMap} for displaying only repulsive values within a
     * {@link PotentialMap}. Maximum are displayed with {@code alpha} while zero
     * is displayed fully transparent.
     * 
     * @param maxRepulsiveValue
     *            negative value for the maximum repulsive potential displayed
     * @param alpha
     * @return resulting {@code ColorMap} to display a {@link PotentialMap}
     */
    public static ColorMap createForRepulsivePotentials(double maxRepulsiveValue, int alpha) {
	if (maxRepulsiveValue > 0) {
	    throw new IllegalArgumentException("Max repulsive potential must be negative.");
	}
	return createWithAlpha(maxRepulsiveValue, 0, alpha, 0, COLOR_REPULSIVE);
    }

    /**
     * Create a {@code ColorMap} for displaying only repulsive values within a
     * {@link PotentialMap} using the default maximum value. Maximum values are
     * displayed with {@code alpha} while zero is displayed fully transparent.
     * 
     * @see PotentialMap#MAX_REPULSIVE_VALUE
     * @param alpha
     * @return resulting {@code ColorMap} to display a {@link PotentialMap}
     */
    public static ColorMap createForRepulsivePotentials(int alpha) {
	return createForRepulsivePotentials(PotentialMap.MAX_REPULSIVE_VALUE, alpha);
    }

    /**
     * Create a {@code ColorMap} for displaying only attractive values within a
     * {@link PotentialMap}. Maximum values are displayed with {@code alpha}
     * while zero is displayed fully transparent.
     * 
     * @param maxAttractiveValue
     *            positive value for the maximum attractive potential displayed
     * @param alpha
     * @return resulting {@code ColorMap} to display a {@link PotentialMap}
     */
    public static ColorMap createForAttractivePotentials(double maxAttractiveValue, int alpha) {
	if (maxAttractiveValue < 0) {
	    throw new IllegalArgumentException("Max attractive potential must be positive.");
	}
	return createWithAlpha(0, maxAttractiveValue, 0, alpha, COLOR_ATTRACTIVE);
    }

    /**
     * Create a {@code ColorMap} for displaying only attractive values within a
     * {@link PotentialMap} using the default maximum value. Maximum values are
     * displayed with {@code alpha} while zero is displayed fully transparent.
     * 
     * @see PotentialMap#MAX_ATTRACTIVE_VALUE
     * @param alpha
     * @return resulting {@code ColorMap} to display a {@link PotentialMap}
     */
    public static ColorMap createForAttractivePotentials(int alpha) {
	return createForAttractivePotentials(PotentialMap.MAX_ATTRACTIVE_VALUE, alpha);
    }

    private static Color createColorWithAlpha(int alpha, Color color) {
	return new Color(((alpha << 24) | 0x00FFFFFF) & color.getRGB(), true);
    }
}