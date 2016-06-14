package de.zmt.params.def;

import java.util.EnumMap;
import java.util.Map;

import de.zmt.pathfinding.MapType;

/**
 * {@link Map} containing weight factors to be used in pathfinding.
 * 
 * @author mey
 *
 */
class PathfindingWeights extends MapParamDefinition<MapType, Double, Map<MapType, Double>> {
    private static final long serialVersionUID = 1L;

    public PathfindingWeights() {
	super(new EnumMap<>(MapType.class));
	for (MapType type : MapType.values()) {
	    getMap().put(type, type.getDefaultWeight());
	}
    }

    public Double get(MapType key) {
	return getMap().get(key);
    }
}
