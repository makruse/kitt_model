package de.zmt.params.def;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

import de.zmt.pathfinding.MapType;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * {@link Map} containing weight factors to be used in pathfinding.
 * 
 * @author mey
 *
 */
class PathfindingWeights implements Serializable, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    private final Map<MapType, Double> map = new EnumMap<>(MapType.class);

    public PathfindingWeights() {
	super();
	for (MapType type : MapType.values()) {
	    map.put(type, type.getDefaultWeight());
	}
    }

    public Double get(MapType key) {
	return map.get(key);
    }

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	Inspector inspector = Inspector.getInspector(map, state, name);
	inspector.setTitle(getClass().getSimpleName());
	return inspector;
    }
}
