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
}
