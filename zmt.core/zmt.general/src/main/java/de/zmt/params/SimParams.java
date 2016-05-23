package de.zmt.params;

import java.io.Serializable;
import java.util.Collection;

import de.zmt.params.def.OptionalParamDefinition;
import de.zmt.params.def.ParamDefinition;

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
     * A {@link Collection} of all {@link ParamDefinition} objects, including
     * optional and non-optional ones.<br>
     * <b>NOTE:</b> The iteration order of the returned collection appears
     * within the GUI on their inspectors.
     */
    @Override
    Collection<? extends ParamDefinition> getDefinitions();

    /**
     * Adds an {@link OptionalParamDefinition} to this parameters object.
     * 
     * @param optionalDef
     * @return @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    boolean addOptionalDefinition(OptionalParamDefinition optionalDef);

    /**
     * Removes an {@link OptionalParamDefinition} from this parameters object.
     * 
     * @param optionalDef
     * @return true if removal succeeded
     */
    boolean removeOptionalDefinition(OptionalParamDefinition optionalDef);

    /** @return Seed value from this Params object. */
    long getSeed();
}