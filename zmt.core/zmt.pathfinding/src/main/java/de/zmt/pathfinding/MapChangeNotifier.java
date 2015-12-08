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
    void addListener(MapChangeListener listener);

    /**
     * Removes a previously added listener.
     * 
     * @param listener
     */
    void removeListener(Object listener);

}