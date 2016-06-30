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

    /**
     * An identifier wrapping a key object to get a value from a
     * {@link DefinitionAccessor}.
     * 
     * @author mey
     *
     * @param <T>
     *            the type of wrapped object
     */
    public static interface Identifier<T> {
        /**
         * Returns the object wrapped by this identifier
         * 
         * @return the object wrapped by this identifier
         */
        T get();

        /**
         * Creates an identifier containing the giving object with a
         * {@link #toString()} method displaying the object.
         * 
         * @param object
         *            the object to wrap
         * @return the identifier wrapping the object
         */
        public static <T> Identifier<T> create(T object) {
            return new Identifier<T>() {

                @Override
                public T get() {
                    return object;
                }

                @Override
                public String toString() {
                    return "[" + object + "]";
                }
            };
        }
    }
}
