package de.zmt.util;

import java.util.Arrays;

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
// TODO add lookup via grayscale image (BufferedImage)
public final class Grid2DUtil {
    private Grid2DUtil() {

    }

    /** X/Y extent of a square within grid */
    private static final int SQUARE_SIZE = 1;
    private static final double SQUARE_HALF_SIZE = SQUARE_SIZE / 2d;

    /**
     * Find locations along a line within a field.
     * 
     * @see #findLineLocations(int, int, Int2D, Int2D, LookupMode,
     *      LocationsResult)
     * @param width
     *            grid width
     * @param height
     *            grid height
     * @param start
     *            start point
     * @param end
     *            end point
     * @param mode
     *            {@link LookupMode}
     * @return {@link LocationsResult}
     */
    public static LocationsResult findLineLocations(int width, int height,
	    Int2D start, Int2D end, LookupMode mode) {
	LocationsResult resultObject = new LocationsResult();
	return findLineLocations(width, height, start, end, mode, resultObject);
    }

    /**
     * Find locations along a line within a field.
     * 
     * @param width
     *            grid width
     * @param height
     *            grid height
     * @param start
     *            start point
     * @param end
     *            end point
     * @param mode
     *            {@link LookupMode}
     * @param resultObject
     *            that will be reused
     * @return {@link LocationsResult} given {@code resultsObject} with results
     */
    public static LocationsResult findLineLocations(int width, int height,
	    Int2D start, Int2D end, LookupMode mode,
	    LocationsResult resultObject) {
	// TODO toroidal mode
	if (mode == LookupMode.TOROIDAL) {
	    throw new UnsupportedOperationException(LookupMode.TOROIDAL
		    + " not yet implemented for line lookup.");
	}

	// check parameter validity
	if ((checkOutBounds(start, width, height) || checkOutBounds(end, width,
		height))
		&& (mode == LookupMode.BOUNDED || mode == LookupMode.TOROIDAL)) {
	    throw new IllegalArgumentException(
		    "Start and end positions must be inside boundaries in "
			    + LookupMode.BOUNDED + " and "
			    + LookupMode.TOROIDAL + " modes.");
	}

	return findLineLocations(start, end, resultObject);
    }

    /**
     * Find locations along a line.
     * 
     * @see #findLineLocations(Int2D, Int2D, LocationsResult)
     * @param start
     *            start point
     * @param end
     *            end point
     * @return {@link LocationsResult}
     */
    public static LocationsResult findLineLocations(Int2D start, Int2D end) {
	return findLineLocations(start, end, new LocationsResult());
    }

    /**
     * Find locations along a line.
     * 
     * @see <a
     *      href=http://tech-algorithm.com/articles/drawing-line-using-bresenham
     *      -algorithm/>Drawing Line Using Bresenham Algorithm</a>
     * @param start
     *            start point
     * @param end
     *            end point
     * @param resultObject
     *            that will be reused
     * @return {@link LocationsResult} given {@code resultsObject} with results
     */
    public static LocationsResult findLineLocations(Int2D start, Int2D end,
	    LocationsResult resultObject) {
	resultObject.clear();

	int w = end.x - start.x;
	int h = end.y - start.y;
	int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
	if (w < 0) {
	    dx1 = -1;
	} else if (w > 0) {
	    dx1 = 1;
	}
	if (h < 0) {
	    dy1 = -1;
	} else if (h > 0) {
	    dy1 = 1;
	}
	if (w < 0) {
	    dx2 = -1;
	} else if (w > 0) {
	    dx2 = 1;
	}
	int longest = Math.abs(w);
	int shortest = Math.abs(h);
	if (!(longest > shortest)) {
	    longest = Math.abs(h);
	    shortest = Math.abs(w);
	    if (h < 0) {
		dy2 = -1;
	    } else if (h > 0) {
		dy2 = 1;
	    }
	    dx2 = 0;
	}
	int numerator = longest >> 1;
	int x = start.x;
	int y = start.y;
	for (int i = 0; i <= longest; i++) {
	    resultObject.add(x, y);
	    numerator += shortest;
	    if (!(numerator < longest)) {
		numerator -= longest;
		x += dx1;
		y += dy1;
	    } else {
		x += dx2;
		y += dy2;
	    }
	}

	return resultObject;
    }

