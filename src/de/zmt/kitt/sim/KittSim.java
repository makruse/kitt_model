package de.zmt.kitt.sim;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

import javax.xml.bind.JAXBException;

import sim.engine.SimState;
import de.zmt.kitt.sim.display.KittGui.GuiPortrayable;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.engine.output.KittOutput;
import de.zmt.kitt.sim.params.KittParams;
import de.zmt.sim.engine.Parameterizable;
import de.zmt.sim.engine.output.Output;
import de.zmt.sim.engine.params.AbstractParams;
import de.zmt.sim.portrayal.portrayable.ProvidesPortrayable;

/**
 * main class for running the simulation without gui
 */
public class KittSim extends SimState implements Parameterizable,
	ProvidesPortrayable<GuiPortrayable> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittSim.class
	    .getName());

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_INPUT_DIR = "resources" + File.separator;
    public static final String DEFAULT_OUTPUT_DIR = "out" + File.separator;

    /** Environment needs to be updated after the agents */
    private static final int ENVIRONMENT_ORDERING = 1;
    /** Output is stepped after everything else */
    private static final int OUTPUT_ORDERING = 2;

    /** Simulation environment including fields. */
    private Environment environment;
    /** Simulation output (GUI and file) */
    private Output output;
    /** Simulation parameters */
    private KittParams params;

    /**
     * 
     * @param configPath
     *            path to configuration file
     */
    public KittSim(String configPath) {
	// seed is set in start() from config
	super(0);
	params = new KittParams();

	try {
	    params = KittParams.readFromXml(configPath, KittParams.class);
	} catch (FileNotFoundException e) {
	    logger.log(Level.INFO, "No file found at " + configPath
		    + ". Loading default parameter set.");
	} catch (JAXBException e) {
	    logger.log(Level.WARNING,
		    "Parameter loading failed: XML parsing failed at "
			    + configPath, e);
	}
    }

    public Output getOutput() {
	return output;
    }

    @Override
    public KittParams getParams() {
	return params;
    }

    @Override
    public void setParams(AbstractParams params) {
	this.params = (KittParams) params;
    }

    @Override
    public void start() {
	super.start();

	setSeed(getParams().getEnvironmentDefinition().getSeed());

	environment = new Environment(random, getParams(), schedule);
	output = KittOutput.create(environment, new File(DEFAULT_OUTPUT_DIR),
		getParams());

	schedule.scheduleRepeating(schedule.getTime() + 1,
		ENVIRONMENT_ORDERING, environment);
	schedule.scheduleRepeating(schedule.getTime() + 1, OUTPUT_ORDERING,
		output);
    }

    @Override
    public void finish() {
	super.finish();

	try {
	    output.close();
	} catch (IOException e) {
	    logger.log(Level.SEVERE, "Failed to close output.", e);
	}
    }

    @Override
    public GuiPortrayable providePortrayable() {
	return environment.providePortrayable();
    }

    /**
     * run one simulation with the given configuration-file path Sim searches
     * first in the current local path for the configfile, if not found it
     * searches in the class path of ItnClass
     * 
     * @param inputPath
     * @param fileName
     */
    public static void runSimulation(String inputPath, String fileName) {

	long startTime = System.currentTimeMillis();
	KittSim sim = new KittSim(inputPath + fileName);

	// run the simulation
	sim.start();

	while (sim.schedule.step(sim)
		&& sim.schedule.getSteps() < sim.getParams()
			.getEnvironmentDefinition().getSimTime())
	    ;

	sim.finish();
	long runTime = System.currentTimeMillis() - startTime;

	String runTimeString = new SimpleDateFormat("mm:ss.SSS")
		.format(new Date(runTime));

	logger.info("Simulation finished with " + sim.schedule.getSteps()
		+ " steps in " + runTimeString);
    }

    /**
     * @param args
     * 
     *            filename of the local configuration file ( if not found,
     *            default configuration file config01.xml
     */
    public static void main(String[] args) {
	// setup logging
	final InputStream inputStream = KittSim.class
		.getResourceAsStream("logging.properties");
	try {
	    LogManager.getLogManager().readConfiguration(inputStream);
	} catch (final IOException e) {
	    Logger.getAnonymousLogger().severe(
		    "Could not load default logging.properties file");
	    Logger.getAnonymousLogger().severe(e.getMessage());
	}

	runSimulation(DEFAULT_INPUT_DIR, KittParams.DEFAULT_FILENAME);

	System.exit(0);
    }

}
