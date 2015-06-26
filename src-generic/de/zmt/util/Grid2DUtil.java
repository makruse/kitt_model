package de.zmt.util;

import sim.field.grid.DoubleGrid2D;
import sim.util.*;

/**
 * Contains modified methods from {@link sim.field.grid.AbstractGrid2D} to
 * handle real numbers in neighborhood lookup for position and distance.
 * 
 * @see sim.field.grid.AbstractGrid2D
 * @author cmeyer
 * 
 */
// TODO add vonneumann and other grid2d features
public final class Grid2DUtil {
    private Grid2DUtil() {

    }

    /** X/Y extent of a square within grid */
    private static final int SQUARE_SIZE = 1;
    private static final double SQUARE_HALF_SIZE = SQUARE_SIZE / 2d;

    /**
     * @see #findMooreLocations(int, int, Double2D, double, LookupMode,
     *      LocationsResult)
     * @param width
     *            grid width
     * @param height
     *            grid height
     * @param center
     *            center of lookup
     * @param dist
     *            distance of lookup
     * @param mode
     *            {@link LookupMode}
     * @return {@link LocationsResult}
     */
    public static LocationsResult findMooreLocations(int width, int height,
	    Double2D center, final double dist, LookupMode mode) {
	return findMooreLocations(width, height, center, dist, mode,
		new LocationsResult());
    }

    /**
     * Moore locations lookup with real numbers support.
     * 
     * @see sim.field.grid.Grid2D#getMooreLocations(int, int, int, int, boolean,
     *      IntBag, IntBag)
     * @param width
     *            grid width
     * @param height
     *            grid height
     * @param center
     *            center of lookup
     * @param dist
     *            distance of lookup
     * @param mode
     *            {@link LookupMode}
     * @param resultsObject
     *            that will be reused
     * @return {@link LocationsResult} given {@code resultsObject} with results
     */
    public static LocationsResult findMooreLocations(int width, int height,
	    Double2D center, final double dist, LookupMode mode,
	    LocationsResult resultsObject) {
	boolean bounded = mode == LookupMode.BOUNDED;
	double x = center.x;
	double y = center.y;

	// won't work for negative distances
	if (dist < 0) {
	    throw new RuntimeException("Distance must be positive");
	}

	if ((x < 0 || x >= width || y < 0 || y >= height) && bounded) {
	    throw new RuntimeException("Invalid initial position");
	}

	resultsObject.xPos.clear();
	resultsObject.yPos.clear();

	// for toroidal environments the code will be different because of
	// wrapping around
	if (mode == LookupMode.TOROIDAL) {
	    // compute xmin and xmax for the neighborhood
	    int xmin = (int) (x - dist);
	    int xmax = (int) (x + dist);

	    // next: is xmax - xmin humongous? If so, no need to continue
	    // wrapping around
	    if (xmax - xmin >= width) // too wide, just use whole neighborhood
	    {
		xmin = 0;
		xmax = width - 1;
	    }

	    // compute ymin and ymax for the neighborhood
	    int ymin = (int) (y - dist);
	    int ymax = (int) (y + dist);

	    // next: is ymax - ymin humongous? If so, no need to continue
	    // wrapping around
	    if (ymax - ymin >= height) // too wide, just use whole neighborhood
	    {
		ymin = 0;
		ymax = width - 1;
	    }

	    for (int x0 = xmin; x0 <= xmax; x0++) {
		final int x_0 = tx(x0, width, width * 2, x0 + width, x0 - width);
		for (int y0 = ymin; y0 <= ymax; y0++) {
		    final int y_0 = ty(y0, height, height * 2, y0 + height, y0
			    - height);
		    resultsObject.xPos.add(x_0);
		    resultsObject.yPos.add(y_0);
		}
	    }
	}
	// not toroidal
	else {
	    // compute xmin and xmax for the neighborhood such that they are
	    // within boundaries
	    final int xmin = (int) ((x - dist >= 0) || !bounded ? x - dist : 0);
	    final int xmax = (int) ((x + dist < width - 1) || !bounded ? x
		    + dist : width - 1);
	    // compute ymin and ymax for the neighborhood such that they are
	    // within boundaries
	    final int ymin = (int) ((y - dist >= 0) || !bounded ? y - dist : 0);
	    final int ymax = (int) ((y + dist < height - 1) || !bounded ? y
		    + dist : height - 1);
	    for (int x0 = xmin; x0 <= xmax; x0++) {
		for (int y0 = ymin; y0 <= ymax; y0++) {
		    resultsObject.xPos.add(x0);
		    resultsObject.yPos.add(y0);
		}
	    }
	}

	return resultsObject;
    }

