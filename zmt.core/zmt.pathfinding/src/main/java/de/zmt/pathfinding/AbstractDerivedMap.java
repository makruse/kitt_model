package de.zmt.pathfinding;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides a skeletal implementation for a {@link DerivedMap}.
 * Changes in an underlying map are propagated automatically if it implements
 * the {@link MapChangeNotifier} interface.
 * <p>
 * Each map is associated with a name and can be accessed via
 * {@link #getUnderlyingMap(String)}. If no name is specified the map's string
 * representation will be used along with its hash code.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of underlying pathfinding maps
 */
abstract class AbstractDerivedMap<T extends PathfindingMap> extends AbstractDynamicMap implements DerivedMap<T> {
    private static final long serialVersionUID = 1L;

    /** Neutral weight factor. */
    static final double NEUTRAL_WEIGHT = 1d;

    /** Pathfinding maps to derive flow directions from. */
    private final Map<String, T> underlyingMaps = new HashMap<>();
    /** {@code Map} pointing from pathfinding map to the objects wrapping it. */
    private final Map<T, Double> weights = new HashMap<>();

    /**
     * Constructs an empty {@code DerivedFlowMap} with given dimensions.
     * 
     * @param width
     *            width of map
     * @param height
     *            height of map
     */
    public AbstractDerivedMap(int width, int height) {
	super(width, height);
    }

    /**
     * Adds a map to derive directions from. If the added map is a
     * {@link MapChangeNotifier}, a listener is added to the map so that changes
     * will trigger an update for affected locations. To clear the listener
     * reference, {@link #removeMap(Object)} has to be called.
     * <p>
     * Dimensions for added maps must match those of this map.
     * <p>
     * <b>NOTE:</b> This is a structural change and triggers a forced update of
     * all locations which is expansive. Use {@link #applyChanges(Changes)} to
     * chain several structural changes and trigger the update only once.
     * 
     * @param map
     *            map to add
     * @return the name which the map was associated with
     */
    public String addMap(T map) {
	return addMap(map, NEUTRAL_WEIGHT);
    }

    /**
     * Adds {@code map} and associate it with a weight.
     * <p>
     * <b>NOTE:</b> This is a structural change and triggers a forced update of
     * all locations which is expansive. Use {@link #applyChanges(Changes)} to
     * chain several structural changes and trigger the update only once.
     * <p>
     * <b>NOTE:</b> Each instance of a map can only be associated with one
     * weight. If an instance is added more than once, all instances will be
     * associated with the weight given last.
     * 
     * @param map
     *            the map to add
     * @param weight
     *            the weight to associate the map with
     * @return the name which the map was associated with
     */
    public String addMap(T map, double weight) {
	String name = addMapInternal(map, weight);
	forceUpdateAll();
	return name;
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
     * @param weight
     *            the weight to associate the map with
     * @return the name which the map was associated with
     */
    final String addMapInternal(T map, double weight) {
	if (map.getWidth() != getWidth() || map.getHeight() != getHeight()) {
	    throw new IllegalArgumentException("Expected: is <" + getWidth() + ", " + getHeight() + ">\n" + "but: was <"
		    + map.getWidth() + ", " + map.getHeight() + ">");
	}
	if (map == this) {
	    throw new IllegalArgumentException("Cannot add itself as an underlying map.");
	}
	if (map instanceof MapChangeNotifier) {
	    ((MapChangeNotifier) map).addListener(this);
	}

	String name;
	if (map instanceof NamedMap) {
	    name = ((NamedMap) map).getName();
	}
	// if map is not named: use simple class name and hash code
	else {
	    name = map.getClass().getSimpleName() + "@" + Integer.toHexString(map.hashCode());
	}

	underlyingMaps.put(name, map);
	weights.put(map, weight);
	return name;
    }

    /**
     * Removes an underlying pathfinding map. If it is a
     * {@link MapChangeNotifier} the change listener that was added before is
     * also removed.
     * <p>
     * <b>NOTE:</b> This is a structural change and triggers a forced update of
     * all locations which is expansive. Use {@link #applyChanges(Changes)} to
     * chain several structural changes and trigger the update only once.
     * 
     * @param name
     *            the name associated with the map to be removed
     * @return the removed map or <code>null</code> if the map could not be
     *         removed
     */
    public T removeMap(String name) {
	T map = underlyingMaps.get(name);
	if (removeMap(map)) {
	    return map;
	}
	return null;
    }

    /**
     * Removes an underlying pathfinding map. If it is a
     * {@link MapChangeNotifier} the change listener that was added before is
     * also removed.
     * <p>
     * <b>NOTE:</b> This is a structural change and triggers a forced update of
     * all locations which is expansive. Use {@link #applyChanges(Changes)} to
     * chain several structural changes and trigger the update only once.
     * 
     * @param map
     *            the map to remove
     * @return <code>true</code> if the map could be removed
     */
    public boolean removeMap(Object map) {
	if (removeMapInternal(map)) {
	    forceUpdateAll();
	    return true;
	}
	return false;
    }

    /**
     * Removes an underlying pathfinding map. If it is a
     * {@link MapChangeNotifier} the change listener that was added before is
     * also removed.
     * 
     * @param map
     *            the map to remove
     * @return <code>true</code> if the map could be removed
     */
    private boolean removeMapInternal(Object map) {
	if (underlyingMaps.values().remove(map)) {
	    if (map instanceof MapChangeNotifier) {
		((MapChangeNotifier) map).removeListener(this);
	    }
	    weights.remove(map);
	    return true;
	}
	return false;
    }

    /** Removes all underlying maps. */
    public void clear() {
	for (Iterator<T> iterator = underlyingMaps.values().iterator(); iterator.hasNext();) {
	    T map = iterator.next();
	    if (removeMapInternal(map)) {
		iterator.remove();
	    }
	}
	forceUpdateAll();
    }

    /**
     * Returns an underlying map by its name.
     * 
     * @param name
     * @return the underlying map with the given name
     */
    public T getUnderlyingMap(String name) {
	return underlyingMaps.get(name);
    }

    /**
     * Re-associates a map with a weight.
     * 
     * @param map
     *            the map to associate the weight to
     * @param weight
     *            the weight to associate the map with
     * @return weight that was associated with the map before
     */
    public final double setWeight(T map, double weight) {
	Double oldWeight = weights.put(map, weight);
	forceUpdateAll();

	return oldWeight;
    }

    /**
     * Re-associates a map with a weight.
     * 
     * @param name
     *            the name of the map
     * @param weight
     *            the weight to associate the map with
     * @return weight that was associated with the map before
     */
    public final double setWeight(String name, double weight) {
	if (underlyingMaps.containsKey(name)) {
	    return setWeight(underlyingMaps.get(name), weight);
	}
	throw new IllegalArgumentException(name + " is not associated with an underlying map.");
    }

    @Override
    public Map<T, String> applyChanges(Changes<T> changes) {
	if (getWidth() != changes.getWidth() || getHeight() != changes.getHeight()) {
	    throw new IllegalArgumentException(
		    "Dimensions from " + changes + " must match " + getWidth() + ", " + getHeight() + ".");
	}

	Map<T, String> names = new HashMap<>();
	for (T map : changes.getMapsToAdd()) {
	    names.put(map, addMapInternal(map, NEUTRAL_WEIGHT));
	}
	for (T map : changes.getMapsToRemove()) {
	    removeMapInternal(map);
	}
	weights.putAll(changes.getWeightsToPut());
	forceUpdateAll();

	return Collections.unmodifiableMap(names);
    }

    @Override
    public Changes<T> content() {
	return new Changes<>(getWidth(), getHeight(), underlyingMaps.values(), weights, Collections.<T> emptyList());
    }

    /**
     * Obtains weight associated with map. If there is no weight associated a
     * neutral factor is returned.
     * 
     * @param map
     * @return weight factor for {@code map}
     */
    protected double getWeight(T map) {
	return weights.get(map);
    }

    /**
     * Read-only accessor to underlying maps for deriving directions.
     *
     * @return pathfinding maps
     */
    protected final Collection<T> getUnderlyingMaps() {
	return underlyingMaps.values();
    }

    @Override
    public void updateIfDirty(int x, int y) {
	// update underlying maps before updating itself
	for (T map : underlyingMaps.values()) {
	    if (map instanceof DynamicMap) {
		((DynamicMap) map).updateIfDirty(x, y);
	    }
	}
	super.updateIfDirty(x, y);
    }

    @Override
    public String toString() {
	return super.toString() + underlyingMaps.keySet();
    }
}