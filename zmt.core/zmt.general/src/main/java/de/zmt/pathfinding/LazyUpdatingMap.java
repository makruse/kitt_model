package de.zmt.pathfinding;

import de.zmt.pathfinding.filter.ConvolveOp;
import sim.field.grid.BooleanGrid;

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

    /** Locations that have been modified and need to be updated. */
    private final BooleanGrid dirtyGrid;
    private int xExtend;
    private int yExtend;

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
	this(new BooleanGrid(width, height), xExtend, yExtend);
    }

    /**
     * Constructs a new lazy updating map with given grid and extends.
     * 
     * @param dirty
     * @param xExtend
     * @param yExtend
     */
    public LazyUpdatingMap(BooleanGrid dirty, int xExtend, int yExtend) {
	super();
	this.dirtyGrid = dirty;
	this.xExtend = xExtend;
	this.yExtend = yExtend;
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
	int xMax = Math.min(dirtyGrid.getWidth(), x + xExtend + 1);
	int yMin = Math.max(0, y - yExtend);
	int yMax = Math.min(dirtyGrid.getHeight(), y + xExtend + 1);

	for (int i = xMin; i < xMax; i++) {
	    for (int j = yMin; j < yMax; j++) {
		dirtyGrid.set(i, j, true);
	    }
	}
    }

    @Override
    public final void forceUpdateAll() {
	for (int x = 0; x < dirtyGrid.getWidth(); x++) {
	    for (int y = 0; y < dirtyGrid.getHeight(); y++) {
		update(x, y);
		afterUpdate(x, y);
	    }
	}
    }

    @Override
    public final void updateIfDirty(int x, int y) {
	// if requested value is dated: it needs to be updated
	if (dirtyGrid.get(x, y)) {
	    update(x, y);
	    afterUpdate(x, y);
	}
    }

    /**
     * Mark location as clean and notify listeners.
     * 
     * @param x
     * @param y
     */
    private void afterUpdate(int x, int y) {
	// mark location as up-to-date
	dirtyGrid.set(x, y, false);
	notifyListeners(x, y);
    }

    @Override
    public final void updateIfDirtyAll() {
	for (int x = 0; x < dirtyGrid.getWidth(); x++) {
	    for (int y = 0; y < dirtyGrid.getHeight(); y++) {
		updateIfDirty(x, y);
	    }
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
	return dirtyGrid.getWidth();
    }

    @Override
    public int getHeight() {
	return dirtyGrid.getHeight();
    }

    @Override
    public String toString() {
	return getClass().getName() + "[width=" + getWidth() + ", height=" + getHeight() + "]";
    }

}