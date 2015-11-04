package de.zmt.launcher;

import org.kohsuke.args4j.*;

import sim.engine.ZmtSimState;
import sim.engine.params.*;

/**
 * Provides launcher related arguments entered in the command line.
 * 
 * @author mey
 *
 */
public class LauncherArgs {
    private static final String DEFAULT_SIM_PARAMS_PATH = ZmtSimState.DEFAULT_INPUT_DIR + SimParams.DEFAULT_FILENAME;
    private static final String DEFAULT_AUTO_PARAMS_PATH = ZmtSimState.DEFAULT_INPUT_DIR + AutoParams.DEFAULT_FILENAME;
    private static final double DEFAULT_SIM_TIME = 2000;

    @Option(name = "-h", aliases = "--help", help = true, hidden = true, usage = "Print help screen.")
    private boolean help;
    @Argument(index = 0, metaVar = "<NAME>", required = true, usage = "Specify simulation name.")
    private String simName;
    @Argument(index = 1, required = true, metaVar = "<MODE>", usage = "Set interface mode. Available options are gui, single and batch.")
    private Mode mode;
    @Option(name = "-s", aliases = "--sim-params", metaVar = "SIM_PARAMS", usage = "Optional: Path to the simulation parameters XML file.")
    private String simParamsPath = DEFAULT_SIM_PARAMS_PATH;
    @Option(name = "-a", aliases = "--auto-params", metaVar = "AUTO_PARAMS", usage = "Optional: Path to the automation parameters XML file.\n(BATCH mode only)")
    private String autoParamsPath = DEFAULT_AUTO_PARAMS_PATH;
    @Option(name = "-u", aliases = "--until", usage = "Optional: Make simultion stop after given time has been reached or exceeded.\n(SINGLE mode only)")
    private double simTime = DEFAULT_SIM_TIME;

    /**
     * Restore default values. Used to ensure that {@link CmdLineParser} will
     * display the correct values. Otherwise values from command line are
     * displayed as default values.
     */
    public void restoreDefaults() {
	help = false;
	simParamsPath = DEFAULT_SIM_PARAMS_PATH;
	autoParamsPath = DEFAULT_AUTO_PARAMS_PATH;
	simTime = DEFAULT_SIM_TIME;
    }

    public boolean isHelp() {
	return help;
    }

    String getSimName() {
	return simName;
    }

    Mode getMode() {
	return mode;
    }

    String getSimParamsPath() {
	return simParamsPath;
    }

    String getAutoParamsPath() {
	return autoParamsPath;
    }

    double getSimTime() {
	return simTime;
    }

    /** @return true if {@code simParamsPath} differs from default. */
    boolean isSimParamsPathSet() {
	return !simParamsPath.equals(DEFAULT_SIM_PARAMS_PATH);
    }

    public static enum Mode {
	GUI, SINGLE, BATCH
    }
}
