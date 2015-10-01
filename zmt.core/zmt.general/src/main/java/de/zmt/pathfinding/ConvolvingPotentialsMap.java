package de.zmt.pathfinding;

import de.zmt.pathfinding.filter.ConvolveOp;
import de.zmt.sim.field.grid.BooleanGrid;
import de.zmt.sim.portrayal.portrayable.*;
import sim.field.grid.DoubleGrid2D;

/**
 * Implementation of a {@link LazyUpdatingMap} that will run a
 * {@link ConvolveOp} when refreshed. For example, this can be used to create a
 * blurred version of a changing map.
 * 
 * @author mey
 *
 */
public class ConvolvingPotentialsMap extends LazyUpdatingMap
	implements PotentialsMap, ProvidesPortrayable<FieldPortrayable<DoubleGrid2D>> {

    protected final DoubleGrid2D mapGrid;
    private final ConvolveOp convolveOp;
    private final DoubleGrid2D src;

    /**
     * Constructs a new ConvolvingPotentialsMap.
     * 
     * @param convolveOp
     *            the {@link ConvolveOp} to be used
     * @param src
     *            the source for the convolution
     */
    public ConvolvingPotentialsMap(ConvolveOp convolveOp, DoubleGrid2D src) {
	// extends equal origin positions
	this(convolveOp, src, new BooleanGrid(src.getWidth(), src.getHeight()));
    }

    public ConvolvingPotentialsMap(ConvolveOp convolveOp, DoubleGrid2D src, BooleanGrid dirtyGrid) {
	// extends equal origin positions
	super(dirtyGrid, convolveOp.getKernel().getxOrigin(), convolveOp.getKernel().getyOrigin());
	this.mapGrid = new DoubleGrid2D(src.getWidth(), src.getHeight());
	this.convolveOp = convolveOp;
	this.src = src;
	forceRefreshAll();
    }

    public DoubleGrid2D getMapGrid() {
	return mapGrid;
    }

    @Override
    public double obtainPotential(int x, int y) {
	refreshIfDirty(x, y);
	return mapGrid.get(x, y);
    }

    /**
     * Refreshes given cell by running the convolve operation on it.
     */
    @Override
    protected void refresh(int x, int y) {
	mapGrid.set(x, y, convolveOp.filter(x, y, src));
    }

    @Override
    public FieldPortrayable<DoubleGrid2D> providePortrayable() {
	return new FieldPortrayable<DoubleGrid2D>() {

	    @Override
	    public DoubleGrid2D getField() {
		// provide an up-to-date field
		refreshIfDirtyAll();
		return mapGrid;
	    }
	};
    }
}
