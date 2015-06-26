package de.zmt.sim.engine.params;

import java.io.Serializable;
import java.util.Collection;

import de.zmt.sim.engine.params.def.*;

public interface Params extends Serializable {
    public static final String DEFAULT_FILENAME = "params.xml";

    /** @return All {@link ParamDefinition}s held by this object. */
    Collection<? extends ParamDefinition> getDefinitions();

    /**
     * 
     * @param type
     * @return collection of all parameter definitions that match given type
     */
    <T extends ParamDefinition> Collection<T> getDefinitions(
	    Class<T> type);

    /**
     * Remove an {@link OptionalDefinition}.
     * 
     * @param optionalDef
     * @return true if removal succeeded
     */
    boolean removeOptionalDefinition(
	    OptionalParamDefinition optionalDef);

}