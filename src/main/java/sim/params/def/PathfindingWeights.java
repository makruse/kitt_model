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

    private static final double DEFAULT_WEIGHT_FOOD = 1;
    private static final double DEFAULT_WEIGHT_RISK = 2;

    public PathfindingWeights() {
	super(MapType.class);
	put(MapType.FOOD, DEFAULT_WEIGHT_FOOD);
	put(MapType.RISK, DEFAULT_WEIGHT_RISK);
    }

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }
}
