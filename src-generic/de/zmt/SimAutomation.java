package de.zmt;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.xml.bind.JAXBException;

import sim.engine.SimState;
import de.zmt.sim.engine.Parameterizable;
import de.zmt.sim.engine.params.*;
import de.zmt.sim.engine.params.def.AutoDefinition;
import de.zmt.util.*;

/**
 * Entry class for starting automated simulation runs providing main method.
 * 
 * @author cmeyer
 * 
 */
public final class SimAutomation {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SimAutomation.class
	    .getName());

    private static final String RESULTS_DIR_PREFIX = "results_";
    private static final String PARAMS_FILENAME_SUFFIX = "_params.xml";

    private SimAutomation() {
    }

    public static void main(String[] args) {
	ValidatedArgs validatedArgs = validateArgs(args);
	main(validatedArgs.simClass, validatedArgs.autoParamsPath,
		validatedArgs.initialParamsPath);
    }

    /**
     * Validate program arguments or use default values if they are missing.
     * 
     * @param args
     *            arguments
     * @return validated arguments
     */
    @SuppressWarnings("unchecked")
    private static ValidatedArgs validateArgs(String[] args) {
	Class<? extends Parameterizable> simClass;
	String autoParamsPath = AutomationUtil.DEFAULT_INPUT_DIR
		+ AutoParams.DEFAULT_FILENAME;
	String initialParamsPath = AutomationUtil.DEFAULT_INPUT_DIR
		+ Params.DEFAULT_FILENAME;

	switch (args.length) {
	case 3:
	    initialParamsPath = args[2];
	case 2:
	    autoParamsPath = args[1];
	case 1:
	    String className = args[0];
	    try {
		simClass = (Class<? extends Parameterizable>) Class
			.forName(className);
	    } catch (ClassNotFoundException e) {
		System.out.println("Class " + className
			+ " could not be found.");
		System.exit(1);
		return null;
	    }
	    // class need to be a SimState as well
	    if (SimState.class.isAssignableFrom(simClass)) {
		return new ValidatedArgs(simClass, autoParamsPath,
			initialParamsPath);
	    } else {
		System.out.println(simClass.getSimpleName()
			+ " need to be a child of "
			+ SimState.class.getSimpleName());
	    }
	default:
	    System.out.println("Wrong number of parameters");
	    System.out
		    .println("Usage: SIM_CLASS [AUTO_PARAMS_FILE] [INITIAL_PARAMS_FILE]");
	    System.exit(1);
	    return null;
	}

    }

    /**
     * Automates given class with parameters loaded from default locations.
     * 
     * @see AutomationUtil#DEFAULT_INPUT_DIR
     * @see AutoParams#DEFAULT_FILENAME
     * @see Params#DEFAULT_FILENAME
     * @param simClass
     */
    public static void main(Class<? extends Parameterizable> simClass) {
	main(simClass, AutomationUtil.DEFAULT_INPUT_DIR
		+ AutoParams.DEFAULT_FILENAME, AutomationUtil.DEFAULT_INPUT_DIR
		+ Params.DEFAULT_FILENAME);
    }

    /**
     * Start a series of automated simulation runs for {@code simClass} with
     * parameters from given paths.
     * 
     * @param simClass
     * @param autoParamsPath
     * @param initialParamsPath
     */
    public static void main(Class<? extends Parameterizable> simClass,
	    String autoParamsPath, String initialParamsPath) {
	AutomationUtil.setupLogger();

	LoadedParams loadedParams = loadParams(autoParamsPath,
		initialParamsPath, ParamsUtil.obtainParamsClass(simClass));

	Collection<Map<AutoDefinition.FieldLocator, Object>> combinations = AutomationUtil
		.combineAutoDefs(loadedParams.autoParams.getDefinitions());

	runCombinations(simClass, combinations, loadedParams);
    }

    /**
     * Load parameters from validated arguments.
     * 
     * @param validArgs
     * @return loaded parameters
     */
    private static LoadedParams loadParams(String autoParamsPath,
	    String initialParamsPath, Class<? extends Params> simParamsClass) {
	AutoParams autoParams;
	Params currentSimParams;
	try {
	    autoParams = ParamsUtil.readFromXml(autoParamsPath,
		    AutoParams.class);
	    currentSimParams = ParamsUtil.readFromXml(initialParamsPath,
		    simParamsClass);
	} catch (FileNotFoundException | JAXBException e) {
	    logger.log(Level.SEVERE, "Could not load parameters from "
		    + autoParamsPath, e);
	    System.exit(1);
	    return null;
	}

	return new LoadedParams(autoParams, currentSimParams);
    }

    /**
     * Iterate combinations apply them to parameters and run a simulation with
     * each of them.
     * 
     * @param simClass
     * @param combinations
     * @param currentSimParams
     */
    // TODO threading
    // TODO link output to simCount
    private static void runCombinations(
	    Class<? extends Parameterizable> simClass,
	    Collection<Map<AutoDefinition.FieldLocator, Object>> combinations,
	    LoadedParams loadedParams) {
	String resultsPath = findResultsPath();

	int simNum = 0;
	for (Map<AutoDefinition.FieldLocator, Object> combination : combinations) {
	    logger.info("Running simulation " + simNum
		    + "\nUsing parameter combination: " + combination);

	    AutomationUtil.applyCombination(combination,
		    loadedParams.currentSimParams);
	    writeCurrentParams(loadedParams.currentSimParams, resultsPath,
		    simNum);

	    // create new simulation and run it
	    Parameterizable sim;
	    try {
		sim = simClass.newInstance();
	    } catch (InstantiationException | IllegalAccessException e) {
		logger.log(
			Level.SEVERE,
			"Unable to instantiate simulation class "
				+ simClass.getSimpleName()
				+ ". Accessible no-argument constructor present?",
			e);
		System.exit(1);
		return;
	    }
	    sim.setParams(loadedParams.currentSimParams);
	    sim.setOutputPath(resultsPath);
	    AutomationUtil.runSimulation((SimState) sim,
		    loadedParams.autoParams.getSimTime(), simNum);
	    simNum++;
	}
    }

    /** @return non-existent directory to write results to, e.g. "results_0023/" */
    private static String findResultsPath() {
	int resultsDirCount = 0;
	File resultsFile;
	do {
	    resultsFile = new File(RESULTS_DIR_PREFIX
		    + String.format(AutomationUtil.ZERO_PADDED_FORMAT_STRING,
			    resultsDirCount));
	    resultsDirCount++;
	} while (resultsFile.exists());

	// ... and create it
	resultsFile.mkdir();
	return resultsFile + File.separator;
    }

    /**
     * Parameters are written into results directory with prefixed number.
     * 
     * @param currentSimParams
     * @param resultsDir
     * @param simNum
     */
    private static void writeCurrentParams(Params currentSimParams,
	    String resultsPath, int simNum) {
	String outParamsPath = resultsPath
		+ String.format(AutomationUtil.ZERO_PADDED_FORMAT_STRING,
			simNum) + PARAMS_FILENAME_SUFFIX;
	try {
	    ParamsUtil.writeToXml(currentSimParams, outParamsPath);
	} catch (JAXBException | IOException e) {
	    logger.log(Level.WARNING, "Could not write current parameters to "
		    + outParamsPath, e);
	}
    }

    private static class ValidatedArgs {
	private final Class<? extends Parameterizable> simClass;
	private final String autoParamsPath;
	private final String initialParamsPath;

	public ValidatedArgs(Class<? extends Parameterizable> simClass,
		String autoParamsPath, String initialParamsPath) {
	    this.simClass = simClass;
	    this.autoParamsPath = autoParamsPath;
	    this.initialParamsPath = initialParamsPath;
	}
    }

    private static class LoadedParams {
	private final AutoParams autoParams;
	private final Params currentSimParams;

	public LoadedParams(AutoParams autoParams, Params currentSimParams) {
	    this.autoParams = autoParams;
	    this.currentSimParams = currentSimParams;
	}
    }
}
