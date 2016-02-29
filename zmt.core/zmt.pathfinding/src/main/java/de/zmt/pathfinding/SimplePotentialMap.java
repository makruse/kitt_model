package de.zmt.pathfinding;

import sim.display.GUIState;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.PotentialMapInspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * A potential map backed by a {@link DoubleGrid2D}.
 * 
 * @author mey
 *
 */
public class SimplePotentialMap extends SimplePathfindingMap<DoubleGrid2D>
	implements GridBackedPotentialMap, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code SimplePotentialMap} backed by given grid.
     * 
     * @param mapGrid
     *            grid that backs this map
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

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new PotentialMapInspector(state, this);
    }
}