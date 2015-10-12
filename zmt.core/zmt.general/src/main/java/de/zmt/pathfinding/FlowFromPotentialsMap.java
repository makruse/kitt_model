package de.zmt.pathfinding;

import java.util.*;
import java.util.logging.Logger;

import de.zmt.util.Grid2DUtil.LocationsResult;
import sim.field.grid.*;
import sim.util.*;

/**
 * A flow map deriving directions from underlying potential maps with each
 * direction pointing towards the highest adjacent direction. If there is no
 * underlying potentials available or it is constant, this map will return a
 * zero vector.
 * <p>
 * Changes from underlying maps are propagated to this map if their type is
 * {@link DynamicMap}.
 * 
 * @author mey
 *
 */
public class FlowFromPotentialsMap implements FlowMap, DynamicMap {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FlowFromPotentialsMap.class.getName());

    /** Distance in neighborhood lookups. */
    private static final int POTENTIALS_LOOKUP_DIST = 1;
    /**
     * Size of result when retrieving neighbors with distance
     * {@value #POTENTIALS_LOOKUP_DIST}.
     */
    private static final int RESULTS_SIZE_LOOKUP_DIST = (1 + 2 * POTENTIALS_LOOKUP_DIST)
	    * (1 + 2 * POTENTIALS_LOOKUP_DIST);

    /** potential maps to derive flow directions from. */
    private final Collection<PotentialMap> potentialMaps;
    /** Directions towards highest adjacent potential. */
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

    private final LocationsResult locationsCache = new LocationsResult(new IntBag(RESULTS_SIZE_LOOKUP_DIST),
	    new IntBag(RESULTS_SIZE_LOOKUP_DIST));
    private final Queue<DoubleBag> valuesCaches = new ArrayDeque<>(
	    Arrays.asList(new DoubleBag(RESULTS_SIZE_LOOKUP_DIST), new DoubleBag(RESULTS_SIZE_LOOKUP_DIST)));

    /**
     * Constructs an empty map with given dimensions. All locations are
     * initialized to zero vectors.
     * 
     * @param width
     * @param height
     */
    public FlowFromPotentialsMap(int width, int height) {
	super();
	this.potentialMaps = new EqualDimensionsMaps<>(new ArrayList<PotentialMap>(), width, height);
	this.flowMapGrid = new ObjectGrid2D(width, height);
	updatingMap = new LazyUpdatingMap(width, height) {

	    @Override
	    protected void update(int x, int y) {
		flowMapGrid.set(x, y, computeDirection(x, y));
	    }
	};
	updatingMap.forceUpdateAll();
    }

    /**
     * Adds a potential map to derive directions from. If it is a
     * {@link DynamicMap} a listener is added so that changes will trigger
     * updating directions on affected locations. Dimensions for added maps must
     * match those of this map.
     * <p>
     * A forced update of all directions is triggered after add.
     * 
     * @param potentialMap
     *            map to add
     * @return {@code true} if the map was added
     */
    public boolean addMap(PotentialMap potentialMap) {
	if (potentialMap instanceof DynamicMap) {
	    ((DynamicMap) potentialMap).addListener(myChangeListener);
	}
	if (potentialMaps.add(potentialMap)) {
	    updatingMap.forceUpdateAll();
	    return true;
	}
	return false;
    }

    /**
     * Removes an underlying potential map. If it is a {@link DynamicMap} the
     * change listener that was added before is also removed.
     * <p>
     * A forced update of all directions is triggered after removal.
     * 
     * @param potentialsMap
     * @return {@code false} was not added before
     */
    public boolean removeMap(Object potentialsMap) {
	if (potentialsMap instanceof DynamicMap) {
	    ((DynamicMap) potentialsMap).removeListener(myChangeListener);
	}

	if (potentialMaps.remove(potentialsMap)) {
	    updatingMap.forceUpdateAll();
	    return true;
	}
	return false;
    }

    @Override
    public Double2D obtainDirection(int x, int y) {
	updatingMap.updateIfDirty(x, y);
	return (Double2D) flowMapGrid.get(x, y);
    }

    /**
     * Returns a direction vector towards the neighbor cell with the highest
     * overall potential. The overall potential is the sum of all added
     * potential maps for this location. If there are no underlying potential
     * maps or the values are constant, a zero vector will be returned.
     * 
     * @param x
     *            x-coordinate of location
     * @param y
     *            y-coordinate of location
     * @return direction vector pointing to the neighbor location with the
     *         highest overall potential
     */
    Double2D computeDirection(int x, int y) {
	if (potentialMaps.isEmpty()) {
	    return DirectionConstants.DIRECTION_NEUTRAL;
	}

	flowMapGrid.getMooreLocations(x, y, POTENTIALS_LOOKUP_DIST, Grid2D.BOUNDED, true, locationsCache.xPos,
		locationsCache.yPos);
	// point to newly obtained neighbor locations
	LocationsResult locations = locationsCache;

	int originIndex = findLocationIndex(locations, x, y);
	int highestIndex = findIndexOfBestLocation(locations, originIndex);

	// in case the current position is the best: return neutral direction
	if (highestIndex == originIndex) {
	    return DirectionConstants.DIRECTION_NEUTRAL;
	} else {
	    // otherwise return direction to position with highest value
	    Int2D bestPosition = new Int2D(locations.xPos.get(highestIndex), locations.yPos.get(highestIndex));
	    return new Double2D(bestPosition.subtract(x, y)).normalize();
	}
    }

    /**
     * Returns the index of the location with the highest overall potential. If
     * the origin is one of the best locations it is always returned, e.g. if
     * all locations yield the same results. This is to prevent giving a biased
     * direction if potentials are constant.
     * 
     * @param locations
     *            locations from neighborhood lookup
     * @param originIndex
     *            index of origin in {@code locations}
     * @return the index of the location with the highest overall potential
     */
    private int findIndexOfBestLocation(LocationsResult locations, int originIndex) {
	DoubleBag potentialSums = computePotentialSums(locations);

	// best location defaults to origin
	// this is important to prevent
	int bestLocationIndex = originIndex;

	for (int i = 0; i < locations.size(); i++) {
	    double value = potentialSums.get(i);
	    // if value is higher than highest found before
	    if (value > potentialSums.get(bestLocationIndex)) {
		// mark current value as the highest
		bestLocationIndex = i;
	    }
	}
	return bestLocationIndex;
    }

    /**
     * Sums up all potentials from each map at given {@code locations}.
     * 
     * @param locations
     *            locations from neighborhood lookup
     * @return bag containing sum at each location from all potential maps
     */
    private DoubleBag computePotentialSums(LocationsResult locations) {
	DoubleBag previousCache = null;
	for (PotentialMap map : potentialMaps) {
	    DoubleBag cache = valuesCaches.poll();
	    cache.clear();

	    // no previous results (first iteration): fill cache from map
	    if (previousCache == null) {
		for (int i = 0; i < locations.size(); i++) {
		    cache.add(map.obtainPotential(locations.xPos.get(i), locations.yPos.get(i)));
		}
	    }
	    // previous results present: save potentials sum
	    else {
		for (int i = 0; i < locations.size(); i++) {
		    double previousPotential = previousCache.get(i);
		    double currentPotential = map.obtainPotential(locations.xPos.get(i), locations.yPos.get(i));
		    cache.add(previousPotential + currentPotential);
		}
	    }

	    previousCache = cache;
	    valuesCaches.offer(cache);
	}

	// last cache containing sum values of all maps
	return previousCache;
    }

    /**
     * Find the index of given location within a {@link LocationsResult}.
     * 
     * @param locations
     * @param x
     *            x-coordinate to look for
     * @param y
     *            y-coordinate to look for
     * @return index of {@code (x, y)} in {@code locations}
     */
    private static int findLocationIndex(LocationsResult locations, int x, int y) {
	for (int i = 0; i < locations.size(); i++) {
	    if (locations.xPos.get(i) == x && locations.yPos.get(i) == y) {
		return i;
	    }
	}
	throw new IllegalArgumentException("(" + x + ", " + y + ") was not found in " + locations + "!");
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

}
