package de.zmt.pathfinding;

import de.zmt.pathfinding.filter.ConvolveOp;
import de.zmt.sim.field.grid.BooleanGrid;
import de.zmt.sim.portrayal.portrayable.*;
import sim.field.grid.DoubleGrid2D;

/**
 * Implementation of a {@link LazyUpdatingMap} that will run a
 * {@link ConvolveOp} when updated. For example, this can be used to create a
 * blurred version of a changing map.
 * <p>
 * Automatic update is enabled by default and dirty locations are updated on
 * request.
 * 
 * @author mey
 *
 */
public class ConvolvingPotentialMap extends LazyUpdatingMap
	implements PotentialMap, ProvidesPortrayable<FieldPortrayable<DoubleGrid2D>> {

    private final DoubleGrid2D mapGrid;
    private final ConvolveOp convolveOp;
    private final DoubleGrid2D src;
    private boolean automaticUpdate = true;

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
     * If set to <code>true</code>, the update of dirty locations happen
     * automatically when that location is read. Set it to <code>false</code> to
     * handle updates of dirty locations manually, i.e. an update if dirty
     * method needs to be called somewhere.
     * 
     * @see #updateIfDirty(int, int)
     * @see #updateIfDirtyAll()
     * @param automaticUpdate
     */
    public void setAutoUpdate(boolean automaticUpdate) {
	this.automaticUpdate = automaticUpdate;
    }

    @Override
    public double obtainPotential(int x, int y) {
	if (automaticUpdate) {
	    updateIfDirty(x, y);
	}
	return mapGrid.get(x, y);
    }

    /**
     * Updates given location by running the convolve operation on it.
     */
    @Override
    protected void update(int x, int y) {
	mapGrid.set(x, y, convolveOp.filter(x, y, src));
    }

    /**
     * Returns a portrayable of the field.<br>
     * <b>NOTE:</b> This displays the field as is, including not-updated dirty
     * locations, even when automatic update is enabled.
     */
    @Override
    public FieldPortrayable<DoubleGrid2D> providePortrayable() {
	return new FieldPortrayable<DoubleGrid2D>() {

	    @Override
	    public DoubleGrid2D getField() {
		return mapGrid;
	    }
	};
    }
}
