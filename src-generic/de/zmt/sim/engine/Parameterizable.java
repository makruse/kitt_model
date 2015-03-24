package de.zmt.sim.engine;

import de.zmt.sim.engine.params.AbstractParams;

/**
 * {@link sim.engine.SimState}s providing {@link AbstractParams} need to implement
 * this interface.
 * @author cmeyer
 *
 */
public interface Parameterizable {

    AbstractParams getParams();

    void setParams(AbstractParams params);
}