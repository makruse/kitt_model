package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.DIRECTION_NEUTRAL;

import sim.field.grid.ObjectGrid2D;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.util.Double2D;

/**
 * This class provides a skeletal implementation for a {@link FlowMap} that can
 * listen for changes elsewhere and update accordingly.
 * 
 * @author mey
 *
 */
abstract class ListeningFlowMap extends LazyUpdatingMap implements FlowMap, MapChangeListener {
    private static final long serialVersionUID = 1L;

    /** Grid containing a flow direction for every location. */
    private final ObjectGrid2D flowMapGrid;

    /**
     * Constructs a new listening flow map.
     *
     * @param width
     *            width of map
     * @param height
     *            height of map
     */
    public ListeningFlowMap(int width, int height) {
	super(width, height);
	flowMapGrid = new ObjectGrid2D(width, height, DIRECTION_NEUTRAL);
    }

    /**
     * Gets the grid containing a flow direction for every location.
     *
     * @return flow map grid
     */
    protected ObjectGrid2D getFlowMapGrid() {
	return flowMapGrid;
    }

    @Override
    protected void update(int x, int y) {
	getFlowMapGrid().set(x, y, computeDirection(x, y));
    }

    /**
     * Called only when the location needs to be updated after locations have
     * been marked dirty. Otherwise direction vectors are fetched from a cache.
     * Implementing classes must specify the result.
     * 
     * @param x
     *            the x-coordinate of location
     * @param y
     *            the y-coordinate of location
     * @return result of direction at given location
     */
    protected abstract Double2D computeDirection(int x, int y);

    /** Mark the location dirty when notified. */
    @Override
    public void changed(int x, int y) {
	markDirty(x, y);
    }

    /**
     * Obtains flow direction for given location after updating from integral
     * maps if needed.
     */
    @Override
    public Double2D obtainDirection(int x, int y) {
	updateIfDirty(x, y);
	return (Double2D) getFlowMapGrid().get(x, y);
    }

    @Override
    public FieldPortrayable<ObjectGrid2D> providePortrayable() {
	return new FieldPortrayable<ObjectGrid2D>() {

	    @Override
	    public ObjectGrid2D getField() {
		return getFlowMapGrid();
	    }
	};
    }

}