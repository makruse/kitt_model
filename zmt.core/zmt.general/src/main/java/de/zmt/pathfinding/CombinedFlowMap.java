package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.DIRECTION_NEUTRAL;

import java.util.*;

import sim.util.Double2D;

/**
 * A flow map that returns directions from combining other flow maps. This is
 * done by adding and normalizing the directions at every location. A weight
 * factor is associated with every flow map, which defines the influence each
 * map has on the final result.
 * <p>
 * {@link PotentialMap}s can also be added when wrapped into a
 * {@link FlowFromPotentialMap}.
 * 
 * @author mey
 */
public class CombinedFlowMap extends ListeningFlowMap implements FlowMap {
    private static final long serialVersionUID = 1L;

    /** Neutral weight factor. */
    public static final double NEUTRAL_WEIGHT = 1d;

    /** Flow maps to derive flow directions from. */
    private final Collection<FlowMap> integralMaps = new ArrayList<>();
    /** {@code Map} pointing from pathfinding map to the objects wrapping it. */
    private final Map<FlowMap, Double> weights = new HashMap<>();

    public CombinedFlowMap(int width, int height) {
	super(width, height);
    }

    /**
     * Adds a {@link FlowMap} to derive directions from. If it is a
     * {@link MapChangeNotifier} a listener is added so that changes will
     * trigger updating directions on affected locations. Dimensions for added
     * maps must match those of this map.
     * <p>
     * A forced update of all directions is triggered after add.
     * 
     * @param map
     *            map to add
     * @return {@code true} if the map was added
     */
    public boolean addMap(FlowMap map) {
	if (map.getWidth() != getWidth() || map.getHeight() != getHeight()) {
	    throw new IllegalArgumentException("Expected: is <" + getWidth() + ", " + getHeight() + ">\n" + "but: was <"
		    + map.getWidth() + ", " + map.getHeight() + ">");
	}
	if (map instanceof MapChangeNotifier) {
	    ((MapChangeNotifier) map).addListener(getMyChangeListener());
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
     * @see #addMap(FlowMap)
     * @param map
     * @param weight
     * @return <code>true</code> if the map was added
     */
    public boolean addMap(FlowMap map, double weight) {
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
	    ((MapChangeNotifier) map).removeListener(getMyChangeListener());
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
    public final double setWeight(FlowMap map, double weight) {
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
    final Collection<FlowMap> getIntegralMaps() {
	return Collections.unmodifiableCollection(integralMaps);
    }

    /**
     * Obtains weight associated with map. If there is no weight associated a
     * neutral factor is returned.
     * 
     * @param map
     * @return weight factor for {@code map}
     */
    double obtainWeight(FlowMap map) {
	Double weight = weights.get(map);
	if (weight != null) {
	    return weight;
	}
	return NEUTRAL_WEIGHT;
    }

    /**
     * Accumulates weighted directions from underlying maps.
     */
    @Override
    protected Double2D computeDirection(int x, int y) {
	if (integralMaps.isEmpty()) {
	    return DIRECTION_NEUTRAL;
	}

	Double2D directionsSum = DIRECTION_NEUTRAL;
	for (FlowMap map : integralMaps) {
	    double weight = obtainWeight(map);
	    Double2D weightedDirection = map.obtainDirection(x, y).multiply(weight);
	    directionsSum = directionsSum.add(weightedDirection);
	}

	// check needed here: normalizing (0,0) will throw an exception
	if (directionsSum.equals(DIRECTION_NEUTRAL)) {
	    return DIRECTION_NEUTRAL;
	} else {
	    return directionsSum.normalize();
	}
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + integralMaps;
    }
}
