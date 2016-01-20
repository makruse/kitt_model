package sim.engine;

import java.nio.file.*;

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
    public static final Path DEFAULT_INPUT_DIR = Paths.get("parameters");
    /** Default directory to write simulation output. */
    public static final Path DEFAULT_OUTPUT_DIR = Paths.get("output");

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

    /**
     * Sets the path to write simulation output.
     *
     * @param outputPath
     *            the new output path
     */
    public abstract void setOutputPath(Path outputPath);
}
