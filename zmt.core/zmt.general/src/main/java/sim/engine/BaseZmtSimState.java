package sim.engine;

import java.nio.file.Path;

import de.zmt.params.SimParams;

/**
 * Super class for {@link SimState} of ZMT simulations.
 * 
 * @author mey
 *
 * @param <T>
 *            type of {@code SimParams} this class uses
 */
public abstract class BaseZmtSimState<T extends SimParams> extends ZmtSimState {
    private static final long serialVersionUID = 1L;

    /** path for writing output to */
    private Path outputPath = ZmtSimState.DEFAULT_OUTPUT_DIR;
    /** Simulation parameters */
    private T params;

    public BaseZmtSimState() {
        // seed is set from parameters in start
        super(0);
    }

    protected Path getOutputPath() {
        return outputPath;
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
     * @param outputDir
     */
    @Override
    public void setOutputPath(Path outputDir) {
        this.outputPath = outputDir;
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

    @Override
    public abstract Class<? extends T> getParamsClass();
}
