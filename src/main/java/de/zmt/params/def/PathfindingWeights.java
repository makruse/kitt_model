package de.zmt.params.def;

import java.util.EnumMap;
import java.util.Map;

import de.zmt.pathfinding.PathfindingMapType;

/**
 * {@link Map} containing weight factors to be used in pathfinding.
 * 
 * @author mey
 *
 */
class PathfindingWeights extends MapParamDefinition<PathfindingMapType, Double, Map<PathfindingMapType, Double>> {
    private static final long serialVersionUID = 1L;

    public PathfindingWeights() {
	super(new EnumMap<>(PathfindingMapType.class));
	for (PathfindingMapType type : PathfindingMapType.values()) {
	    getMap().put(type, type.getDefaultWeight());
	}
    }

    public Double get(PathfindingMapType key) {
	return getMap().get(key);
    }
}
