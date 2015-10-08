package de.zmt.pathfinding;

import de.zmt.pathfinding.filter.ConvolveOp;
import de.zmt.sim.field.grid.BooleanGrid;

/**
 * Class for maintaining a lazy updating map. A boolean grid will be used to
 * mark cells dirty that are in need of update, e.g. data this map depends on
 * has changed. During initialization, all positions are refreshed.
 * <p>
 * Extends for both directions can be used when a change in one cell of the
 * underlying data will also affect neighbor cells in this map. This is useful
 * if this map is derived with a {@link ConvolveOp} that takes neighbors into
 * account.
 * 
 * @author mey
 *
 */
public abstract class LazyUpdatingMap implements PathfindingMap {

    /**
     * Cells that have been modified and need to be updated.
     */
    final BooleanGrid dirtyGrid;
    private final int xExtend;
    private final int yExtend;

    /**
     * Constructor called from child classes.
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
     * Constructor initializing both extends to zero.
     * 
     * @param width
     * @param height
     */
    public LazyUpdatingMap(int width, int height) {
	this(width, height, 0, 0);
    }

    /**
     * Refreshes all positions independent from being marked dirty. All dirty
     * flags are removed.
     */
    public void forceRefreshAll() {
	for (int x = 0; x < dirtyGrid.getWidth(); x++) {
	    for (int y = 0; y < dirtyGrid.getHeight(); y++) {
		refresh(x, y);
	    }
	}
    }

    /**
     * Mark the given position and extends as dirty.
     * 
     * @param x
     * @param y
     */
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

    /**
     * Refreshes the value at the given position if marked dirty.
     * 
     * @param x
     * @param y
     */
    public final void refreshIfDirty(int x, int y) {
	// requested value is dated, need to be refreshed
	if (dirtyGrid.get(x, y)) {
	    refresh(x, y);
	    // mark cell as up-to-date
	    dirtyGrid.set(x, y, false);
	}
    }

    /**
     * Refreshes any value marked dirty.
     * 
     * @see #refreshIfDirty(int, int)
     */
    public final void refreshIfDirtyAll() {
	for (int x = 0; x < dirtyGrid.getWidth(); x++) {
	    for (int y = 0; y < dirtyGrid.getHeight(); y++) {
		refreshIfDirty(x, y);
	    }
	}
    }

    /**
     * Refreshes the value at the given position. This will be called from
     * {@link #refreshIfDirty(int, int)} when marked dirty.
     * 
     * @see #refreshIfDirty(int, int)
     * 
     * @param x
     * @param y
     */
    protected abstract void refresh(int x, int y);

    @Override
    public int getWidth() {
	return dirtyGrid.getWidth();
    }

    @Override
    public int getHeight() {
	return dirtyGrid.getHeight();
    }
}