package sim.engine.params;

import java.io.Serializable;
import java.util.Collection;

import sim.engine.params.def.*;

/**
 * Simulation parameters that can contain optional definitions and a value for
 * the seed of the random number generator used.
 * 
 * @author mey
 *
 */
public interface SimParams extends Serializable, Params {
    public static final String DEFAULT_FILENAME = "params.xml";

    /**
     * 
     * @param type
     * @return collection of all parameter definitions that match given type
     */
    <T extends ParamDefinition> Collection<T> getDefinitions(Class<T> type);

    /**
     * Remove an {@link OptionalParamDefinition}.
     * 
     * @param optionalDef
     * @return true if removal succeeded
     */
    boolean removeOptionalDefinition(OptionalParamDefinition optionalDef);

    /** @return Seed value from this Params object. */
    long getSeed();
}