    /**
     * @see #findMooreLocations(int, int, Double2D, double, LookupMode,
     *      LocationsResult)
     * @param width
     *            grid width
     * @param height
     *            grid height
     * @param center
     *            center of lookup
     * @param distance
     *            distance of lookup
     * @param mode
     *            {@link LookupMode}
     * @return {@link LocationsResult}
     */
    public static LocationsResult findMooreLocations(int width, int height,
	    Double2D center, double distance, LookupMode mode) {
	return findMooreLocations(width, height, center, distance, mode,
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
     * @param distance
     *            distance of lookup
     * @param mode
     *            {@link LookupMode}
     * @param resultsObject
     *            that will be reused
     * @return {@link LocationsResult} given {@code resultsObject} with results
     */
    public static LocationsResult findMooreLocations(int width, int height,
	    Double2D center, double distance, LookupMode mode,
	    LocationsResult resultsObject) {
	boolean bounded = mode == LookupMode.BOUNDED;
	double x = center.x;
	double y = center.y;

	// won't work for negative distances
	if (distance < 0) {
	    throw new IllegalArgumentException("Distance must be positive");
	}

	if (checkOutBounds(center, width, height) && bounded) {
	    throw new IllegalArgumentException("Invalid center position");
	}

	resultsObject.clear();

	// for toroidal environments the code will be different because of
	// wrapping around
	if (mode == LookupMode.TOROIDAL) {
	    // compute xmin and xmax for the neighborhood
	    int xmin = (int) (x - distance);
	    int xmax = (int) (x + distance);

	    // next: is xmax - xmin humongous? If so, no need to continue
	    // wrapping around
	    if (xmax - xmin >= width) // too wide, just use whole neighborhood
	    {
		xmin = 0;
		xmax = width - 1;
	    }

	    // compute ymin and ymax for the neighborhood
	    int ymin = (int) (y - distance);
	    int ymax = (int) (y + distance);

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
		    resultsObject.add(x_0, y_0);
		}
	    }
	}
	// not toroidal
	else {
	    // compute xmin and xmax for the neighborhood such that they are
	    // within boundaries
	    final int xmin = (int) ((x - distance >= 0) || !bounded ? x - distance : 0);
	    final int xmax = (int) ((x + distance < width - 1) || !bounded ? x
		    + distance : width - 1);
	    // compute ymin and ymax for the neighborhood such that they are
	    // within boundaries
	    final int ymin = (int) ((y - distance >= 0) || !bounded ? y - distance : 0);
	    final int ymax = (int) ((y + distance < height - 1) || !bounded ? y
		    + distance : height - 1);
	    for (int x0 = xmin; x0 <= xmax; x0++) {
		for (int y0 = ymin; y0 <= ymax; y0++) {
		    resultsObject.add(x0, y0);
		}
	    }
	}

	return resultsObject;
    }

    private static boolean checkOutBounds(Int2D pos, int width, int height) {
	return pos.x < 0 || pos.x >= width || pos.y < 0 || pos.y >= height;
    }

    private static boolean checkOutBounds(Double2D pos, int width, int height) {
	return pos.x < 0 || pos.x >= width || pos.y < 0 || pos.y >= height;
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
     * 
     * @param grid
     * @param resultObject
     * @return result object with added values from contained locations
     */
    private static DoubleNeighborsResult obtainValuesAtLocations(
	    DoubleGrid2D grid, DoubleNeighborsResult resultObject) {
	LocationsResult locationsResult = resultObject.locations;
	resultObject.values.clear();

	for (int i = 0; i < locationsResult.xPos.numObjs; i++) {
	    double val = grid.field[locationsResult.xPos.objs[i]][locationsResult.yPos.objs[i]];
	    resultObject.values.add(val);
	}
	return resultObject;
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
		mode, resultObject.locations);
	return obtainValuesAtLocations(grid, resultObject);
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
	private final IntBag xPos;
	private final IntBag yPos;

	public LocationsResult() {
	    xPos = new IntBag();
	    yPos = new IntBag();
	}

	public LocationsResult(IntBag xPos, IntBag yPos) {
	    this.xPos = xPos;
	    this.yPos = yPos;
	}

	private void clear() {
	    xPos.clear();
	    yPos.clear();
	}

	private void add(int x, int y) {
	    xPos.add(x);
	    yPos.add(y);
	}

	public int getX(int index) {
	    return xPos.get(index);
	}

	public int getY(int index) {
	    return yPos.get(index);
	}

	public int getSize() {
	    return xPos.numObjs;
	}

	@Override
	public String toString() {
	    return "LocationsResult [xPos=" + Arrays.toString(xPos.objs)
		    + ", yPos=" + Arrays.toString(yPos.objs) + "]";
	}
    }

    /**
     * Contains a {@link LocationsResult} and values from these locations.
     * 
     * @author cmeyer
     * 
     */
    public static class DoubleNeighborsResult {
	private final LocationsResult locations;
	private final DoubleBag values;

	public DoubleNeighborsResult() {
	    this.locations = new LocationsResult();
	    this.values = new DoubleBag();
	}

	public DoubleNeighborsResult(LocationsResult radialLocationsResult,
		DoubleBag values) {
	    this.locations = radialLocationsResult;
	    this.values = values;
	}

	public LocationsResult getLocations() {
	    return locations;
	}

	public double getValue(int index) {
	    return values.get(index);
	}

	@Override
	public String toString() {
	    return "DoubleNeighborsResult [locationsResult=" + locations
		    + ", values=" + Arrays.toString(values.objs) + "]";
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
