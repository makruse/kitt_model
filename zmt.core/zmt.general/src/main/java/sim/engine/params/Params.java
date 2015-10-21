package sim.engine.params;

import java.io.Serializable;
import java.util.Collection;

import sim.engine.params.def.ParamDefinition;

public interface Params extends Serializable {
    /** @return All {@link ParamDefinition}s held by this object. */
    Collection<? extends ParamDefinition> getDefinitions();
}
