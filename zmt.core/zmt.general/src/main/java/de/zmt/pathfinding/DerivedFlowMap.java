package de.zmt.pathfinding;

import java.io.Serializable;
import java.util.*;

import sim.field.grid.ObjectGrid2D;
import sim.portrayal.portrayable.*;
import sim.util.Double2D;

/**
 * This class provides a skeletal implementation for a flow map that is derived
 * from other underlying pathfinding maps. If an underlying map changes and
 * these changes should be reflected, it must implement the
 * {@link MapChangeNotifier} interface. Weights can be associated with
 * underlying maps to control its impact in the final result.
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
abstract class DerivedFlowMap<T extends PathfindingMap> extends LazyUpdatingMap
	implements FlowMap, ProvidesPortrayable<FieldPortrayable<ObjectGrid2D>>, Serializable {
    private static final long serialVersionUID = 1L;

    public static final double NEUTRAL_WEIGHT = 1d;

    /** Pathfinding maps to derive flow directions from. */
    private final Collection<T> integralMaps = new ArrayList<>();
    /** Cached direction vectors. */
    private final ObjectGrid2D flowMapGrid;
    /** Added to underlying maps to be notified of changes. */
    private final MapChangeListener myChangeListener = new MapChangeListener() {
	private static final long serialVersionUID = 1L;

	@Override
	public void changed(int x, int y) {
	    markDirty(x, y);
	}
    };

    /** {@code Map} pointing from pathfinding map to the objects wrapping it. */
    protected final Map<T, Double> weights = new HashMap<>();

    /**
     * Constructs a new DerivedFlowMap with given dimensions.
     * 
     * @param width
     * @param height
     */
    public DerivedFlowMap(int width, int height) {
	super(width, height);
	flowMapGrid = new ObjectGrid2D(width, height);
	forceUpdateAll();
    }

    /**
     * Adds a pathfinding map to derive directions from. If it is a
     * {@link MapChangeNotifier} a listener is added so that changes will trigger
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
	if (map.getWidth() != getWidth() || map.getHeight() != getHeight()) {
	    throw new IllegalArgumentException("Expected: is <" + getWidth() + ", " + getHeight() + ">\n" + "but: was <"
		    + map.getWidth() + ", " + map.getHeight() + ">");
	}
	if (map instanceof MapChangeNotifier) {
	    ((MapChangeNotifier) map).addListener(myChangeListener);
	}
	if (integralMaps.add(map)) {
	    forceUpdateAll();
	    return true;
	}
	return false;
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
        if (this.addMap(map)) {
            return true;
        }
        // could not add map, remove weight again
        weights.remove(map);
        return false;
    }

    /**
     * Removes an underlying pathfinding map. If it is a
     * {@link MapChangeNotifier} the change listener that was added before is
     * also removed.
     * <p>
     * A forced update of all directions is triggered after removal.
     * 
     * @param map
     * @return {@code false} if map could not be removed
     */
    public boolean removeMap(Object map) {
	if (map instanceof MapChangeNotifier) {
	    ((MapChangeNotifier) map).removeListener(myChangeListener);
	}

	if (integralMaps.remove(map)) {
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
     * Read-only accessor to integral maps for deriving directions.
     * 
     * @return integral maps
     */
    protected final Collection<T> getIntegralMaps() {
	return Collections.unmodifiableCollection(integralMaps);
    }

    /**
     * Obtains weight associated with map. If there is no weight associated a
     * neutral factor is returned.
     * 
     * @param map
     * @return weight factor for {@code map}
     */
    protected final double obtainWeight(T map) {
        Double weight = weights.get(map);
        if (weight != null) {
            return weight;
        }
        return NEUTRAL_WEIGHT;
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

    @Override
    protected final void update(int x, int y) {
	flowMapGrid.set(x, y, computeDirection(x, y));
    }

    @Override
    public Double2D obtainDirection(int x, int y) {
	for (T map : integralMaps) {
	    if (map instanceof MapUpdateHandler) {
		((MapUpdateHandler) map).updateIfDirty(x, y);
	    }
	}
	updateIfDirty(x, y);
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
    public FieldPortrayable<ObjectGrid2D> providePortrayable() {
	return new FieldPortrayable<ObjectGrid2D>() {

	    @Override
	    public ObjectGrid2D getField() {
		return flowMapGrid;
	    }
	};
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[integralMaps=" + integralMaps + "]";
    }

}