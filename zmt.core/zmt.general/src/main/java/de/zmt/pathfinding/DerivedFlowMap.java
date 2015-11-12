package de.zmt.pathfinding;

import java.util.*;

import sim.field.grid.ObjectGrid2D;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.util.Double2D;

/**
 * This class provides a skeletal implementation for a flow map that is derived
 * from other underlying pathfinding maps. Changes in an underlying map are
 * propagated automatically if it implements the {@link MapChangeNotifier}
 * interface.
 * <p>
 * Implementing classes need to specify abstract
 * {@link #computeDirection(int, int)} which is called when an update is needed.
 * Otherwise cached directions are fetched from a grid.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of underlying maps
 */
abstract class DerivedFlowMap<T extends PathfindingMap> extends LazyUpdatingMap
	implements GridBackedFlowMap, MapChangeListener {
    private static final long serialVersionUID = 1L;

    /** Grid containing a flow direction for every location. */
    private final ObjectGrid2D flowMapGrid;

    /** Pathfinding maps to derive flow directions from. */
    private final Collection<T> underlyingMaps = new ArrayList<>(1);

    /**
     * Constructs a new {@code DerivedFlowMap} with given dimensions.
     * 
     * @param width
     *            width of map
     * @param height
     *            height of map
     */
    public DerivedFlowMap(int width, int height) {
	super(width, height);
	flowMapGrid = new ObjectGrid2D(width, height);
    }

    /**
     * Adds a map to derive directions from. If it is a
     * {@link MapChangeNotifier}, this object is added as listener so that
     * changes will trigger an update for affected locations. A forced update of
     * all locations is triggered after the addition.
     * <p>
     * Dimensions for added maps must match those of this map.
     * 
     * @param map
     *            map to add
     * @return {@code true} if the map was added
     */
    protected boolean addMap(T map) {
	if (addMapInternal(map)) {
	    forceUpdateAll();
	    return true;
	}
	return false;
    }

    /**
     * Adds a map to derive directions from. If it is a
     * {@link MapChangeNotifier}, this object is added as listener so that
     * changes will trigger an update for affected locations.
     * <p>
     * Dimensions for added maps must match those of this map.
     * 
     * @param map
     *            map to add
     * @return {@code true} if the map was added
     */
    final boolean addMapInternal(T map) {
	if (map.getWidth() != getWidth() || map.getHeight() != getHeight()) {
	    throw new IllegalArgumentException("Expected: is <" + getWidth() + ", " + getHeight() + ">\n" + "but: was <"
		    + map.getWidth() + ", " + map.getHeight() + ">");
	}
	if (map instanceof MapChangeNotifier) {
	    ((MapChangeNotifier) map).addListener(this);
	}

	return underlyingMaps.add(map);
    }

    /**
     * Removes an underlying pathfinding map. If it is a
     * {@link MapChangeNotifier} the change listener that was added before is
     * also removed. A forced update of all directions is triggered after
     * removal.
     * 
     * @param map
     * @return <code>true</code> if the map could be removed
     */
    protected boolean removeMap(Object map) {
	if (map instanceof MapChangeNotifier) {
	    ((MapChangeNotifier) map).removeListener(this);
	}

	if (underlyingMaps.remove(map)) {
	    forceUpdateAll();
	    return true;
	}
	return false;
    }

    /**
     * Read-only accessor to underlying maps for deriving directions.
     *
     * @return pathfinding maps
     */
    protected final Collection<T> getUnderlyingMaps() {
	return Collections.unmodifiableCollection(underlyingMaps);
    }

    /**
     * Gets the grid containing the cached directions as {@link Double2D}
     * objects.
     */
    @Override
    public final ObjectGrid2D getMapGrid() {
	return flowMapGrid;
    }

    @Override
    public void updateIfDirty(int x, int y) {
	// update underlying maps before updating itself
	for (T map : underlyingMaps) {
	    if (map instanceof MapUpdateHandler) {
		((MapUpdateHandler) map).updateIfDirty(x, y);
	    }
	}
	super.updateIfDirty(x, y);
    }

    @Override
    protected void update(int x, int y) {
	getMapGrid().set(x, y, computeDirection(x, y));
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
     * Obtains flow direction for given location after updating updating if
     * needed.
     */
    @Override
    public Double2D obtainDirection(int x, int y) {
	updateIfDirty(x, y);
	return (Double2D) getMapGrid().get(x, y);
    }

    @Override
    public FieldPortrayable<ObjectGrid2D> providePortrayable() {
	return new FieldPortrayable<ObjectGrid2D>() {

	    @Override
	    public ObjectGrid2D getField() {
		return getMapGrid();
	    }
	};
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + underlyingMaps;
    }
}