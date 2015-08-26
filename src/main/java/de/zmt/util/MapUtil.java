package de.zmt.util;

import static java.lang.Math.abs;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.imageio.ImageIO;

import de.zmt.sim.Habitat;
import ec.util.MersenneTwisterFast;
import sim.field.grid.*;
import sim.util.*;

/**
 * Utility functions for creating grids based on a habitat map.
 * 
 * @author cmeyer
 * 
 */
public final class MapUtil {
    private MapUtil() {

    }

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(MapUtil.class.getName());

    private static final int DIRECT_MOORE_NEIGHBORHOOD_SIZE = 8;
    private static final int DIRECT_VON_NEUMANN_NEIGHBORHOOD_SIZE = 4;

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
    public static IntGrid2D createHabitatGridFromMap(MersenneTwisterFast random, BufferedImage mapImage) {
	logger.fine("Creating habitat field from image.");

	IntGrid2D habitatField = new IntGrid2D(mapImage.getWidth(), mapImage.getHeight());

	// traverse habitat field and populate from map image
	for (int y = 0; y < habitatField.getHeight(); y++) {
	    for (int x = 0; x < habitatField.getWidth(); x++) {
		Color color = new Color(mapImage.getRGB(x, y));
		Habitat curHabitat = Habitat.valueOf(color);

		if (curHabitat == null) {
		    logger.warning("Color " + color + " in image " + mapImage + " is not associated to a habitat type. "
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
     * @see <a href=
     *      "http://gamedev.stackexchange.com/questions/21059/calculating-normal-vector-on-a-2d-pixelated-map">
     *      Calculating normal vector on a 2d pixelated map</a>
     */
    // FIXME thin areas do not work (other boundaries present in 3x3 patch)
    // TODO specify habitats to calculate normals for
    public static ObjectGrid2D createNormalGridFromHabitats(IntGrid2D habitatGrid) {
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

	// cache bags in neighborhood lookups
	IntBag results = new IntBag(DIRECT_VON_NEUMANN_NEIGHBORHOOD_SIZE);
	IntBag xPos = new IntBag(DIRECT_VON_NEUMANN_NEIGHBORHOOD_SIZE);
	IntBag yPos = new IntBag(DIRECT_VON_NEUMANN_NEIGHBORHOOD_SIZE);

	// traverse habitat map and mark all boundary positions
	for (int y = 0; y < h; y++) {
	    for (int x = 0; x < w; x++) {
		int ordinal = habitatField.get(x, y);

		habitatField.getVonNeumannNeighbors(x, y, 1, Grid2D.BOUNDED, false, results, xPos, yPos);
		for (int i = 0; i < results.numObjs; i++) {
		    int ngOrdinal = results.get(i);

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
     * {@link #computeNormalFromNeighbors(int, int, Habitat, BoundaryNeighbors, IntGrid2D)}
     * and creates grid of results from it.
     * 
     * @param boundaryGrid
     * @param habitatGrid
     * @return grid of boundary normals
     */
    private static ObjectGrid2D buildNormalGrid(ObjectGrid2D boundaryGrid, IntGrid2D habitatGrid) {
	int w = boundaryGrid.getWidth();
	int h = boundaryGrid.getHeight();
	ObjectGrid2D normalGrid = new ObjectGrid2D(w, h);
	LookupCache cache = new LookupCache();

	// find normal for every position marked as boundary
	for (int y = 0; y < h; y++) {
	    for (int x = 0; x < w; x++) {
		Habitat boundaryHabitat = (Habitat) boundaryGrid.get(x, y);

		// this is not part of a boundary, skip element
		if (boundaryHabitat == null) {
		    continue;
		}

		BoundaryNeighbors neighbors = findBoundaryNeighbors(boundaryGrid, y, x, boundaryHabitat, cache);
		Double2D normal = computeNormalFromNeighbors(x, y, boundaryHabitat, neighbors, habitatGrid);
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
     * @param cache
     * @return boundary neighbors object
     */
    private static BoundaryNeighbors findBoundaryNeighbors(ObjectGrid2D boundaryGrid, int y, int x,
	    Habitat boundaryHabitat, LookupCache cache) {
	boundaryGrid.getMooreNeighborsAndLocations(x, y, 1, Grid2D.BOUNDED, false, cache.results, cache.xPos,
		cache.yPos);
	// no more than two neighbors needed in 3x3 patch
	Deque<Int2D> neighborsInBoundary = new ArrayDeque<>(DIRECT_MOORE_NEIGHBORHOOD_SIZE);
	for (int i = 0; i < cache.xPos.size(); i++) {
	    int xPos = cache.xPos.get(i);
	    int yPos = cache.yPos.get(i);
	    Habitat ngBoundaryNeighborHabitat = (Habitat) cache.results.get(i);

	    // check if neighbor is part of the same boundary
	    if (ngBoundaryNeighborHabitat == boundaryHabitat) {
		Int2D neighborInBoundary = new Int2D(xPos, yPos);
		neighborsInBoundary.add(neighborInBoundary);
	    }
	}

	switch (neighborsInBoundary.size()) {
	case 0:
	    logger.warning("Single pixel island at " + x + "x" + y + "! Cannot derive a direction from that.");
	    return new BoundaryNeighbors(null, null);
	case 1:
	    return new BoundaryNeighbors(neighborsInBoundary.getFirst(), null);
	case 2:
	case 3:
	    return new BoundaryNeighbors(neighborsInBoundary.getFirst(), neighborsInBoundary.getLast());
	default:
	    logger.warning("Could not find neighbors for (" + x + ", " + y + "): More than one boundary at that pixel."
		    + " The area is too small.");
	    return new BoundaryNeighbors(null, null);
	}
    }

    /**
     * Finds normal vector pointing towards adjacent boundary from neighbors.
     * 
     * @param x
     * @param y
     * @param boundaryHabitat
     *            habitat enclosed by this boundary
     * @param neighbors
     * @param habitatGrid
     * @return normal vector
     */
    private static Double2D computeNormalFromNeighbors(int x, int y, Habitat boundaryHabitat,
	    BoundaryNeighbors neighbors, IntGrid2D habitatGrid) {
	// SPECIAL CASE
	// No neighbors (single pixel island)
	if (neighbors.first == null) {
	    return null;
	}

	// directions towards first neighbor
	Int2D ng1Dir = new Int2D(neighbors.first.x - x, neighbors.first.y - y);
	Int2D ng2Pos = neighbors.second;

	// SPECIAL CASE
	// end of line: only one neighbor
	if (ng2Pos == null) {
	    // not on grid boundary (peninsula):
	    if (x != 0 && x != habitatGrid.getWidth() - 1 && y != 0 && y != habitatGrid.getHeight() - 1) {
		// let normal point away from neighbor
		return new Double2D(x - neighbors.first.x, y - neighbors.first.y).normalize();
	    }
	    // on grid boundary: suppose habitat boundary will just continue
	    else {
		ng2Pos = new Int2D(x - ng1Dir.x, y - ng1Dir.y);
	    }
	}

	// direction towards second neighbor
	Int2D ng2Dir = new Int2D(ng2Pos.x - x, ng2Pos.y - y);
	Double2D normal = computeNormalFromNeighborDirections(x, y, ng1Dir, ng2Dir);

	// compute where the normal is pointing to, clamp to grid boundaries
	int normalTargetX = clamp(x + (int) Math.round(normal.x), 0, habitatGrid.getWidth() - 1);
	int normalTargetY = clamp(y + (int) Math.round(normal.y), 0, habitatGrid.getHeight() - 1);

	// let the normal point outwards
	// check where the border is located and negate if needed
	if (habitatGrid.get(normalTargetX, normalTargetY) != boundaryHabitat.ordinal()) {
	    return normal.normalize();
	} else {
	    return normal.normalize().negate();
	}
    }

    /**
     * 
     * @param x
     * @param y
     * @param ng1Dir
     *            direction towards first neighbor
     * @param ng2Dir
     *            direction towards second neighbor
     * @return raw normal calculated from both neighbors
     */
    private static Double2D computeNormalFromNeighborDirections(int x, int y, Int2D ng1Dir, Int2D ng2Dir) {
	// SPECIAL CASE
	// straight boundary
	if ((ng1Dir.x + ng2Dir.x) == 0 && (ng1Dir.y + ng2Dir.y) == 0) {
	    // diagonal from upper left to lower right
	    if (ng1Dir.x == ng1Dir.y) {
		return new Double2D(1, -1);
	    }
	    // straight vertical
	    else if (ng1Dir.x == 0 && abs(ng1Dir.y) == 1) {
		return new Double2D(1, 0);
	    }
	    // diagonal from upper right to lower left
	    else if (ng1Dir.x == -ng1Dir.y) {
		return new Double2D(1, 1);
	    }
	    // straight horizontal
	    else if (abs(ng1Dir.x) == 1 && ng1Dir.y == 0) {
		return new Double2D(0, 1);
	    }
	    throw new IllegalArgumentException("Boundary neighbors are not next to given coordinates.");
	}
	// NORMAL CASE
	// two neighbors + uneven border
	Double2D ng1DirNormal = new Double2D(ng1Dir.x, ng1Dir.y).normalize();
	Double2D ng2DirNormal = new Double2D(ng2Dir.x, ng2Dir.y).normalize();
	return ng1DirNormal.add(ng2DirNormal);
    }

    /**
     * @param value
     * @param min
     * @param max
     * @return {@code value} clamped between {@code min} and {@code max}.
     */
    private static int clamp(int value, int min, int max) {
	return Math.max(Math.min(value, max), min);
    }

    /**
     * Average every normal with its neighbors to make reflections more smooth.
     * 
     * @param normalGrid
     * @param habitatGrid
     * @return smooth normal grid
     */
    private static ObjectGrid2D smoothNormalGrid(ObjectGrid2D normalGrid, IntGrid2D habitatGrid) {
	ObjectGrid2D smoothNormalGrid = new ObjectGrid2D(normalGrid.getWidth(), normalGrid.getHeight());
	LookupCache cache = new LookupCache();

	for (int y = 0; y < normalGrid.getHeight(); y++) {
	    for (int x = 0; x < normalGrid.getWidth(); x++) {
		Double2D normal = (Double2D) normalGrid.get(x, y);
		// no normal at this position, continue with the next one
		if (normal == null) {
		    smoothNormalGrid.set(x, y, new Double2D());
		    continue;
		}

		int habitatOrdinal = habitatGrid.get(x, y);
		MutableDouble2D normalsAvg = new MutableDouble2D(normal);
		normalGrid.getMooreNeighborsAndLocations(x, y, 1, Grid2D.BOUNDED, false, cache.results, cache.xPos,
			cache.yPos);
		int ngNormalCount = 0;

		// traverse neighbor normals
		for (int i = 0; i < cache.results.numObjs; i++) {
		    int ngX = cache.xPos.get(i);
		    int ngY = cache.yPos.get(i);

		    // check if neighbor normal belongs to same boundary
		    if (habitatGrid.get(ngX, ngY) == habitatOrdinal) {
			// add neighbor normal
			normalsAvg.addIn((Double2D) cache.results.get(i));
			ngNormalCount++;
		    }
		}

		// divide by count to get the average
		normalsAvg.multiplyIn(1 / (double) ngNormalCount);

		if (normalsAvg.equals(new Double2D(0, 0))) {
		    logger.warning("Could not calculate normal for (" + x + ", " + y
			    + "): Areas need to be thicker than one pixel.");
		} else {
		    smoothNormalGrid.set(x, y, new Double2D(normalsAvg).normalize());
		}
	    }
	}

	return smoothNormalGrid;
    }

    /**
     * Creates food field populated by random values of available food within
     * range from {@link Habitat} definitions.
     * 
     * @see Habitat#getFoodDensityRange()
     * @param habitatField
     * @param random
     * @return populated food field
     */
    public static DoubleGrid2D createFoodFieldFromHabitats(IntGrid2D habitatField, MersenneTwisterFast random) {
	logger.fine("creating food field from habitat field");

	DoubleGrid2D foodField = new DoubleGrid2D(habitatField.getWidth(), habitatField.getHeight());
	// traverse food grid and populate from habitat rules
	for (int y = 0; y < foodField.getHeight(); y++) {
	    for (int x = 0; x < foodField.getWidth(); x++) {
		Habitat currentHabitat = Habitat.values()[habitatField.get(x, y)];

		double foodRange = currentHabitat.getFoodDensityRange().getEstimatedValue();
		// random value between 0 and range
		double foodVal = random.nextDouble() * foodRange;
		foodField.set(x, y, foodVal);
	    }
	}

	return foodField;
    }

    public static BufferedImage loadMapImage(String imagePath) {
	BufferedImage mapImage = null;
	logger.fine("Loading map image from " + imagePath);
	try {
	    mapImage = ImageIO.read(new File(imagePath));
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Could not load map image from " + imagePath);
	}
	return mapImage;
    }

    /**
     * Class for caching bags in neighborhood lookup.
     * 
     * @author cmeyer
     * 
     */
    private static class LookupCache {
	private final Bag results = new Bag(DIRECT_MOORE_NEIGHBORHOOD_SIZE);
	private final IntBag xPos = new IntBag(DIRECT_MOORE_NEIGHBORHOOD_SIZE);
	private final IntBag yPos = new IntBag(DIRECT_MOORE_NEIGHBORHOOD_SIZE);
    }

    /**
     * Positions of two neighbors from the same habitat boundary than the
     * corresponding position.
     * 
     * @author cmeyer
     * 
     */
    private static class BoundaryNeighbors {
	private final Int2D first;
	private final Int2D second;

	public BoundaryNeighbors(Int2D first, Int2D second) {
	    this.first = first;
	    this.second = second;
	}
    }
}
