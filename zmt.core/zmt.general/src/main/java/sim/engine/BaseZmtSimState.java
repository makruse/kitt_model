package sim.engine;

import sim.engine.params.SimParams;

/**
 * Super class for {@link SimState} of ZMT simulations.
 * 
 * @author cmeyer
 *
 * @param <T>
 *            type of {@code SimParams} this class uses
 */
// TODO override setSeed to not create new random
// need to rearrange packages in order to do that
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

    /** Sets seed from parameters. */
    @Override
    public void start() {
	super.start();

	if (params == null) {
	    throw new IllegalStateException("params must be set before start!");
	}
	setSeed(params.getSeed());
    }

    /**
     * Fix not to create a new random number generator every time the seed is
     * set. Leads to problems with resources not reinitialized every time the
     * simulation is restarted.
     */
    @Override
    public void setSeed(long seed) {
	// force to 32 bits since that's what MTF will be using anyway
	seed = (int) seed;
        random.setSeed(seed);
        this.seed = seed;
    }

}
