package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.NEUTRAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import sim.display.GUIState;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.CombinedInspector;
import sim.portrayal.inspector.FlowMapInspector;
import sim.portrayal.inspector.ProvidesInspector;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.util.Double2D;

/**
 * This class provides a skeletal implementation for a flow map that is derived
 * from other underlying pathfinding maps. Changes in an underlying map are
 * propagated automatically if it implements the {@link MapChangeNotifier}
 * interface.
 * <p>
 * Each map is associated with a name and can be accessed via
 * {@link #getUnderlyingMap(String)}. If no name is specified the map's string
 * representation will be used along with its hash code.
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
public abstract class DerivedFlowMap<T extends PathfindingMap> extends AbstractDynamicMap
	implements GridBackedFlowMap, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    /** Neutral weight factor. */
    static final double NEUTRAL_WEIGHT = 1d;

    /** Grid containing a flow direction for every location. */
    private final ObjectGrid2D flowMapGrid;
    /** Pathfinding maps to derive flow directions from. */
    private final Map<String, T> underlyingMaps = new HashMap<>();
    /** {@code Map} pointing from pathfinding map to the objects wrapping it. */
    private final Map<T, Double> weights = new HashMap<>();

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
	flowMapGrid = new ObjectGrid2D(width, height, NEUTRAL);
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
     * all locations which is expansive. Use
     * {@link #changeStructure(MapChanger)} to chain several structural changes
     * and trigger the update only once.
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
     * all locations which is expansive. Use
     * {@link #changeStructure(MapChanger)} to chain several structural changes
     * and trigger the update only once.
     * <p>
     * <b>NOTE:</b> Each instance of a map can only be associated with one
     * weight. If an instance is added more than once, all instances will be
     * associated with the weight given last.
     * 
     * @param map
     * @param weight
     * @return the name which the map was associated with
     */
    public String addMap(T map, double weight) {
	// need to set weight before adding which triggers update
	weights.put(map, weight);
	String name = addMapInternal(map);
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
     * @return the name which the map was associated with
     */
    final String addMapInternal(T map) {
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
	return name;
    }

    /**
     * Removes an underlying pathfinding map. If it is a
     * {@link MapChangeNotifier} the change listener that was added before is
     * also removed.
     * <p>
     * <b>NOTE:</b> This is a structural change and triggers a forced update of
     * all locations which is expansive. Use
     * {@link #changeStructure(MapChanger)} to chain several structural changes
     * and trigger the update only once.
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
     * all locations which is expansive. Use
     * {@link #changeStructure(MapChanger)} to chain several structural changes
     * and trigger the update only once.
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

    /**
     * Removes all underlying maps.
     * 
     * @return removed maps
     */
    public Collection<T> clear() {
	Collection<T> toRemove = new ArrayList<>(underlyingMaps.values());

	for (Iterator<T> iterator = toRemove.iterator(); iterator.hasNext();) {
	    T map = iterator.next();
	    if (!removeMapInternal(map)) {
		iterator.remove();
	    }
	}
	forceUpdateAll();
	return toRemove;
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
     * @param weight
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
     * @return weight that was associated with the map before
     */
    public final double setWeight(String name, double weight) {
	if (underlyingMaps.containsKey(name)) {
	    return setWeight(underlyingMaps.get(name), weight);
	}
	throw new IllegalArgumentException(name + " is not associated with an underlying map.");
    }

    /**
     * Makes several structural changes with a {@link MapChanger} and trigger the
     * expensive update only once.
     * 
     * @param changer
     * @return a {@link Map} with each added pathfinding map linked to its name
     *         it was associated with
     */
    public Map<T, String> changeStructure(MapChanger<T> changer) {
	Map<T, String> names = new HashMap<>();
	for (T map : changer.mapsToAdd) {
	    names.put(map, addMapInternal(map));
	}
	for (T map : changer.mapsToRemove) {
	    removeMapInternal(map);
	}
	weights.putAll(changer.weightsToPut);
	forceUpdateAll();

	return Collections.unmodifiableMap(names);
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
	for (T map : underlyingMaps.values()) {
	    if (map instanceof DynamicMap) {
		((DynamicMap) map).updateIfDirty(x, y);
	    }
	}
	super.updateIfDirty(x, y);
    }

    @Override
    protected void update(int x, int y) {
	getMapGrid().set(x, y, computeDirection(x, y));
    }

    /**
     * Obtains flow direction for given location after updating updating if
     * needed.
     */
    @Override
    public final Double2D obtainDirection(int x, int y) {
	updateIfDirty(x, y);
	return (Double2D) getMapGrid().get(x, y);
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new CombinedInspector(new FlowMapInspector(state, this),
		Inspector.getInspector(underlyingMaps, state, name));
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
	return getClass().getSimpleName() + underlyingMaps.keySet();
    }
}