package de.zmt.params;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public abstract Map<K, V> getMap();

    /**
     * Gets the set of keys which are not automatable. An empty set is returned.
     * Implementing classes may override this method.
     * 
     * @return the set of keys which are not automatable
     */
    protected Set<K> getNotAutomatableKeys() {
        return Collections.emptySet();
    }

    /**
     * Returns a {@link MapAccessor} for the map.
     *
     * @return the definition accessor
     */
    @Override
    public DefinitionAccessor<?> accessor() {
        return new MapAccessor<>(getMap(), getNotAutomatableKeys());
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
        return super.toString() + "[" + getMap() + "]";
    }

    /**
     * {@link MapParamDefinition} default implementation .
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
        private final Map<K, V> map;

        /**
         * Constructs a new default {@link MapParamDefinition} consisting of an
         * empty {@link HashMap}.
         */
        public Default() {
            this(new HashMap<>());
        }

        /**
         * Constructs a new default {@link MapParamDefinition} consisting of the
         * given map.
         * 
         * @param map
         *            the map this definition will consist of
         */
        public Default(Map<K, V> map) {
            this.map = map;
        }

        @Override
        public Map<K, V> getMap() {
            return map;
        }
    }
}
