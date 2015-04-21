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
public class EncapsulatedMap<K, V> implements Propertied, Serializable {
    private static final long serialVersionUID = 1L;

    protected final Map<K, V> map;

    public EncapsulatedMap() {
	// use linked hash map to maintain key insertion order
	map = new LinkedHashMap<K, V>();
    }

    public EncapsulatedMap(Map<K, V> map) {
	this.map = map;
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