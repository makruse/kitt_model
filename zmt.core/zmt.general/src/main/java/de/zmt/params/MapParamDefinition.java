package de.zmt.params;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.zmt.params.accessor.DefinitionAccessor;
import de.zmt.params.accessor.MapAccessor;
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
 */
public abstract class MapParamDefinition<K, V> extends BaseParamDefinition implements ProvidesInspector {
    private static final long serialVersionUID = 1L;

    /**
     * Gets the map this definition consists of.
     *
     * @return the map this definition consists of
     */
    protected abstract Map<K, V> getMap();

    /**
     * Returns a {@link MapAccessor} for the map.
     *
     * @return the definition accessor
     */
    @Override
    public DefinitionAccessor<?> accessor() {
        return new MapAccessor<>(getMap());
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
        Inspector inspector = Inspector.getInspector(getMap(), state, name);
        inspector.setTitle(getTitle());
        return inspector;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMap() == null) ? 0 : getMap().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MapParamDefinition<?, ?> other = (MapParamDefinition<?, ?>) obj;
        if (getMap() == null) {
            if (other.getMap() != null) {
                return false;
            }
        } else if (!getMap().equals(other.getMap())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getMap() + "]";
    }

    /**
     * {@link MapParamDefinition} default implementation using a {@link HashMap}
     * .
     * 
     * @author mey
     *
     * @param <K>
     *            the type of keys maintained by the map
     * @param <V>
     *            the type of values mapped by the map
     */
    public static class Default<K, V> extends MapParamDefinition<K, V> {
        private static final long serialVersionUID = 1L;

        /** The map this definition consists of. */
        @XStreamImplicit
        private final HashMap<K, V> map = new HashMap<>();

        @Override
        protected Map<K, V> getMap() {
            return map;
        }
    }
}
