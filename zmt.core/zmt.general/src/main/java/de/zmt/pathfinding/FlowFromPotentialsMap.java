package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.*;

import java.util.*;
import java.util.logging.Logger;

import de.zmt.util.Grid2DUtil.LocationsResult;
import sim.field.grid.*;
import sim.util.*;

/**
 * A flow map deriving directions from underlying potential maps with each
 * location pointing towards the adjacent location with the highest potential
 * added together. If a location has no neighbor with a higher potential, it
 * will contain the zero vector.
 *
 * @author mey
 *
 */
public class FlowFromPotentialsMap extends DerivedFlowMap<PotentialMap> {
    private static final long serialVersionUID = 1L;

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

    /** An empty grid to access Moore locations lookup method. */
    private final Grid2D lookupGrid;
    private final LocationsResult locationsCache = new LocationsResult(new IntBag(RESULTS_SIZE_LOOKUP_DIST),
	    new IntBag(RESULTS_SIZE_LOOKUP_DIST));
    private final Queue<DoubleBag> valuesCaches = new ArrayDeque<>(
	    Arrays.asList(new DoubleBag(RESULTS_SIZE_LOOKUP_DIST), new DoubleBag(RESULTS_SIZE_LOOKUP_DIST)));

    /**
     * Constructs a new {@code FlowFromPotentialsMap} with given dimensions.
     * 
     * @param width
     *            width of map
     * @param height
     *            height of map
     */
    public FlowFromPotentialsMap(int width, int height) {
	super(width, height);
	// initialize an empty abstract grid having same dimensions
	lookupGrid = new AbstractGrid2D() {
	    private static final long serialVersionUID = 1L;

	    {
		this.width = FlowFromPotentialsMap.this.getWidth();
		this.height = FlowFromPotentialsMap.this.getHeight();
	    }
	};
    }

    /**
     * Constructs a new {@code FlowFromPotentialMap} with given potential map as
     * its first underlying map.
     * 
     * @param underlyingMap
     *            {@link PotentialMap} to derive directions from.
     */
    public FlowFromPotentialsMap(PotentialMap underlyingMap) {
	this(underlyingMap.getWidth(), underlyingMap.getHeight());
	addMap(underlyingMap);
    }

    /**
     * Returns a direction vector towards the neighbor cell with the highest
     * potential.
     * 
     * @param x
     *            x-coordinate of location
     * @param y
     *            y-coordinate of location
     * @return direction vector pointing to the neighbor location with the
     *         highest potential
     */
    @Override
    protected Double2D computeDirection(int x, int y) {
	lookupGrid.getMooreLocations(x, y, POTENTIALS_LOOKUP_DIST, Grid2D.BOUNDED, true, locationsCache.xPos,
		locationsCache.yPos);
	// point to newly obtained neighbor locations
	LocationsResult locations = locationsCache;

	int originIndex = findLocationIndex(locations, x, y);
	int bestLocationIndex = findIndexOfBestLocation(locations, collectPotentialSums(locations), originIndex);

	int bestX = locations.xPos.get(bestLocationIndex);
	int bestY = locations.yPos.get(bestLocationIndex);
	return obtainDirectionConstant(x, y, bestX, bestY);
    }

    /**
     * Collect sums of potential values from underlying potential maps at given
     * {@code locations}.
     * 
     * @param locations
     *            locations from neighborhood lookup
     * @return bag containing the potential sum for each location from
     *         underlying potential maps
     */
    private DoubleBag collectPotentialSums(LocationsResult locations) {
	DoubleBag previousCache = null;
	for (PotentialMap map : getUnderlyingMaps()) {
	    DoubleBag cache = valuesCaches.poll();
	    cache.clear();

	    for (int i = 0; i < locations.size(); i++) {
		double currentPotential = map.obtainPotential(locations.xPos.get(i), locations.yPos.get(i));
		double weightedCurrentPotential = currentPotential * getWeight(map);

		if (previousCache == null) {
		    cache.add(weightedCurrentPotential);
		}
		// add to previously cached value if present
		else {
		    cache.add(previousCache.get(i) + weightedCurrentPotential);
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

    /**
     * Returns the index of the location with the highest overall potential. If
     * the origin is one of the best locations it is always returned, e.g. if
     * all locations yield the same results. This is to prevent giving a biased
     * direction if potentials are constant.
     * 
     * @param locations
     *            locations from neighborhood lookup
     * @param potentialSums
     *            potential sum for every location
     * @param originIndex
     *            index of origin in {@code locations}
     * @return the index of the location with the highest overall potential
     */
    private static int findIndexOfBestLocation(LocationsResult locations, DoubleBag potentialSums, int originIndex) {
	// best location defaults to origin
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
     * Obtains direction constant pointing from current location to best
     * location.
     * 
     * @param x
     *            x-coordinate of current location
     * @param y
     *            y-coordinate of current location
     * @param bestX
     *            x-coordinate of best location
     * @param bestY
     *            y-coordinate of best location
     * @return direction from current location to best location
     */
    private static Double2D obtainDirectionConstant(int x, int y, int bestX, int bestY) {
	if (bestX < x) {
	    if (bestY < y) {
		return DIRECTION_NORTHWEST;
	    }
	    if (bestY > y) {
		return DIRECTION_SOUTHWEST;
	    }
	    return DIRECTION_WEST;
	}
	if (bestX > x) {
	    if (bestY < y) {
		return DIRECTION_NORTHEAST;
	    }
	    if (bestY > y) {
		return DIRECTION_SOUTHEAST;
	    }
	    return DIRECTION_EAST;
	}
	assert bestX == x;
	if (bestY < y) {
	    return DIRECTION_NORTH;
	}
	if (bestY > y) {
	    return DIRECTION_SOUTH;
	}
	assert bestX == y;
	return DIRECTION_NEUTRAL;
    }
}
