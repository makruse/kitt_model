package de.zmt.params.accessor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.zmt.params.accessor.NotAutomatable.IllegalAutomationException;

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
public class MapAccessor<K, V> implements DefinitionAccessor<V> {
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
    public Set<Identifier<K>> identifiers() {
        // wrap in identifiers
        return map.keySet().stream().filter(key -> !notAutomatableKeys.contains(key)).map(Identifier::create)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    /**
     * @throws IllegalArgumentException
     *             {@inheritDoc}
     * @throws IllegalAutomationException
     *             {@inheritDoc}
     */
    @Override
    public V get(Identifier<?> identifier) {
        return map.get(unwrapAndValidate(identifier));
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
    public V set(Identifier<?> identifier, Object value) {
        return map.put(unwrapAndValidate(identifier), (V) value);
    }

    /**
     * @param identifier
     *            the identifier to unwrap
     * @return the unwrapped key from identifier
     * @throws IllegalAutomationException
     *             if not automatable
     * @throws IllegalArgumentException
     *             if not in map
     */
    @SuppressWarnings("unchecked")
    private K unwrapAndValidate(Identifier<?> identifier) {
        Object key = identifier.get();
        if (notAutomatableKeys.contains(key)) {
            throw new IllegalAutomationException("Automation not allowed for identifier: " + key);
        }
        if (!map.containsKey(key)) {
            throw new IllegalArgumentException(String.format(MISSING_KEY_MESSAGE_FORMAT_STRING, key, map));
        }
        // casting key is safe due to containsKey check
        return (K) key;
    }
}
