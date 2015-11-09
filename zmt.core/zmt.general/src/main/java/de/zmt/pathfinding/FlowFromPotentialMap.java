package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.DIRECTION_NEUTRAL;

import java.util.logging.Logger;

import de.zmt.util.Grid2DUtil.LocationsResult;
import sim.field.grid.*;
import sim.util.*;

/**
 * A flow map deriving directions from an underlying potential map with each
 * location pointing towards the adjacent location with the highest potential.
 * If a location has no neighbor with a higher potential, it will contain the
 * zero vector.
 *
 * @author mey
 *
 */
public class FlowFromPotentialMap extends DerivedFlowMap<PotentialMap> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FlowFromPotentialMap.class.getName());

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

    private final DoubleBag valuesCache = new DoubleBag(RESULTS_SIZE_LOOKUP_DIST);

    /**
     * Constructs a new {@code FlowFromPotentialMap} from given
     * {@code integralMap}.
     * 
     * @param potentialMap
     *            {@link PotentialMap} to derive directions from.
     */
    public FlowFromPotentialMap(PotentialMap potentialMap) {
	super(potentialMap);

	// initialize an empty abstract grid having same dimensions
	lookupGrid = new AbstractGrid2D() {
	    private static final long serialVersionUID = 1L;

	    {
		this.width = FlowFromPotentialMap.this.getWidth();
		this.height = FlowFromPotentialMap.this.getHeight();
	    }
	};
	forceUpdateAll();
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
	int highestIndex = findIndexOfBestLocation(locations, collectPotentialValues(locations), originIndex);

	// in case the current position is the best: return neutral direction
	if (highestIndex == originIndex) {
	    return DIRECTION_NEUTRAL;
	}
	// otherwise return direction to position with highest value
	else {
	    // TODO only 8 possible values, optimize with constants
	    Int2D bestPosition = new Int2D(locations.xPos.get(highestIndex), locations.yPos.get(highestIndex));
	    return new Double2D(bestPosition.subtract(x, y)).normalize();
	}
    }

    /**
     * Collect potential values from integral potential map at given
     * {@code locations}.
     * 
     * @param locations
     *            locations from neighborhood lookup
     * @return bag containing value at each location from integral potential map
     */
    private DoubleBag collectPotentialValues(LocationsResult locations) {
	valuesCache.clear();

	for (int i = 0; i < locations.size(); i++) {
	    double currentPotential = getIntegralMap().obtainPotential(locations.xPos.get(i), locations.yPos.get(i));
	    valuesCache.add(currentPotential);
	}

	return valuesCache;
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
     * Returns the index of the location with the highest potential. If the
     * origin is one of the best locations it is always returned, e.g. if all
     * locations yield the same results. This is to prevent giving a biased
     * direction if potentials are constant.
     * 
     * @param locations
     *            locations from neighborhood lookup
     * @param potentialValues
     *            potentials on {@code locations} from all integral map
     * @param originIndex
     *            index of origin in {@code locations}
     * @return the index of the location with the highest overall potential
     */
    private static int findIndexOfBestLocation(LocationsResult locations, DoubleBag potentialValues, int originIndex) {
	// best location defaults to origin
	int bestLocationIndex = originIndex;

	for (int i = 0; i < locations.size(); i++) {
	    double value = potentialValues.get(i);
	    // if value is higher than highest found before
	    if (value > potentialValues.get(bestLocationIndex)) {
		// mark current value as the highest
		bestLocationIndex = i;
	    }
	}
	return bestLocationIndex;
    }

}
