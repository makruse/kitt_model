package de.zmt.params.accessor;

import java.util.Set;

import de.zmt.params.ParamDefinition;
import de.zmt.params.accessor.NotAutomatable.IllegalAutomationException;

/**
 * An accessor for parameters in a {@link ParamDefinition}. Used to change
 * parameter values in an automated way without knowing the specific class.
 * 
 * @author mey
 * @param <V>
 *            the type of value contained
 *
 */
public interface DefinitionAccessor<V> {
    /**
     * Returns the set of identifiers associated with a value.
     * 
     * @return the set of identifiers associated
     */
    Set<? extends Identifier<?>> identifiers();

    /**
     * Gets the value the given identifier is pointing at.
     * 
     * @param identifier
     *            the identifier to get the value for
     * @return the value the given identifier is pointing at
     * @throws IllegalArgumentException
     *             if the given identifier is invalid or <code>null</code>
     * @throws IllegalAutomationException
     *             if the given identifier cannot be automated
     */
    V get(Identifier<?> identifier);

    /**
     * Sets a value pointed at by a given identifier with a given one.
     * 
     * @param identifier
     *            the identifier pointing to the value to be set
     * @param value
     *            the value the identifier is to point at
     * @return the previous value the identifier was pointing at
     * @throws IllegalArgumentException
     *             if the given identifier is invalid or <code>null</code>
     * @throws ClassCastException
     *             if given value's type does not match V
     * @throws IllegalAutomationException
     *             if the given identifier cannot be automated
     * 
     */
    V set(Identifier<?> identifier, Object value);

    public static interface Identifier<T> {
        T get();

        public static <T> Identifier<T> create(T object) {
            return () -> object;
        }
    }
}
