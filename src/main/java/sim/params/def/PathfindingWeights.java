package sim.params.def;

import java.util.EnumMap;
import java.util.Map;

import de.zmt.pathfinding.MapType;

/**
 * {@link Map} containing weight factors to be used in pathfinding.
 * 
 * @author mey
 *
 */
class PathfindingWeights extends EnumMap<MapType, Double> {
    private static final long serialVersionUID = 1L;

    public PathfindingWeights() {
	super(MapType.class);
	for (MapType type : MapType.values()) {
	    put(type, type.getDefaultWeight());
	}
    }

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }
}