    // this internal version of tx is arranged to be 34 bytes. It first tries
    // stx, then tx.
    private static int tx(int x, int width, int widthtimestwo, int xpluswidth,
	    int xminuswidth) {
	if (x >= -width && x < widthtimestwo) {
	    if (x < 0) {
		return xpluswidth;
	    }
	    if (x < width) {
		return x;
	    }
	    return xminuswidth;
	}
	return tx2(x, width);
    }

    // used internally by the internal version of tx above. Do not call
    // directly.
    private static int tx2(int x, int width) {
	x = x % width;
	if (x < 0) {
	    x = x + width;
	}
	return x;
    }

    // this internal version of ty is arranged to be 34 bytes. It first tries
    // sty, then ty.
    private static int ty(int y, int height, int heighttimestwo,
	    int yplusheight, int yminusheight) {
	if (y >= -height && y < heighttimestwo) {
	    if (y < 0) {
		return yplusheight;
	    }
	    if (y < height) {
		return y;
	    }
	    return yminusheight;
	}
	return ty2(y, height);
    }

    // used internally by the internal version of ty above. Do not call
    // directly.
    private static int ty2(int y, int height) {
	y = y % height;
	if (y < 0) {
	    y = y + height;
	}
	return y;
    }

    /**
     * @see #findRadialLocations(int, int, Double2D, double, LookupMode,
     *      LocationsResult)
     * @param width
     *            grid width
     * @param height
     *            grid height
     * @param center
     *            center of lookup
     * @param radius
     *            radius of lookup
     * @param mode
     *            {@link LookupMode}
     * @return {@link LocationsResult}
     */
    public static LocationsResult findRadialLocations(int width, int height,
	    Double2D center, final double radius, LookupMode mode) {
	return findRadialLocations(width, height, center, radius, mode,
		new LocationsResult());
    }

    /**
     * Radial locations lookup with real numbers support.
     * 
     * @see sim.field.grid.Grid2D#getRadialLocations(int, int, int, int,
     *      boolean, IntBag, IntBag)
     * @param width
     *            grid width
     * @param height
     *            grid height
     * @param center
     *            center of lookup
     * @param dist
     *            distance of lookup
     * @param mode
     *            {@link LookupMode}
     * @param resultsObject
     *            that will be reused
     * @return {@link LocationsResult} given {@code resultsObject} with results
     */
    public static LocationsResult findRadialLocations(int width, int height,
	    Double2D center, final double radius, LookupMode mode,
	    LocationsResult resultsObject) {
	// won't work for negative radius
	if (radius < 0) {
	    throw new RuntimeException("Radius must be positive");
	}

	// grab the rectangle
	if ((mode == LookupMode.TOROIDAL)) {
	    findMooreLocations(width, height, center, radius + 0.5,
		    LookupMode.UNBOUNDED, resultsObject);
	} else {
	    findMooreLocations(width, height, center, radius, mode,
		    resultsObject);
	}
	int len = resultsObject.xPos.size();

	int widthtimestwo = width * 2;
	int heighttimestwo = height * 2;
	double radiusSq = radius * radius;

	// remove all positions from moore neighborhood not within radius
	for (int i = 0; i < len; i++) {
	    int xp = resultsObject.xPos.get(i);
	    int yp = resultsObject.yPos.get(i);

	    // not within radius: remove position, do not save distance
	    if (!squareWithinRadius(center, radiusSq, xp, yp)) {
		resultsObject.xPos.remove(i);
		resultsObject.yPos.remove(i);
		i--;
		len--;
	    }
	    // need to convert to toroidal position
	    else if (mode == LookupMode.TOROIDAL) {
		int _x = resultsObject.xPos.get(i);
		int _y = resultsObject.yPos.get(i);
		resultsObject.xPos.set(i,
			tx(_x, width, widthtimestwo, _x + width, _x - width));
		resultsObject.yPos.set(i,
			tx(_y, height, heighttimestwo, _y + width, _y - width));
	    }
	}

	return resultsObject;
    }

    /**
     * Checks intersection between a circle and a (grid) square (length 1).
     * 
     * @see <a href=http://www.gamasutra.com/view/feature/131790/
     *      simple_intersection_tests_for_games.php?page=4>Gamasutra: A
     *      Box-Sphere Intersection Test</a>
     * @param circleCenter
     * @param circleRadiusSq
     *            squared radius of the circle
     * @param squareX
     * @param squareY
     * @return true if circle intersects square
     */
    private static boolean squareWithinRadius(Double2D circleCenter,
	    double circleRadiusSq, int squareX, int squareY) {

	double d = 0;

	d += squareWithinRadius_dim(squareX, circleCenter.x);
	d += squareWithinRadius_dim(squareY, circleCenter.y);

	return d < circleRadiusSq;

    }

