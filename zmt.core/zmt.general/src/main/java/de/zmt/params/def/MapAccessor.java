package de.zmt.params.def;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.zmt.params.def.NotAutomatable.IllegalAutomationException;

/**
 * Wrapper implementation of {@link DefinitionAccessor}. Operations are
 * delegated to an existing {@link Map} object.
 * 
 * @author mey
 *
 * @param <K>
 *            the type of key used in the underlying map
 * @param <V>
 *            the type of parameter values
 */
public class MapAccessor<K extends Serializable, V> implements DefinitionAccessor<V> {
    private static final String MISSING_KEY_MESSAGE_FORMAT_STRING = "%s is not contained in underlying map: %s";

    /** The wrapped map. */
    private final Map<K, V> map;
    private final Set<K> notAutomatableKeys;

    /**
     * Constructs a new {@link MapAccessor} wrapping the given map.
     * 
     * @param map
     */
    public MapAccessor(Map<K, V> map) {
	super();
	this.map = map;
	this.notAutomatableKeys = Collections.emptySet();
    }

    public MapAccessor(Map<K, V> map, Set<K> notAutomatableKeys) {
	super();
	this.map = map;
	this.notAutomatableKeys = notAutomatableKeys;
    }

    @Override
    public Set<K> identifiers() {
	HashSet<K> returnSet = new HashSet<>(map.keySet());
	returnSet.removeAll(notAutomatableKeys);
	return Collections.unmodifiableSet(returnSet);
    }

    /**
     * @throws IllegalArgumentException
     *             {@inheritDoc}
     * @throws IllegalAutomationException
     *             {@inheritDoc}
     */
    @Override
    public V get(Object identifier) {
	checkIfAutomatable(identifier);
	if (map.containsKey(identifier)) {
	    return map.get(identifier);
	}
	throw new IllegalArgumentException(String.format(MISSING_KEY_MESSAGE_FORMAT_STRING, identifier, map));
    }

    /**
     * @throws ClassCastException
     *             {@inheritDoc}
     * @throws IllegalArgumentException
     *             {@inheritDoc}
     * @throws IllegalAutomationException
     *             {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public V set(Object identifier, Object value) {
	checkIfAutomatable(identifier);
	if (map.containsKey(identifier)) {
	    // casting key is safe due to containsKey check
	    return map.put((K) identifier, (V) value);
	}

	throw new IllegalArgumentException(String.format(MISSING_KEY_MESSAGE_FORMAT_STRING, identifier, map));
    }

    /**
     * @param identifier
     *            the identifier to check
     * @throws IllegalAutomationException
     *             if not automatable
     */
    private void checkIfAutomatable(Object identifier) {
        if (notAutomatableKeys.contains(identifier)) {
            throw new IllegalAutomationException("Automation not allowed for identifier: " + identifier);
        }
    }
}
