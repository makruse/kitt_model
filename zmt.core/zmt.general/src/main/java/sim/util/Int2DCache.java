package sim.util;

import sim.field.grid.ObjectGrid2D;

/**
 * Cache for storing {@link Int2D} objects. The default size of the cache is
 * (0,0). Before getting any objects the cache size needs to be adjusted
 * accordingly.
 * 
 * @author mey
 *
 */
public final class Int2DCache {
    /** The grid to store the cached {@link Int2D} objects by their position. */
    private static ObjectGrid2D cache = new ObjectGrid2D(0, 0);

    private Int2DCache() {

    }

    /**
     * Adjust the cache if needed, to fit given dimensions.
     * <p>
     * <b>NOTE:</b> The maximum obtainable object will be
     * {@code (width-1, height-1)}.
     * 
     * @param width
     *            the width of the cache
     * @param height
     *            the height of the cache
     */
    public static synchronized void adjustCacheSize(int width, int height) {
        // locations cache is sufficient: do nothing
        if (cache != null && cache.getWidth() >= width && cache.getHeight() >= height) {
            return;
        }

        // create new grid that fits requirements
        ObjectGrid2D newCache = new ObjectGrid2D(Math.max(width, cache.getWidth()),
                Math.max(height, cache.getHeight()));

        for (int x = 0; x < newCache.getWidth(); x++) {
            for (int y = 0; y < newCache.getHeight(); y++) {
                Object location;

                // already in old cache: copy reference
                if (x < cache.getWidth() && y < cache.getHeight()) {
                    location = cache.get(x, y);
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
        cache = newCache;
    }

    /**
     * Gets {@link Int2D} object for given location from cache.
     * 
     * @param x
     *            the x-coordinate for the location
     * @param y
     *            the y-coordinate for the location
     * @return {@link Int2D} object for given location
     * @throws NullPointerException
     *             if the desired object is not stored
     * 
     */
    public static Int2D get(int x, int y) {
        return (Int2D) cache.get(x, y);
    }

    public final static int getWidth() {
        return cache.getWidth();
    }

    public final static int getHeight() {
        return cache.getHeight();
    }

}
