package de.zmt.kitt.util;

import static java.lang.Math.abs;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import sim.field.grid.*;
import sim.util.*;
import de.zmt.kitt.sim.Habitat;
import ec.util.MersenneTwisterFast;

/**
 * Utility functions for creating habitat and food fields.
 * 
 * @author cmeyer
 * 
 */
public class MapUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(MapUtil.class
	    .getName());

    private static final int DIRECT_MOORE_NEIGHBORHOOD_SIZE = 8;
    private static final int DIRECT_VONNEUMANN_NEIGHBORHOOD_SIZE = 4;

    // bags that are reused in neighborhood lookup
    private static final Bag RESULTS_CACHE = new Bag(
	    DIRECT_MOORE_NEIGHBORHOOD_SIZE);
    private static final IntBag INT_RESULTS_CACHE = new IntBag(
	    DIRECT_VONNEUMANN_NEIGHBORHOOD_SIZE);
    private static final IntBag X_POS_CACHE = new IntBag(
	    DIRECT_MOORE_NEIGHBORHOOD_SIZE);
    private static final IntBag Y_POS_CACHE = new IntBag(
	    DIRECT_MOORE_NEIGHBORHOOD_SIZE);

    /**
     * Creates habitat field from given image map. Colors are associated to
     * habitats. If an invalid color is encountered, {@link Habitat#DEFAULT} is
     * used.
     * 
     * @see Habitat#getColor()
     * @param random
     * @param mapImage
     * @return populated habitat field
     */
    public static IntGrid2D createHabitatGridFromMap(
	    MersenneTwisterFast random, BufferedImage mapImage) {
	logger.fine("Creating habitat field from image.");

	IntGrid2D habitatField = new IntGrid2D(mapImage.getWidth(),
		mapImage.getHeight());

	// traverse habitat field and populate from map image
	for (int y = 0; y < habitatField.getHeight(); y++) {
	    for (int x = 0; x < habitatField.getWidth(); x++) {
		Color color = new Color(mapImage.getRGB(x, y));
		Habitat curHabitat = Habitat.valueOf(color);

		if (curHabitat == null) {
		    logger.warning("Color " + color + " in image " + mapImage
			    + " is not associated to a habitat type. "
			    + "Using default.");
		    curHabitat = Habitat.DEFAULT;
		}

		habitatField.set(x, y, curHabitat.ordinal());
	    }
	}

	return habitatField;
    }

    /**
     * Creates a grid of {@link Double2D} normal vectors for habitat boundaries.
     * Positions not part of a boundary are set to <code>null</code>.
     * <p>
     * <b>NOTE:</b> This is not thread-safe due to the reused cache.
     * 
     * @param habitatGrid
     * @return grid of boundary normals
     * @see <a
     *      href="http://gamedev.stackexchange.com/questions/21059/calculating-normal-vector-on-a-2d-pixelated-map">Calculating
     *      normal vector on a 2d pixelated map</a>
     */
    public static ObjectGrid2D createNormalGridFromHabitats(
	    IntGrid2D habitatGrid) {
	ObjectGrid2D boundaryGrid = buildBoundaryGrid(habitatGrid);
	ObjectGrid2D normalGrid = buildNormalGrid(boundaryGrid, habitatGrid);
	return smoothNormalGrid(normalGrid, habitatGrid);
    }

    /**
     * 
     * @param habitatField
     * @return ObjectGrid2D with boundaries represented by habitat or null for
     *         positions that are not boundaries
     */
    private static ObjectGrid2D buildBoundaryGrid(IntGrid2D habitatField) {
	int w = habitatField.getWidth();
	int h = habitatField.getHeight();
	Habitat[] habitatValues = Habitat.values();
	ObjectGrid2D boundaryGrid = new ObjectGrid2D(w, h);

	// traverse habitat map and mark all boundary positions
	for (int y = 0; y < h; y++) {
	    for (int x = 0; x < w; x++) {
		int ordinal = habitatField.get(x, y);

		habitatField.getVonNeumannNeighbors(x, y, 1, Grid2D.BOUNDED,
			false, INT_RESULTS_CACHE, X_POS_CACHE, Y_POS_CACHE);
		for (int i = 0; i < INT_RESULTS_CACHE.numObjs; i++) {
		    int ngOrdinal = INT_RESULTS_CACHE.get(i);

		    if (ordinal != ngOrdinal) {
			// boundary found: has different neighbor
			boundaryGrid.set(x, y, habitatValues[ordinal]);
			break;
		    }
		}
	    }
	}
	return boundaryGrid;
    }

    /**
     * Finds neighbors for every boundary, calls
     * {@link #calcNormalFromNeighbors(int, int, Int2D[], ObjectGrid2D)} and
     * creates grid of results from it.
     * 
     * @param boundaryGrid
     * @param habitatGrid
     * @return grid of boundary normals
     */
    private static ObjectGrid2D buildNormalGrid(ObjectGrid2D boundaryGrid,
	    IntGrid2D habitatGrid) {
	int w = boundaryGrid.getWidth();
	int h = boundaryGrid.getHeight();
	ObjectGrid2D normalGrid = new ObjectGrid2D(w, h);
	// find normal for every position marked as boundary
	for (int y = 0; y < h; y++) {
	    for (int x = 0; x < w; x++) {
		Habitat boundaryHabitat = (Habitat) boundaryGrid.get(x, y);

		// this is not part of a boundary, skip element
		if (boundaryHabitat == null) {
		    continue;
		}

		Int2D[] boundaryNeighbors = findBoundaryNeighbors(boundaryGrid,
			y, x, boundaryHabitat);
		Double2D normal = calcNormalFromNeighbors(x, y,
			boundaryHabitat, boundaryNeighbors, habitatGrid);
		normalGrid.set(x, y, normal);
	    }
	}

	return normalGrid;
    }

    /**
     * 
     * @param boundaryGrid
     * @param y
     * @param x
     * @param boundaryHabitat
     * @return maximum of two neighbors in array of length 2
     */
    private static Int2D[] findBoundaryNeighbors(ObjectGrid2D boundaryGrid,
	    int y, int x, Habitat boundaryHabitat) {
	boundaryGrid.getMooreNeighborsAndLocations(x, y, 1, Grid2D.BOUNDED,
		false, RESULTS_CACHE, X_POS_CACHE, Y_POS_CACHE);
	// no more than two neighbors needed in 3x3 patch
	Int2D[] boundaryNeighbors = new Int2D[2];
	for (int i = 0; i < X_POS_CACHE.size(); i++) {
	    int xPos = X_POS_CACHE.get(i);
	    int yPos = Y_POS_CACHE.get(i);
	    Habitat ngBoundaryNeighborHabitat = (Habitat) RESULTS_CACHE.get(i);

	    // check if neighbor is part of the same boundary
	    if (ngBoundaryNeighborHabitat == boundaryHabitat) {
		Int2D boundaryNeighbor = new Int2D(xPos, yPos);
		if (boundaryNeighbors[0] == null) {
		    boundaryNeighbors[0] = boundaryNeighbor;
		}
		// in case of having more than two neighbors:
		// keep the last one
		// to prevent neighbors from being next to each other
		else {
		    boundaryNeighbors[1] = boundaryNeighbor;
		}
	    }
	}

	return boundaryNeighbors;
    }

    /**
     * Finds normal vector from neighbors pointing towards adjacent boundary.
     * 
     * @param x
     * @param y
     * @param boundaryNeighbors
     *            two neighbors: array length 2
     * @param habitatGrid
     * @return normal vector
     */
    private static Double2D calcNormalFromNeighbors(int x, int y,
	    Habitat boundaryHabitat, Int2D[] boundaryNeighbors,
	    IntGrid2D habitatGrid) {
	// SPECIAL CASE
	// No neighbors (single pixel island)
	if (boundaryNeighbors[0] == null) {
	    logger.warning("Single pixel island at " + x + "x" + y
		    + "! Cannot derive a direction from that.");
	    return new Double2D();
	}

	// directions towards first neighbor
	Int2D ng0Dir = new Int2D(boundaryNeighbors[0].x - x,
		boundaryNeighbors[0].y - y);

	// SPECIAL CASE
	// end of line: only one neighbor
	if (boundaryNeighbors[1] == null) {
	    // not on grid boundary
	    if (x != 0 && x != habitatGrid.getWidth() - 1 && y != 0
		    && y != habitatGrid.getHeight() - 1) {
		// let normal point away from neighbor
		return new Double2D(x - boundaryNeighbors[0].x, y
			- boundaryNeighbors[0].y).normalize();
	    }
	    // on grid boundary: suppose habitat boundary will just continue
	    else {
		boundaryNeighbors[1] = new Int2D(x - ng0Dir.x, y - ng0Dir.y);
	    }
	}

	Double2D normal = calcNormalFromBothNeighbors(x, y, boundaryNeighbors,
		ng0Dir);

	// let the normal point outwards
	// check where the border is located and negate if needed
	if (habitatGrid.get(x + (int) Math.round(normal.x),
		y + (int) Math.round(normal.y)) != boundaryHabitat.ordinal()) {
	    return normal.normalize();
	} else {
	    return normal.normalize().negate();
	}
    }

    /**
     * 
     * @param x
     * @param y
     * @param boundaryNeighbors
     * @param ng0Dir
     *            direction towards first neighbor
     * @return raw normal calculated from both neighbors
     */
    private static Double2D calcNormalFromBothNeighbors(int x, int y,
	    Int2D[] boundaryNeighbors, Int2D ng0Dir) {
	Double2D normal;

	// direction towards second neighbor
	Int2D ng1Dir = new Int2D(boundaryNeighbors[1].x - x,
		boundaryNeighbors[1].y - y);

	// SPECIAL CASE
	// straight boundary
	if ((ng0Dir.x + ng1Dir.x) == 0 && (ng0Dir.y + ng1Dir.y) == 0) {
	    // diagonal from upper left to lower right
	    if (ng0Dir.x == ng0Dir.y) {
		normal = new Double2D(1, -1);
	    }
	    // straight vertical
	    else if (ng0Dir.x == 0 && abs(ng0Dir.y) == 1) {
		normal = new Double2D(1, 0);
	    }
	    // diagonal from upper right to lower left
	    else if (ng0Dir.x == -ng0Dir.y) {
		normal = new Double2D(1, 1);
	    }
	    // straight horizontal
	    else if (abs(ng0Dir.x) == 1 && ng0Dir.y == 0) {
		normal = new Double2D(0, 1);
	    } else {
		throw new IllegalArgumentException(
			"Boundary neighbors are not next to given coordinates.");
	    }
	}
	// NORMAL CASE
	// two neighbors + uneven border
	else {
	    Double2D ng0DirNormal = new Double2D(ng0Dir.x, ng0Dir.y)
		    .normalize();
	    Double2D ng1DirNormal = new Double2D(ng1Dir.x, ng1Dir.y)
		    .normalize();
	    normal = ng0DirNormal.add(ng1DirNormal);
	}
	return normal;
    }

    /**
     * Average every normal with its neighbors to make reflections more smooth.
     * 
     * @param normalGrid
     * @param habitatGrid
     * @return smooth normal grid
     */
    private static ObjectGrid2D smoothNormalGrid(ObjectGrid2D normalGrid,
	    IntGrid2D habitatGrid) {
	ObjectGrid2D smoothNormalGrid = new ObjectGrid2D(normalGrid.getWidth(),
		normalGrid.getHeight());

	for (int y = 0; y < normalGrid.getHeight(); y++) {
	    for (int x = 0; x < normalGrid.getWidth(); x++) {
		Double2D normal = (Double2D) normalGrid.get(x, y);
		// no normal at this position, continue with the next one
		if (normal == null) {
		    continue;
		}

		int habitatOrdinal = habitatGrid.get(x, y);
		MutableDouble2D normalsAvg = new MutableDouble2D(normal);
		normalGrid.getMooreNeighborsAndLocations(x, y, 1,
			Grid2D.BOUNDED, false, RESULTS_CACHE, X_POS_CACHE,
			Y_POS_CACHE);
		int ngNormalCount = 0;

		// traverse neighbor normals
		for (int i = 0; i < RESULTS_CACHE.numObjs; i++) {
		    int ngX = X_POS_CACHE.get(i);
		    int ngY = Y_POS_CACHE.get(i);

		    // check if neighbor normal belongs to same boundary
		    if (habitatGrid.get(ngX, ngY) == habitatOrdinal) {
			// add neighbor normal
			normalsAvg.addIn((Double2D) RESULTS_CACHE.get(i));
			ngNormalCount++;
		    }
		}

		// divide by count to get the average
		normalsAvg.multiplyIn(1 / (double) ngNormalCount);
		smoothNormalGrid
			.set(x, y, new Double2D(normalsAvg).normalize());
	    }
	}

	return smoothNormalGrid;
    }

    /**
     * Creates food field populated by random values between min and max values
     * from {@link Habitat} definitions.
     * 
     * @see Habitat#getFoodMin()
     * @see Habitat#getFoodMax()
     * @param habitatField
     * @param random
     * @param mapScale
     *            in pixel per meter
     * @return populated food field
     */
    public static DoubleGrid2D createFoodFieldFromHabitats(
	    IntGrid2D habitatField, MersenneTwisterFast random, double mapScale) {
	logger.fine("creating food field from habitat field");

	DoubleGrid2D foodField = new DoubleGrid2D(
		(int) (habitatField.getWidth() / mapScale),
		(int) (habitatField.getHeight() / mapScale));
	// traverse food grid and populate from habitat rules
	for (int y = 0; y < foodField.getHeight(); y++) {
	    for (int x = 0; x < foodField.getWidth(); x++) {
		Habitat currentHabitat = Habitat.values()[habitatField.get(
			(int) (x * mapScale), (int) (y * mapScale))];

		double minFood = currentHabitat.getFoodMin();
		double maxFood = currentHabitat.getFoodMax();
		double foodVal = random.nextDouble() * (maxFood - minFood)
			+ minFood;
		foodField.set(x, y, foodVal);
	    }
	}

	return foodField;
    }
}
