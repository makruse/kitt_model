package de.zmt.sim.engine;

import de.zmt.sim.engine.params.Params;

/**
 * {@link sim.engine.SimState}s which can be automated need to implement this
 * interface. A non-argument constructor is needed as well.
 * 
 * @author cmeyer
 * 
 */
public interface Parameterizable {
    /**
     * Name for field in implementing classes specifying the associated
     * {@link AbstractParams} class, in order to provide it without
     * instantiation.
     * 
     * @see de.zmt.util.ParamUtil#obtainParamsClass(Class)
     */
    public static final String PARAMS_CLASS_FIELD_NAME = "PARAMS_CLASS";

    Params getParams();

    void setParams(Params params);

    /**
     * Set directory where output files are written to.
     * 
     * @param outputPath
     */
    void setOutputPath(String outputPath);
}