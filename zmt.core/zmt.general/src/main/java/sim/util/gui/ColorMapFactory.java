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
	return new SimpleColorMap(minLevel, maxLevel, minColor,
		maxColor);
    }

    /**
     * Create a {@code ColorMap} for displaying a {@link PotentialMap}. Negative
     * values are in red, positive ones in blue. Minimum and maximum values are
     * displayed with {@code alpha} while zero is displayed fully transparent.
     * 
     * @param alpha
     * @return resulting {@code ColorMap} to display a {@link PotentialMap}
     */
    public static ColorMap createForPotentials(int alpha) {
	ColorMap repulsiveMap = createWithAlpha(PotentialMap.MAX_REPULSIVE_VALUE, 0, alpha, 0, COLOR_REPULSIVE);
	ColorMap attractiveMap = createWithAlpha(0, PotentialMap.MAX_ATTRACTIVE_VALUE, 0, alpha, COLOR_ATTRACTIVE);
	return new CompositeColorMap(repulsiveMap, attractiveMap);
    }

    private static Color createColorWithAlpha(int alpha, Color color) {
	return new Color(((alpha << 24) | 0x00FFFFFF) & color.getRGB(), true);
    }
}
