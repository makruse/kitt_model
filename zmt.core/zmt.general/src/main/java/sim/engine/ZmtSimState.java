package sim.engine;

import java.io.File;

import sim.engine.SimState;

/**
 * {@link sim.engine.SimState}s which can be automated need to extend this
 * class.
 * 
 * @author cmeyer
 * 
 */
public abstract class ZmtSimState extends SimState implements Parameterizable {
    private static final long serialVersionUID = 1L;

    /** Default directory for loading the parameters from. */
    public static final String DEFAULT_INPUT_DIR = "parameters"
            + File.separator;
    /** Default directory to dump output to. */
    public static final String DEFAULT_OUTPUT_DIR = "results" + File.separator;

    public ZmtSimState(long seed) {
	super(seed);
    }

    public abstract void setOutputPath(String outputPath);
}
