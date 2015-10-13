package de.zmt.pathfinding;

import java.util.*;

import de.zmt.sim.portrayal.portrayable.*;
import sim.field.grid.ObjectGrid2D;
import sim.util.Double2D;

/**
 * This class provides a skeletal implementation for a flow map that is derived
 * from other underlying pathfinding maps. If an underlying map changes and
 * these changes should be reflected, it must implement the {@link DynamicMap}
 * interface.
 * <p>
 * Implementing classes need to specify abstract
 * {@link #computeDirection(int, int)} which is called when an update is needed.
 * Otherwise directions are fetched from a grid where results of that method are
 * cached.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of underlying maps
 */
public abstract class DerivedFlowMap<T extends PathfindingMap>
	implements FlowMap, DynamicMap, ProvidesPortrayable<FieldPortrayable<ObjectGrid2D>> {

    /** Pathfinding maps to derive flow directions from. */
    private final Collection<T> integralMaps = new ArrayList<>();
    /** Cached direction vectors. */
    private final ObjectGrid2D flowMapGrid;
    /**
     * Updating map which locations are marked dirty when changes of underlying
     * maps are propagated.
     */
    private final LazyUpdatingMap updatingMap;
    /** Added to underlying maps to be notified of changes. */
    private final DynamicMap.ChangeListener myChangeListener = new DynamicMap.ChangeListener() {

	@Override
	public void changed(int x, int y) {
	    updatingMap.markDirty(x, y);
	}
    };

    /**
     * Constructs a new DerivedFlowMap with given dimensions.
     * 
     * @param width
     * @param height
     */
    public DerivedFlowMap(int width, int height) {
	super();
	flowMapGrid = new ObjectGrid2D(width, height);
	updatingMap = new LazyUpdatingMap(width, height) {

	    @Override
	    protected void update(int x, int y) {
		flowMapGrid.set(x, y, computeDirection(x, y));
	    }
	};
	updatingMap.forceUpdateAll();
    }

    /**
     * Adds a pathfinding map to derive directions from. If it is a
     * {@link DynamicMap} a listener is added so that changes will trigger
     * updating directions on affected locations. Dimensions for added maps must
     * match those of this map.
     * <p>
     * A forced update of all directions is triggered after add.
     * 
     * @param map
     *            map to add
     * @return {@code true} if the map was added
     */
    public boolean addMap(T map) {
	if (map.getWidth() != getWidth() || map.getHeight() != getWidth()) {
	    throw new IllegalArgumentException(
		    "Dimensions must match:\n" + "width: " + getWidth() + ", height: " + getHeight());
	}
	if (map instanceof DynamicMap) {
	    ((DynamicMap) map).addListener(myChangeListener);
	}
	if (integralMaps.add(map)) {
	    updatingMap.forceUpdateAll();
	    return true;
	}
	return false;
    }

    /**
     * Removes an underlying pathfinding map. If it is a {@link DynamicMap} the
     * change listener that was added before is also removed.
     * <p>
     * A forced update of all directions is triggered after removal.
     * 
     * @param map
     * @return {@code false} was not added before
     */
    public boolean removeMap(Object map) {
	if (map instanceof DynamicMap) {
	    ((DynamicMap) map).removeListener(myChangeListener);
	}

	if (integralMaps.remove(map)) {
	    updatingMap.forceUpdateAll();
	    return true;
	}
	return false;
    }

    /**
     * Called only when the location needs to be updated. Otherwise direction
     * vectors are fetched from a cache. Implementing classes must specify the
     * result derived from underlying pathfinding maps.
     * 
     * @param x
     *            the x-coordinate of location
     * @param y
     *            the y-coordinate of location
     * @return result of direction at given location
     */
    protected abstract Double2D computeDirection(int x, int y);

    /**
     * Read-only accessor to integral maps for deriving directions
     * 
     * @return integral maps
     */
    protected final Collection<T> getIntegralMaps() {
	return Collections.unmodifiableCollection(integralMaps);
    }

    @Override
    public Double2D obtainDirection(int x, int y) {
	updatingMap.updateIfDirty(x, y);
	return (Double2D) flowMapGrid.get(x, y);
    }

    @Override
    public int getWidth() {
	return flowMapGrid.getWidth();
    }

    @Override
    public int getHeight() {
	return flowMapGrid.getHeight();
    }

    @Override
    public void addListener(ChangeListener listener) {
	updatingMap.addListener(listener);
    }

    @Override
    public void removeListener(Object listener) {
	updatingMap.removeListener(listener);
    }

    @Override
    public FieldPortrayable<ObjectGrid2D> providePortrayable() {
	return new FieldPortrayable<ObjectGrid2D>() {

	    @Override
	    public ObjectGrid2D getField() {
		return flowMapGrid;
	    }
	};
    }
}