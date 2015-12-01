package sim.engine.params;

import java.io.Serializable;
import java.util.Collection;

import sim.engine.params.def.ParamDefinition;

/**
 * Generic parameters object that contains a collection of defintitions.
 * 
 * @author mey
 *
 */
public interface Params extends Serializable {
    /** @return All {@link ParamDefinition}s held by this object. */
    Collection<? extends ParamDefinition> getDefinitions();
}
