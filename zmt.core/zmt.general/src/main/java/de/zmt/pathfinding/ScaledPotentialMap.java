package de.zmt.pathfinding;

import sim.field.grid.DoubleGrid2D;

/**
 * A potential map backed by a {@link DoubleGrid2D} which applies a scale factor
 * on each obtained value.
 * 
 * @author mey
 *
 */
// what about the unscaled portrayal?
public class ScaledPotentialMap extends SimplePotentialMap {
    private static final long serialVersionUID = 1L;

    /** Scale factor to be applied on every potential that is applied. */
    private final double scaleFactor;

    /**
     * Constructs a new {@code ScaledPotentialMap} backed by given grid with a
     * scale factor.
     * 
     * @param mapGrid
     *            grid that backs this map
     * @param scaleFactor
     *            scale factor to be applied on every obtained value
     */
    public ScaledPotentialMap(DoubleGrid2D mapGrid, double scaleFactor) {
	super(mapGrid);
	this.scaleFactor = scaleFactor;
    }

    @Override
    public double obtainPotential(int x, int y) {
	return super.obtainPotential(x, y) * scaleFactor;
    }
}
