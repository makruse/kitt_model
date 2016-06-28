package de.zmt.params;

import java.io.Serializable;

import de.zmt.params.accessor.DefinitionAccessor;
import de.zmt.params.accessor.ReflectionAccessor;
import sim.portrayal.inspector.ParamsInspector;

/**
 * A set of parameter definitions which is stored within a {@link ParamsNode}
 * object.
 * 
 * @author mey
 *
 */
public interface ParamDefinition extends Serializable {
    /**
     * Title appearing in {@link ParamsInspector}'s tab list, also used in
     * automation to discriminate between objects if there are several of one
     * class.
     * <p>
     * The default implementation returns the {@link Class#getSimpleName()}.
     * 
     * @return the definition's title
     */
    default String getTitle() {
        return getClass().getSimpleName();
    }

    /**
     * Returns the {@link DefinitionAccessor} to access parameters via
     * automation. {@link ReflectionAccessor} is returned as a default.
     * Implementing classes can specify a different accessor.
     * 
     * @return the {@link DefinitionAccessor} for this definition
     */
    default DefinitionAccessor<?> accessor() {
        return new ReflectionAccessor(this);
    }
}