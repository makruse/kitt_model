package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.*;

import java.util.logging.Logger;

import sim.util.Double2D;

/**
 * A flow map deriving directions from underlying potential maps with each
 * returned direction vector pointing towards the neighbor with the highest
 * weighted potential sum of all underlying maps. If a location has no neighbor
 * with a higher potential, it will return the zero vector.
 *
 * @see "Moersch et al. 2013, Hybrid Vector Field Pathfinding, p. 14"
 * @see "Hagelbäck 2012, Potential-Field Based navigation in StarCraft, p. 2"
 * @author mey
 *
 */
public class FlowFromPotentialsMap extends DerivedFlowMap<PotentialMap> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FlowFromPotentialsMap.class.getName());

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
    }

    /**
     * Constructs a new {@link FlowFromPotentialsMap} with given potential map
     * as its first underlying map.
     * 
     * @param firstMap
     *            the first underlying map
     */
    public FlowFromPotentialsMap(PotentialMap firstMap) {
	this(firstMap, NEUTRAL_WEIGHT);
    }

    /**
     * Constructs a new {@link FlowFromPotentialsMap} with given potential map
     * as its first underlying map.
     * 
     * @param firstMap
     *            the first underlying map
     * @param weight
     *            the weight to associate the first map with
     */
    public FlowFromPotentialsMap(PotentialMap firstMap, double weight) {
	this(firstMap.getWidth(), firstMap.getHeight());
	addMap(firstMap, weight);
    }

    /**
     * Constructs a new {@link FlowFromPotentialsMap} with initial content from
     * given modification object.
     * 
     * @param content
     *            the {@link DerivedMap.Changes} object defining initial content
     */
    public FlowFromPotentialsMap(Changes<PotentialMap> content) {
	super(content);
    }

    /**
     * Returns a direction vector pointing towards the neighbor with the highest
     * weighted potential sum of all underlying maps.
     * 
     * @param x
     *            x-coordinate of location
     * @param y
     *            y-coordinate of location
     * @return direction vector pointing towards the highest potential sum
     */
    @Override
    protected Double2D computeDirection(int x, int y) {
	if (getUnderlyingMaps().isEmpty()) {
	    return NEUTRAL;
	}

	double eastSum = 0;
	double southSum = 0;
	double westSum = 0;
	double northSum = 0;

	double southEastSum = 0;
	double southWestSum = 0;
	double northWestSum = 0;
	double northEastSum = 0;

	// sum potentials for every neighbor
	for (PotentialMap map : getUnderlyingMaps()) {
	    eastSum += obtainWeightedPotentialSafe(x + 1, y, map);
	    southSum += obtainWeightedPotentialSafe(x, y + 1, map);
	    westSum += obtainWeightedPotentialSafe(x - 1, y, map);
	    northSum += obtainWeightedPotentialSafe(x, y - 1, map);

	    southEastSum += obtainWeightedPotentialSafe(x + 1, y + 1, map);
	    southWestSum += obtainWeightedPotentialSafe(x - 1, y + 1, map);
	    northWestSum += obtainWeightedPotentialSafe(x - 1, y - 1, map);
	    northEastSum += obtainWeightedPotentialSafe(x + 1, y - 1, map);
	}

	// sum all directions weighted by their potential sum
	Double2D sumVector = EAST.multiply(eastSum).add(SOUTH.multiply(southSum))
		.add(WEST.multiply(westSum)).add(NORTH.multiply(northSum))
		.add(SOUTHEAST.multiply(southEastSum)).add(SOUTHWEST.multiply(southWestSum))
		.add(NORTHWEST.multiply(northWestSum)).add(NORTHEAST.multiply(northEastSum));

	// if neutral direction: return it
	if (sumVector.equals(NEUTRAL)) {
	    return NEUTRAL;
	}
	// otherwise normalize
	return sumVector.normalize();
    }

    /**
     * @param x
     * @param y
     * @param map
     * @return weighted potential from given map or '0' if out of bounds
     */
    private double obtainWeightedPotentialSafe(int x, int y, PotentialMap map) {
	if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
	    return 0;
	}
	return map.obtainPotential(x, y) * getWeight(map);
    }
}
