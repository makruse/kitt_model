package de.zmt.params.def;

import java.io.Serializable;

import de.zmt.params.Params;
import sim.portrayal.inspector.ParamsInspector;

/**
 * A set of parameter definitions which is stored within a {@link Params}
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
     * 
     * @return the title
     */
    String getTitle();

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