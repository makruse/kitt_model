package de.zmt.params.def;

import java.util.Map;

import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * A {@link ParamDefinition} using a map to store its parameters.
 *
 * @author mey
 * @param <K>
 *            the type of keys maintained by the map
 * @param <V>
 *            the type of values mapped by the map
 * @param <T>
 *            the type of the map
 */
public class MapParamDefinition<K, V, T extends Map<K, V>> extends BaseParamDefinition implements ProvidesInspector {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The map this definition consists of. */
    private final T map;

    /**
     * Constructs a new {@link MapParamDefinition} consisting of the given map.
     * 
     * @param map
     *            the map this definition consists of
     */
    public MapParamDefinition(T map) {
	super();
	this.map = map;
    }

    /**
     * Gets the map this definition consists of.
     *
     * @return the map this definition consists of
     */
    protected final T getMap() {
	return map;
    }

    /**
     * Returns a {@link MapAccessor} for the map.
     *
     * @return the definition accessor
     */
    @Override
    public DefinitionAccessor<?> accessor() {
	return new MapAccessor<>(map);
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	Inspector inspector = Inspector.getInspector(getMap(), state, name);
	inspector.setTitle(getTitle());
	return inspector;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[" + map + "]";
    }
}
