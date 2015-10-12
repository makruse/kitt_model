package de.zmt.pathfinding;

import de.zmt.pathfinding.filter.ConvolveOp;
import de.zmt.sim.field.grid.BooleanGrid;
import de.zmt.sim.portrayal.portrayable.*;
import sim.field.grid.DoubleGrid2D;

/**
 * Implementation of a {@link LazyUpdatingMap} that will run a
 * {@link ConvolveOp} when refreshed. For example, this can be used to create a
 * blurred version of a changing map.
 * <p>
 * Default refresh behavior is set to manual and the refresh needs to be
 * initiated from outside.
 * 
 * @author mey
 *
 */
public class ConvolvingPotentialMap extends LazyUpdatingMap
	implements PotentialMap, ProvidesPortrayable<FieldPortrayable<DoubleGrid2D>> {

    private final DoubleGrid2D mapGrid;
    private final ConvolveOp convolveOp;
    private final DoubleGrid2D src;
    private boolean automaticRefresh = false;

    /**
     * Constructs a new ConvolvingPotentialsMap.
     * 
     * @param convolveOp
     *            the {@link ConvolveOp} to be used
     * @param src
     *            the source for the convolution
     */
    public ConvolvingPotentialMap(ConvolveOp convolveOp, DoubleGrid2D src) {
	this(convolveOp, src, new BooleanGrid(src.getWidth(), src.getHeight()));
    }

    /**
     * Constructs a new ConvolvingPotentials map with a given dirty grid.
     * 
     * @param convolveOp
     *            the {@link ConvolveOp} to be used
     * @param src
     *            the source for the convolution
     * @param dirtyGrid
     *            grid to be used for dirty flags
     */
    public ConvolvingPotentialMap(ConvolveOp convolveOp, DoubleGrid2D src, BooleanGrid dirtyGrid) {
	// extends equal origin positions
	super(dirtyGrid, convolveOp.getKernel().getxOrigin(), convolveOp.getKernel().getyOrigin());
	this.mapGrid = new DoubleGrid2D(src.getWidth(), src.getHeight());
	this.convolveOp = convolveOp;
	this.src = src;
	forceUpdateAll();
    }

    public DoubleGrid2D getMapGrid() {
	return mapGrid;
    }

    /**
     * If set to true, the refresh of dirty locations happen automatically when
     * that location is read.
     * 
     * @param automaticRefresh
     */
    public void setAutoUpdate(boolean automaticRefresh) {
	this.automaticRefresh = automaticRefresh;
    }

    @Override
    public double obtainPotential(int x, int y) {
	if (automaticRefresh) {
	    updateIfDirty(x, y);
	}
	return mapGrid.get(x, y);
    }

    /**
     * Refreshes given location by running the convolve operation on it.
     */
    @Override
    protected void update(int x, int y) {
	mapGrid.set(x, y, convolveOp.filter(x, y, src));
    }

    @Override
    public FieldPortrayable<DoubleGrid2D> providePortrayable() {
	return new FieldPortrayable<DoubleGrid2D>() {

	    @Override
	    public DoubleGrid2D getField() {
		/*
		 * TODO GUI needs a way to refresh the field before drawing it.
		 * Create a Steppable for that calling updateIfDirtalAll and
		 * pass it to scheduleImmediatelyAfter in GuiState.
		 */
		return mapGrid;
	    }
	};
    }
}
