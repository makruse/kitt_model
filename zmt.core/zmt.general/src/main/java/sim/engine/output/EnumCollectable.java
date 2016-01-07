package sim.engine.output;

import java.util.*;

import sim.util.Propertied;
import sim.util.Properties;

/**
 * {@link Collectable} storing values for enum constants. Headers are derived
 * from enum constants' names.
 * 
 * @author mey
 *
 * @param <K>
 *            enum type
 * @param <V>
 *            value type
 */
public abstract class EnumCollectable<K extends Enum<K>, V> implements ClearableCollectable<V>, Propertied {
    private static final long serialVersionUID = 1L;

    private final Map<K, V> data;
    private final Collection<String> headers;
    private final Set<K> usedConstants;

    /**
     * Constructs a new {@code EnumCollectable} using all enum constants in
     * given type.
     * 
     * @param enumType
     *            the used enum type
     * @param headersSuffix
     *            suffix applied to each header following the enum constant's
     *            name
     */
    public EnumCollectable(Class<K> enumType, String headersSuffix) {
	this(EnumSet.allOf(enumType), headersSuffix);
    }

    /**
     * Constructs a new {@code AbstractEnumCollectable} using all enum constants
     * in given type.
     * 
     * @param usedConstants
     *            the set of enum constants to be used in this
     *            {@code Collectable}
     * @param headersSuffix
     *            suffix applied to each header following the enum constant's
     *            name
     */
    public EnumCollectable(Set<K> usedConstants, String headersSuffix) {
	super();
	data = new EnumMap<>(usedConstants.iterator().next().getDeclaringClass());
	headers = createHeaders(usedConstants, headersSuffix);
	this.usedConstants = usedConstants;
	// set to initial values
	clear();
    }

    /**
     * Creates a header for each {@code usedConstant}, followed by
     * {@code headersSuffix}.
     * 
     * @param usedConstants
     * @param headersSuffix
     * @return headers named after constants and suffix
     */
    private static Collection<String> createHeaders(Collection<? extends Enum<?>> usedConstants, String headersSuffix) {
	Collection<String> headers = new ArrayList<>(usedConstants.size());
	for (Enum<?> constant : usedConstants) {
	    headers.add(constant.name() + headersSuffix);
	}
	return headers;
    }

    /**
     * Returns the value mapped to the given {@code enumConstant}.
     * 
     * @param enumConstant
     * @return value mapped to {@code enumConstant}
     */
    protected final V getValue(Object enumConstant) {
	return data.get(enumConstant);
    }

    /**
     * Associates given enum constant with the given value.
     * 
     * @param enumConstant
     * @param value
     * @return previous value mapped to {@code enumConstant}
     */
    protected final V putValue(K enumConstant, V value) {
	if (usedConstants.contains(enumConstant)) {
	    return data.put(enumConstant, value);
	}
	throw new IllegalArgumentException(enumConstant + " is not used in this " + Collectable.class.getSimpleName()
		+ ". Used constants are specified as " + usedConstants);
    }

    /**
     * @return value {@code data} is to be filled when calling {@link #clear()},
     *         default is <code>null</code>
     */
    protected V obtainInitialValue() {
	return null;
    }

    /** @return the constants used in */
    public Set<K> getUsedConstants() {
	return Collections.unmodifiableSet(usedConstants);
    }

    /**
     * Clears all data stored in this {@code Collectable}, i.e. the values for
     * every enum constant will be <code>null</code>.
     */
    @Override
    public void clear() {
	for (K constant : usedConstants) {
	    data.put(constant, obtainInitialValue());
	}
    }

    @Override
    public Iterable<String> obtainHeaders() {
	return headers;
    }

    @Override
    public Iterable<V> obtainValues() {
	return data.values();
    }

    @Override
    public int getSize() {
	return headers.size();
    }

    @Override
    public Properties properties() {
	return Properties.getProperties(data);
    }

    @Override
    public String toString() {
	return data.toString();
    }
}
