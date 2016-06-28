package de.zmt.params;

import java.util.Map;

import de.zmt.pathfinding.PathfindingMapType;

/**
 * {@link Map} containing weight factors to be used in pathfinding.
 * 
 * @author mey
 *
 */
class PathfindingWeights extends MapParamDefinition.Default<PathfindingMapType, Double> {
    private static final long serialVersionUID = 1L;

    public PathfindingWeights() {
        super();
        for (PathfindingMapType type : PathfindingMapType.values()) {
            getMap().put(type, type.getDefaultWeight());
        }
    }

    public Double get(PathfindingMapType key) {
        return getMap().get(key);
    }
}
