package de.zmt.sim.engine;

import sim.engine.SimState;
import de.zmt.sim.engine.params.SimParams;

/**
 * Super class for {@link SimState} of ZMT simulations.
 * 
 * @author cmeyer
 *
 * @param <T>
 */
public class BaseZmtSimState<T extends SimParams> extends ZmtSimState {
    private static final long serialVersionUID = 1L;

    /** path for writing output to */
    protected String outputPath = ZmtSimState.DEFAULT_OUTPUT_DIR;
    /** Simulation parameters */
    private T params;

    public BaseZmtSimState() {
	// seed is set from parameters in start
	super(0);
    }

    @Override
    public T getParams() {
	return params;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setParams(SimParams params) {
	this.params = (T) params;
    }

    /**
     * Set directory where output files are written to.
     * 
     * @param outputPath
     */
    @Override
    public void setOutputPath(String outputPath) {
	this.outputPath = outputPath;
    }

    @Override
    public void start() {
	super.start();

	if (params == null) {
	    throw new IllegalStateException("params must be set before start!");
	}
	setSeed(params.getSeed());
    }

}
