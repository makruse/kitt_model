package de.zmt.pathfinding;

import java.util.*;

import de.zmt.pathfinding.filter.ConvolveOp;
import sim.field.grid.ObjectGrid2D;
import sim.util.Int2D;

/**
 * Class for maintaining a lazy updating map. A boolean grid will be used to
 * mark locations dirty that are in need of update, e.g. data this map depends
 * on has changed. During initialization, all positions are updated.
 * <p>
 * Extends for both directions can be used when a change in one location of the
 * underlying data will also affect neighbor locations in this map. A useful
 * example would be if this map is derived with a {@link ConvolveOp} that takes
 * neighbors into account.
 * <p>
 * To trigger updating dirty locations, either {@link #forceUpdateAll()},
 * {@link #updateIfDirty(int, int)} or {@link #updateIfDirtyAll()} must be
 * called.
 * 
 * @author mey
 *
 */
public abstract class LazyUpdatingMap extends BasicMapChangeNotifier implements PathfindingMap, MapUpdateHandler {
    private static final long serialVersionUID = 1L;

    /** Cache of {@link Int2D} locations used in {@link #dirtySet}. */
    private static ObjectGrid2D locationsCache = new ObjectGrid2D(0, 0);

    private final int width;
    private final int height;
    private int xExtend;
    private int yExtend;

    /** Locations that have been modified and need to be updated. */
    private final Set<Int2D> dirtySet = new HashSet<>();

    /**
     * Constructs a new lazy updating map with given dimensions and extents.
     * 
     * @param width
     * @param height
     * @param xExtend
     *            horizontal extend from position when marking dirty
     * @param yExtend
     *            vertical extend from position when marking dirty
     */
    public LazyUpdatingMap(int width, int height, int xExtend, int yExtend) {
	this.width = width;
	this.height = height;
	this.xExtend = xExtend;
	this.yExtend = yExtend;
	adjustCacheSize(width, height);
    }

    /**
     * Adjust the cache if needed, to fit given dimensions.
     * 
     * @param width
     * @param height
     */
    private static synchronized void adjustCacheSize(int width, int height) {
	// locations cache is sufficient: do nothing
	if (locationsCache != null && locationsCache.getWidth() >= width && locationsCache.getHeight() >= height) {
	    return;
	}

	// create new grid that fits requirements
	ObjectGrid2D newCache = new ObjectGrid2D(Math.max(width, locationsCache.getWidth()),
		Math.max(height, locationsCache.getHeight()));

	for (int x = 0; x < newCache.getWidth(); x++) {
	    for (int y = 0; y < newCache.getHeight(); y++) {
		Object location;

		// already in old cache: copy reference
		if (x < locationsCache.getWidth() && y < locationsCache.getHeight()) {
		    location = locationsCache.get(x, y);
		}
		// not in old cache: create new
		else {
		    location = new Int2D(x, y);
		}
		newCache.set(x, y, location);
	    }
	}

	/*
	 * Assigning a reference is an atomic operation. There is no other write
	 * operation done on the cache. Concurrent read operations on shared
	 * data are safe which makes this class suitable for multithreading.
	 */
	locationsCache = newCache;
    }

    /**
     * Constructs a new lazy updating map with given dimensions and extends set
     * to zero.
     * 
     * @param width
     * @param height
     */
    public LazyUpdatingMap(int width, int height) {
	this(width, height, 0, 0);
    }

    @Override
    public void markDirty(int x, int y) {
	int xMin = Math.max(0, x - xExtend);
	int xMax = Math.min(getWidth(), x + xExtend + 1);
	int yMin = Math.max(0, y - yExtend);
	int yMax = Math.min(getHeight(), y + xExtend + 1);

	for (int i = xMin; i < xMax; i++) {
	    for (int j = yMin; j < yMax; j++) {
		dirtySet.add((Int2D) locationsCache.get(i, j));
	    }
	}
    }

    @Override
    public final void forceUpdateAll() {
	for (int x = 0; x < getWidth(); x++) {
	    for (int y = 0; y < getHeight(); y++) {
		update(x, y);
		Int2D location = (Int2D) locationsCache.get(x, y);
		dirtySet.remove(location);
		notifyListeners(location.x, location.y);
	    }
	}
    }

    @Override
    public final void updateIfDirty(int x, int y) {
	// if requested value is dated: it needs to be updated
	Int2D location = (Int2D) locationsCache.get(x, y);
	if (dirtySet.contains(location)) {
	    update(x, y);
	    dirtySet.remove(location);
	    notifyListeners(location.x, location.y);
	}
    }

    @Override
    public final void updateIfDirtyAll() {
	for (Iterator<Int2D> iterator = dirtySet.iterator(); iterator.hasNext();) {
	    Int2D location = iterator.next();

	    update(location.x, location.y);
	    iterator.remove();
	    notifyListeners(location.x, location.y);
	}
    }

    public void setxExtend(int xExtend) {
	this.xExtend = xExtend;
    }

    public void setyExtend(int yExtend) {
	this.yExtend = yExtend;
    }

    /**
     * Updates the value at the given position. This will be called from
     * {@link #updateIfDirty(int, int)} when marked dirty.
     * 
     * @param x
     * @param y
     */
    protected abstract void update(int x, int y);

    @Override
    public int getWidth() {
	return width;
    }

    @Override
    public int getHeight() {
	return height;
    }

    @Override
    public String toString() {
	return getClass().getName() + "[width=" + getWidth() + ", height=" + getHeight() + "]";
    }

}