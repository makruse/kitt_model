package de.zmt.launcher;

import java.io.File;
import java.nio.file.Path;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import sim.engine.ZmtSimState;
import sim.engine.params.AutoParams;
import sim.engine.params.SimParams;

/**
 * Stores launcher related arguments entered in the command line.
 * 
 * @author mey
 *
 */
public class LauncherArgs {
    private static final File DEFAULT_SIM_PARAMS_PATH = ZmtSimState.DEFAULT_INPUT_DIR
	    .resolve(SimParams.DEFAULT_FILENAME).toFile();
    private static final File DEFAULT_AUTO_PARAMS_PATH = ZmtSimState.DEFAULT_INPUT_DIR
	    .resolve(AutoParams.DEFAULT_FILENAME).toFile();
    private static final double DEFAULT_SIM_TIME = 2000;

    @Option(name = "-h", aliases = "--help", help = true, hidden = true, usage = "Print help screen.")
    private boolean help;
    @Argument(index = 0, metaVar = "<NAME>", required = true, usage = "Specify simulation name. This name must correspond to the name of the simulation's SimState class.")
    private String simName;
    @Argument(index = 1, metaVar = "<MODE>", required = true, usage = "Set interface mode. Available options are gui, single and batch.")
    private Mode mode;
    @Option(name = "-s", aliases = "--sim-params", metaVar = "SIM_PARAMS", usage = "Path to the simulation parameters XML file.")
    private File simParamsPath = DEFAULT_SIM_PARAMS_PATH;
    @Option(name = "-a", aliases = "--auto-params", metaVar = "AUTO_PARAMS", usage = "Path to the automation parameters XML file.\n(BATCH mode only)")
    private File autoParamsPath = DEFAULT_AUTO_PARAMS_PATH;
    @Option(name = "-u", aliases = "--until", usage = "Make simulation stop after given time has been reached or exceeded.\n(SINGLE mode only)")
    private double simTime = DEFAULT_SIM_TIME;
    @Option(name = "-es", aliases = "--export-sim-params", help = true, usage = "Exports default simulation parameters .")
    private File exportSimParamsFile;
    @Option(name = "-ea", aliases = "--export-auto-params", help = true, usage = "Exports example automation parameters.")
    private File exportAutoParamsFile;
    @Option(name = "-cf", aliases = "--combination-in-folder-names", usage = "Use combination to generate inner folder names. \n(UNSAFE, BATCH mode only)")
    private boolean combinationInFolderNames;

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

    public String getSimName() {
	return simName;
    }

    public Mode getMode() {
	return mode;
    }

    public Path getSimParamsPath() {
	return simParamsPath.toPath();
    }

    public Path getAutoParamsPath() {
	return autoParamsPath.toPath();
    }

    public double getSimTime() {
	return simTime;
    }

    public Path getExportSimParamsPath() {
	return exportSimParamsFile == null ? null : exportSimParamsFile.toPath();
    }

    public Path getExportAutoParamsPath() {
	return exportAutoParamsFile == null ? null : exportAutoParamsFile.toPath();
    }

    public boolean isCombinationInFolderNames() {
	return combinationInFolderNames;
    }

    /** @return <code>true</code> if {@code simParamsPath} is set to default */
    public boolean isDefaultSimParamsPath() {
	return simParamsPath.equals(DEFAULT_SIM_PARAMS_PATH);
    }

    public static enum Mode {
	GUI, SINGLE, BATCH
    }
}