    private static double squareWithinRadius_dim(int squareCoord,
	    double centerCoord) {
	double difference = 0;
	if (centerCoord < squareCoord) {
	    difference = centerCoord - squareCoord;
	} else if (centerCoord > squareCoord + SQUARE_SIZE) {
	    difference = centerCoord - (squareCoord + SQUARE_SIZE);
	}

	return difference * difference;
    }

    /**
     * Combination of radial locations lookup and obtaining the values for these
     * locations.
     * 
     * @see sim.field.grid.Grid2D#getRadialLocations(int, int, int, int,
     *      boolean, IntBag, IntBag)
     * @param grid
     * @param center
     *            center of lookup
     * @param dist
     *            distance of lookup
     * @param mode
     *            {@link LookupMode}
     * @param resultsObject
     *            that will be reused
     * @return {@link DoubleNeighborsResult}
     */
    public static DoubleNeighborsResult findRadialNeighbors(DoubleGrid2D grid,
	    Double2D center, final double radius, LookupMode mode) {
	return findRadialNeighbors(grid, center, radius, mode,
		new DoubleNeighborsResult());
    }

    /**
     * Combination of radial locations lookup and obtaining the values for these
     * locations.
     * 
     * @see sim.field.grid.Grid2D#getRadialLocations(int, int, int, int,
     *      boolean, IntBag, IntBag)
     * @param grid
     * @param center
     *            center of lookup
     * @param dist
     *            distance of lookup
     * @param mode
     *            {@link LookupMode}
     * @param resultsObject
     *            that will be reused
     * @return {@link DoubleNeighborsResult} given {@code resultsObject} with
     *         results
     */
    public static DoubleNeighborsResult findRadialNeighbors(DoubleGrid2D grid,
	    Double2D center, final double radius, LookupMode mode,
	    DoubleNeighborsResult resultObject) {
	findRadialLocations(grid.getWidth(), grid.getHeight(), center, radius,
		mode, resultObject.locationsResult);
	return obtainValuesAtLocations(grid, resultObject);
    }

    /**
     * 
     * @param grid
     * @param resultObject
     * @return result object with added values from contained locations
     */
    private static DoubleNeighborsResult obtainValuesAtLocations(
	    DoubleGrid2D grid, DoubleNeighborsResult resultObject) {
	LocationsResult locationsResult = resultObject.locationsResult;
	resultObject.values.clear();

	for (int i = 0; i < locationsResult.xPos.numObjs; i++) {
	    double val = grid.field[locationsResult.xPos.objs[i]][locationsResult.yPos.objs[i]];
	    resultObject.values.add(val);
	}
	return resultObject;
    }

    /**
     * 
     * @param locations
     * @param center
     * @return {@link DoubleBag} with squared distances from center to each
     *         indexed location
     */
    public static DoubleBag computeDistancesSq(LocationsResult locations,
	    Double2D center) {
	int length = locations.xPos.numObjs;
	DoubleBag distancesSq = new DoubleBag(length);
	for (int i = 0; i < length; i++) {
	    // use center of square for computing distance
	    distancesSq.add(center.distanceSq(locations.xPos.get(i)
		    + SQUARE_HALF_SIZE, locations.yPos.get(i)
		    + SQUARE_HALF_SIZE));
	}

	return distancesSq;
    }

    /**
     * Contains x and y grid positions from neighborhood lookup.
     * 
     * @author cmeyer
     * 
     */
    public static class LocationsResult {
	public final IntBag xPos;
	public final IntBag yPos;

	public LocationsResult() {
	    xPos = new IntBag();
	    yPos = new IntBag();
	}

	public LocationsResult(IntBag xPos, IntBag yPos) {
	    this.xPos = xPos;
	    this.yPos = yPos;
	}
    }

    /**
     * Contains a {@link LocationsResult} and values from these locations.
     * 
     * @author cmeyer
     * 
     */
    public static class DoubleNeighborsResult {
	public final LocationsResult locationsResult;
	public final DoubleBag values;

	public DoubleNeighborsResult() {
	    this.locationsResult = new LocationsResult();
	    this.values = new DoubleBag();
	}

	public DoubleNeighborsResult(LocationsResult radialLocationsResult,
		DoubleBag values) {
	    this.locationsResult = radialLocationsResult;
	    this.values = values;
	}
    }

    /**
     * Wrapper for lookup modes found in {@link Grid2D}.
     * 
     * @author cmeyer
     * 
     */
    public static enum LookupMode {
	/** @see Grid2D#BOUNDED */
	BOUNDED,
	/** @see Grid2D#UNBOUNDED */
	UNBOUNDED,
	/** @see Grid2D#TOROIDAL */
	TOROIDAL
    }
}
