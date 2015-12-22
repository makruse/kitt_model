package sim.engine;

import java.io.File;

import sim.engine.SimState;
import sim.engine.output.Output;

/**
 * {@link sim.engine.SimState}s which can be automated need to extend this
 * class.
 * 
 * @author mey
 * 
 */
public abstract class ZmtSimState extends SimState implements Parameterizable {
    private static final long serialVersionUID = 1L;

    /** Default directory for loading the parameters from. */
    public static final String DEFAULT_INPUT_DIR = "parameters" + File.separator;
    /** Default directory to dump output to. */
    public static final String DEFAULT_OUTPUT_DIR = "results" + File.separator;

    public ZmtSimState(long seed) {
	super(seed);
    }

    /**
     * Gets the output object of this simulation. Defaults to <code>null</code>,
     * can be overridden in sub classes. The returned object can be inspected in
     * GUI if selected in menu.
     * 
     * @return the output object of this simulation
     */
    public Output getOutput() {
        return null;
    }

    public abstract void setOutputPath(String outputPath);
}
