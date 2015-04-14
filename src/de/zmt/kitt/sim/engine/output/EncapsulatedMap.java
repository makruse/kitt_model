package de.zmt.kitt.sim.engine.output;

import java.io.Serializable;
import java.util.*;

import sim.util.*;
import sim.util.Properties;

/**
 * Class with encapsulated map delegating {@link Properties} and
 * {@link #toString()} method.
 * 
 * @author cmeyer
 * 
 * @param <K>
 *            map key type
 * @param <V>
 *            map value type
 */
class EncapsulatedMap<K, V> implements Propertied, Serializable {
    private static final long serialVersionUID = 1L;

    protected final Map<K, V> map;

    public EncapsulatedMap() {
	map = new HashMap<K, V>();
    }

    public EncapsulatedMap(int initialCapacity) {
	map = new HashMap<K, V>(initialCapacity);
    }

    @Override
    public Properties properties() {
	return Properties.getProperties(map);
    }

    @Override
    public String toString() {
	return map.toString();
    }
}