package de.zmt.pathfinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder-like class to make structural changes to a {@link DerivedFlowMap}
 * and trigger the expensive update only once.
 * 
 * @see DerivedFlowMap#changeStructure(MapChanger)
 * @author mey
 * @param <T>
 *            the type of pathfinding maps used
 *
 */
public class MapChanger<T extends PathfindingMap> {
    final Collection<T> mapsToAdd = new ArrayList<>();
    final Map<T, Double> weightsToPut = new HashMap<>();
    final Collection<T> mapsToRemove = new ArrayList<>();

    /**
     * Adds a map to the changer.
     * 
     * @param map
     *            the map to add
     * @return this object
     */
    public MapChanger<T> addMap(T map) {
        return addMap(map, DerivedFlowMap.NEUTRAL_WEIGHT);
    }

    /**
     * Adds a map to the changer and associate it with given weight.
     * 
     * @param map
     *            the map to add
     * @param weight
     *            the weight to associate it with
     * @return this object
     */
    public MapChanger<T> addMap(T map, double weight) {
        weightsToPut.put(map, weight);
        mapsToAdd.add(map);
        return this;
    }

    /**
     * Adds a map to be removed to the changer.
     * 
     * @param map
     *            the map to be removed
     * @return this object
     */
    public MapChanger<T> removeMap(T map) {
        mapsToRemove.add(map);
        return this;
    }

    /**
     * Sets a weight association to the changer.
     * 
     * @param map
     * @param weight
     * @return this object
     */
    public MapChanger<T> setWeight(T map, double weight) {
        weightsToPut.put(map, weight);
        return this;
    }
}