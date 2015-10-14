package de.zmt.pathfinding;

import de.zmt.pathfinding.filter.ConvolveOp;
import de.zmt.sim.portrayal.portrayable.*;
import sim.field.grid.DoubleGrid2D;

/**
 * Implementation of a {@link LazyUpdatingMap} that will run a
 * {@link ConvolveOp} when updated. For example, this can be used to create a
 * blurred version of a changing map.
 * 
 * @author mey
 *
 */
public class ConvolvingPotentialMap extends LazyUpdatingMap
	implements PotentialMap, ProvidesPortrayable<FieldPortrayable<DoubleGrid2D>> {
    private static final long serialVersionUID = 1L;

    private final DoubleGrid2D mapGrid;
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
    public ConvolvingPotentialMap(ConvolveOp convolveOp, DoubleGrid2D src) {
	// extends equal origin positions
	super(src.getWidth(), src.getHeight(), convolveOp.getKernel().getxOrigin(),
		convolveOp.getKernel().getyOrigin());
	this.mapGrid = new DoubleGrid2D(src.getWidth(), src.getHeight());
	this.convolveOp = convolveOp;
	this.src = src;
	forceUpdateAll();
    }

    @Override
    public double obtainPotential(int x, int y) {
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
     * locations.
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
