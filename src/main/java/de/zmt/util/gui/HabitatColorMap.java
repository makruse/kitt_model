package de.zmt.util.gui;

import java.awt.Color;

import de.zmt.util.*;
import sim.util.gui.ColorMap;

/**
 * {@link ColorMap} for portraying the habitat field. Returns habitat colors for
 * ordinal values stored within a grid.
 * 
 * @see MapUtil#createHabitatGridFromMap(ec.util.MersenneTwisterFast,
 *      java.awt.image.BufferedImage)
 * @author cmeyer
 * 
 */
public class HabitatColorMap implements ColorMap {

    @Override
    public Color getColor(double level) {
	return Habitat.values()[(int) level].getColor();
    }

    @Override
    public int getRGB(double level) {
	return getColor(level).getRGB();
    }

    @Override
    public int getAlpha(double level) {
	return getColor(level).getAlpha();
    }

    @Override
    public boolean validLevel(double level) {
	return level < Habitat.values().length;
    }

    @Override
    public double defaultValue() {
	return Habitat.DEFAULT.ordinal();
    }

}
