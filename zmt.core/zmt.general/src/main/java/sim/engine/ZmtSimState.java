package sim.engine;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.zmt.output.Output;
import de.zmt.params.SimParams;

/**
 * {@link sim.engine.SimState}s which can be automated need to extend this
 * class.
 * 
 * @author mey
 * 
 */
public abstract class ZmtSimState extends SimState {
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

    /**
     * Returns the {@link SimParams} class used by this simulation. Implementing
     * classes need to specify this in order to create and load suitable
     * parameter objects.
     * <p>
     * {@code XStream} annotations are processed for this class and all which
     * are referenced.
     * 
     * @return the {@link SimParams} class used by this simulation
     */
    public abstract Class<? extends SimParams> getParamsClass();

    /**
     * Returns the {@link SimParams} object with the current configuration.
     * 
     * @return the {@link SimParams} with the current configuration
     */
    public abstract SimParams getParams();

    /**
     * Sets the current configuration to the given {@link SimParams} object.
     * 
     * @param params
     *            the {@link SimParams} object containing the desired
     *            configuration
     */
    public abstract void setParams(SimParams params);
}
