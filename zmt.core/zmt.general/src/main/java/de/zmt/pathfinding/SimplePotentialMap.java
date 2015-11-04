package de.zmt.pathfinding;

import sim.field.grid.DoubleGrid2D;

/**
 * A potential map backed by a {@link DoubleGrid2D}.
 * 
 * @author mey
 *
 */
public class SimplePotentialMap extends SimplePathfindingMap<DoubleGrid2D> implements PotentialMap {
    /**
     * Constructs a new {@code SimplePotentialMap} backed by given grid.
     * 
     * @param mapGrid
     */
    public SimplePotentialMap(DoubleGrid2D mapGrid) {
	super(mapGrid);
    }

    /**
     * Constructs a new {@code SimplePotentialMap} backed by a new grid
     * containing given values.
     * 
     * @param values
     */
    SimplePotentialMap(double[][] values) {
	this(new DoubleGrid2D(values));
    }

    @Override
    public double obtainPotential(int x, int y) {
	return getMapGrid().get(x, y);
    }
}