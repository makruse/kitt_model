package de.zmt.pathfinding;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.zmt.pathfinding.filter.ConvolveOp;
import sim.util.Int2D;
import sim.util.Int2DCache;

/**
 * Abstract implementation of a {@link DynamicMap}. Locations can be marked
 * dirty to trigger an update if such a location is requested.
 * <p>
 * Extends for both directions can be used when a change in one location of the
 * underlying data will also affect neighbor locations in this map. A useful
 * example would be if this map is derived with a {@link ConvolveOp} that takes
 * neighbors into account.
 * 
 * @author mey
 *
 */
abstract class AbstractDynamicMap extends BasicMapChangeNotifier implements NamedMap, DynamicMap {
    private static final long serialVersionUID = 1L;

    /** Width of map. */
    private final int width;
    /** Height of map. */
    private final int height;
    /** Horizontal extend when marking dirty. */
    private final int xExtend;
    /** Vertical extend when marking dirty. */
    private final int yExtend;

    /** Locations that have been modified and need to be updated. */
    private final Set<Int2D> dirtySet = new HashSet<>();
    /** The name of this map. */
    private String name = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

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
    public AbstractDynamicMap(int width, int height, int xExtend, int yExtend) {
        this.width = width;
        this.height = height;
        this.xExtend = xExtend;
        this.yExtend = yExtend;
        Int2DCache.adjustCacheSize(width, height);
    }

    /**
     * Constructs a new lazy updating map with given dimensions and extends set
     * to zero.
     * 
     * @param width
     * @param height
     */
    public AbstractDynamicMap(int width, int height) {
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
                dirtySet.add(Int2DCache.get(i, j));
            }
        }
    }

    @Override
    public void forceUpdate(int x, int y) {
        updateCleanNotify(Int2DCache.get(x, y));
    }

    @Override
    public final void forceUpdateAll() {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Int2D location = Int2DCache.get(x, y);
                dirtySet.remove(location);
                update(location.x, location.y);
            }
        }
        notifyListenersAll();
    }

    @Override
    public void updateIfDirty(int x, int y) {
        // if requested value is dated: it needs to be updated
        Int2D location = Int2DCache.get(x, y);
        if (dirtySet.contains(location)) {
            updateCleanNotify(location);
        }
    }

    @Override
    public final void updateIfDirtyAll() {
        for (Iterator<Int2D> iterator = dirtySet.iterator(); iterator.hasNext();) {
            Int2D location = iterator.next();
            iterator.remove();
            update(location.x, location.y);
            notifyListeners(location.x, location.y);
        }
    }

    /**
     * Removes dirty flag, notify listeners and calls update.
     * 
     * @param location
     */
    private void updateCleanNotify(Int2D location) {
        dirtySet.remove(location);
        update(location.x, location.y);
        notifyListeners(location.x, location.y);
    }

    /**
     * @param x
     * @param y
     * @return <code>true</code> if location is marked dirty
     */
    boolean isDirty(int x, int y) {
        return dirtySet.contains(Int2DCache.get(x, y));
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
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name of this pathfinding map
     */
    public void setName(String name) {
        this.name = name;
    }

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
        return getName();
    }

}