package de.zmt.sim.engine;

import de.zmt.sim.engine.params.SimParams;
import de.zmt.util.ParamsUtil;

public interface Parameterizable {

    /**
     * Name for field in implementing classes specifying the associated
     * {@link SimParams} class, in order to provide it without instantiation.
     * 
     * @see ParamsUtil#obtainParamsClass(Class)
     */
    static final String PARAMS_CLASS_FIELD_NAME = "PARAMS_CLASS";

    SimParams getParams();

    void setParams(SimParams params);

}