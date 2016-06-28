package de.zmt.pathfinding;

import de.zmt.pathfinding.filter.GridFilteringOp;
import sim.display.GUIState;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.PotentialMapInspector;
import sim.portrayal.inspector.ProvidesInspector;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.portrayal.portrayable.ProvidesPortrayable;

/**
 * Implementation of a {@link AbstractDynamicMap} that will run a
 * {@link GridFilteringOp} when updated. For example, this can be used to create
 * a blurred version of a changing map.
 * 
 * @author mey
 *
 */
public class FilteringPotentialMap extends AbstractDynamicMap implements GridBackedPotentialMap,
        EdgeHandledPotentialMap, ProvidesPortrayable<FieldPortrayable<DoubleGrid2D>>, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    private final DoubleGrid2D mapGrid;
    private final GridFilteringOp filteringOp;
    private final DoubleGrid2D src;

    /**
     * Constructs a new {@link FilteringPotentialMap} with given filtering op.
     * 
     * @param filteringOp
     *            the {@link GridFilteringOp} to be used
     * @param src
     *            the source for the filtering
     */
    public FilteringPotentialMap(GridFilteringOp filteringOp, DoubleGrid2D src) {
        // extends equal origin positions
        super(src.getWidth(), src.getHeight(), filteringOp.getxExtend(), filteringOp.getyExtend());
        this.mapGrid = new DoubleGrid2D(src.getWidth(), src.getHeight());
        this.filteringOp = filteringOp;
        this.src = src;
        forceUpdateAll();
    }

    @Override
    public double obtainPotential(int x, int y) {
        updateIfDirty(x, y);
        return mapGrid.get(x, y);
    }

    @Override
    public DoubleGrid2D getMapGrid() {
        return mapGrid;
    }

    /**
     * Updates given location by running the filtering operation on it.
     */
    @Override
    protected void update(int x, int y) {
        mapGrid.set(x, y, filteringOp.filter(x, y, src));
    }

    @Override
    public EdgeHandler getEdgeHandler() {
        return filteringOp.getEdgeHandler();
    }

    /**
     * Returns the field portrayable.<br>
     * <b>NOTE:</b> This displays the field as is, including not-updated dirty
     * locations. To ensure the correct state is drawn, call
     * {@link #updateIfDirtyAll()} before.
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

    @Override
    public Inspector provideInspector(GUIState state, String name) {
        return new PotentialMapInspector(state, this);
    }
}
