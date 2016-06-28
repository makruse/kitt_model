package de.zmt.params;

import java.util.Collection;

/**
 * Parameters node object that contains a collection of other parameter
 * definitions.
 * 
 * @author mey
 *
 */
public interface ParamsNode extends ParamDefinition {
    /** @return All {@link ParamDefinition}s held by this object. */
    Collection<? extends ParamDefinition> getDefinitions();

    /**
     * Adds a {@link ParamDefinition} to this parameters object. Default
     * behavior is to throw an {@link UnsupportedOperationException}.
     * 
     * @param definition
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     * @throws UnsupportedOperationException
     *             if no definitions can be added to this node
     * @throws IllegalArgumentException
     *             if some property of the definition prevents it from being
     *             added
     */
    default boolean addDefinition(ParamDefinition definition) {
        throw new UnsupportedOperationException(definition + " cannot be added to " + this);
    }

    /**
     * Removes a {@link ParamDefinition} from this parameters object. Default
     * behavior is to throw an {@link UnsupportedOperationException}.
     * 
     * @param definition
     * @return <code>true</code> if removal succeeded
     * @throws UnsupportedOperationException
     *             if removal is not supported by this node
     */
    default boolean removeDefinition(ParamDefinition definition) {
        return false;
    }
}
