package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.DIRECTION_NEUTRAL;

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
 * Otherwise cached directions are fetched from a grid. In here the weight
 * factor associated with every underlying map can be used to define its
 * influence on the final result.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of underlying pathfinding maps
 */
abstract class DerivedFlowMap<T extends PathfindingMap> extends LazyUpdatingMap implements GridBackedFlowMap {
    private static final long serialVersionUID = 1L;

    /** Neutral weight factor. */
    public static final double NEUTRAL_WEIGHT = 1d;

    /** Grid containing a flow direction for every location. */
    private final ObjectGrid2D flowMapGrid;
    /** Pathfinding maps to derive flow directions from. */
    private final Collection<T> underlyingMaps = new ArrayList<>(1);
    /** Read-only view of {@link #underlyingMaps}. */
    private final Collection<T> underlyingMapsReadOnly = Collections.unmodifiableCollection(underlyingMaps);
    /** {@code Map} pointing from pathfinding map to the objects wrapping it. */
    private final Map<T, Double> weights = new HashMap<>();

    private final MapChangeListener myChangeListener = new MapChangeListener() {
	private static final long serialVersionUID = 1L;

	/** Mark the location dirty when notified. */
	@Override
	public void changed(int x, int y) {
	    markDirty(x, y);
	}
    };

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
	// no underlying maps yet, initialize all locations to neutral direction
	flowMapGrid = new ObjectGrid2D(width, height, DIRECTION_NEUTRAL);
    }

    /**
     * Adds a map to derive directions from. A forced update of all locations is
     * triggered after the addition. If the added map is a
     * {@link MapChangeNotifier}, a listener is added to the map so that changes
     * will trigger an update for affected locations. To clear the listener
     * reference, {@link #removeMap(Object)} has to be called.
     * <p>
     * Dimensions for added maps must match those of this map.
     * 
     * @param map
     *            map to add
     * @return {@code true} if the map was added
     */
    public boolean addMap(T map) {
	if (addMapInternal(map)) {
	    forceUpdateAll();
	    return true;
	}
	return false;
    }

    /**
     * Adds a map to derive directions from. If the added map is a
     * {@link MapChangeNotifier}, a listener is added to the map so that changes
     * will trigger an update for affected locations. To clear the listener
     * reference, {@link #removeMap(Object)} has to be called.
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
	if (map == this) {
	    throw new IllegalArgumentException("Cannot add itself as an underlying map.");
	}
	if (map instanceof MapChangeNotifier) {
	    ((MapChangeNotifier) map).addListener(myChangeListener);
	}

	return underlyingMaps.add(map);
    }

    /**
     * Adds {@code map} and associate it with a weight.<br>
     * <b>NOTE:</b> Each instance of a map can only be associated with one
     * weight. If an instances is added more than once, all instances will be
     * associated with the weight given last.
     * 
     * @see #addMap(PathfindingMap)
     * @param map
     * @param weight
     * @return <code>true</code> if the map was added
     */
    public boolean addMap(T map, double weight) {
	// need to set weight before adding which triggers update
	weights.put(map, weight);
	if (addMap(map)) {
	    return true;
	}
	// could not add map, remove weight again
	weights.remove(map);
	return false;
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
    public boolean removeMap(Object map) {
	if (map instanceof MapChangeNotifier) {
	    ((MapChangeNotifier) map).removeListener(myChangeListener);
	}

	if (underlyingMaps.remove(map)) {
	    weights.remove(map);
	    forceUpdateAll();
	    return true;
	}
	return false;
    }

    /**
     * Re-associates a map with a weight.
     * 
     * @param map
     * @param weight
     * @return weight that was associated with the map before
     */
    public final double setWeight(T map, double weight) {
	Double oldWeight = weights.put(map, weight);
	forceUpdateAll();

	if (oldWeight != null) {
	    return oldWeight;
	}
	return NEUTRAL_WEIGHT;
    }

    /**
     * Obtains weight associated with map. If there is no weight associated a
     * neutral factor is returned.
     * 
     * @param map
     * @return weight factor for {@code map}
     */
    protected double getWeight(T map) {
	Double weight = weights.get(map);
	if (weight != null) {
	    return weight;
	}
	return NEUTRAL_WEIGHT;
    }

    /**
     * Read-only accessor to underlying maps for deriving directions.
     *
     * @return pathfinding maps
     */
    protected final Collection<T> getUnderlyingMaps() {
	return underlyingMapsReadOnly;
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

    /**
     * Obtains flow direction for given location after updating updating if
     * needed.
     */
    @Override
    public final Double2D obtainDirection(int x, int y) {
	updateIfDirty(x, y);
	return (Double2D) getMapGrid().get(x, y);
    }

    /**
     * Returns the field portrayable.<br>
     * <b>NOTE:</b> This displays the field as is, including not-updated dirty
     * locations. To ensure the correct state is drawn, call
     * {@link #updateIfDirtyAll()} before.
     */
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