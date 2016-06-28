package de.zmt.pathfinding;

/**
 * A notifier for changes in a {@link PathfindingMap}.
 * 
 * @author mey
 *
 */
public interface MapChangeNotifier {
    /**
     * Adds a listener that is notified when the map changes.
     * 
     * @param listener
     */
    void addListener(DynamicMap listener);

    /**
     * Removes a previously added listener.
     * 
     * @param listener
     */
    void removeListener(Object listener);

    /**
     * Sets the update mode specifying how map changes are propagated.
     * 
     * @param mode
     *            the {@link UpdateMode} specifying how the changes are
     *            propagated
     */
    void setUpdateMode(UpdateMode mode);

    /**
     * Mode specifying update propagation.
     * 
     * @author mey
     *
     */
    public enum UpdateMode {
        /**
         * Changes are propagated when they are first requested. Listeners are
         * marked dirty on changes. (Default)
         */
        LAZY,
        /**
         * Changes are immediately propagated when they happen. Update of
         * listeners is done immediately.
         */
        EAGER;

        /**
         * Returns the default update mode.
         * 
         * @return the default mode
         */
        public static UpdateMode getDefault() {
            return LAZY;
        }
    }

}