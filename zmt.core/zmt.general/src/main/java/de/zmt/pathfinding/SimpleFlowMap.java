package de.zmt.pathfinding;

import sim.field.grid.ObjectGrid2D;
import sim.util.Double2D;

/**
 * A flow map backed by a {@link ObjectGrid2D}.
 * 
 * @author mey
 *
 */
public class SimpleFlowMap extends SimplePathfindingMap<ObjectGrid2D> implements FlowMap {
    /**
     * Constructs a new {@code SimpleFlowMap} backed by given grid.<br>
     * <b>NOTE:</b> The grid must only contain direction vectors as
     * {@link Double2D} objects.
     * 
     * 
     * @param mapGrid
     */
    public SimpleFlowMap(ObjectGrid2D mapGrid) {
	super(mapGrid);
    }

    @Override
    public Double2D obtainDirection(int x, int y) {
	return (Double2D) getMapGrid().get(x, y);
    }

}
