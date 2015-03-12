package de.zmt.kitt.sim;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

import javax.xml.bind.JAXBException;

import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.params.KittParams;
import de.zmt.sim.engine.ParamsSim;

/**
 * main class for running the simulation without gui
 */
public class KittSim extends ParamsSim {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittSim.class
	    .getName());

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_INPUT_DIR = "resources" + File.separator;
    public static final String DEFAULT_OUTPUT_DIR = "out" + File.separator;

    /** the environment of the simulation, contains also the fields */
    private Environment environment;

    /**
     * @param path
     *            the path to the configuration file that initializes the model
     *            constructs the simulation. superclass is first initialized
     *            with seed 0. afterwards when running simulation it gets the
     *            seed from the config file.
     */
    public KittSim(String path) {
	// seed is set in start() from config
	super(0);
	params = new KittParams();

	try {
	    params = KittParams.readFromXml(path, KittParams.class);
	} catch (FileNotFoundException e) {
	    logger.log(Level.INFO, "No file found at " + path
		    + ". Loading default parameter set.");
	} catch (JAXBException e) {
	    logger.log(Level.WARNING,
		    "Parameter loading failed: XML parsing failed at " + path,
		    e);
	}
    }

    public Environment getEnvironment() {
	return environment;
    }

    @Override
    public KittParams getParams() {
	return (KittParams) params;
    }

    public void setParams(KittParams params) {
	this.params = params;
    }

    /**
     * starts the simulation. for each run the model input parameters are set by
     * the last set configuration file. the field is initialized and then seeded
     * by a given number of agents to a random position. Habitats are put into
     * the field, if specified and activated in configuration.
     * 
     * @see sim.engine.SimState#start()
     */
    @Override
    public void start() {
	super.start();

	environment = new Environment(this);
	schedule.scheduleRepeating(environment);
	random.setSeed(getParams().getEnvironmentDefinition().getSeed());
    }

    /**
     * run one simulation with the given configuration-file path Sim searches
     * first in the current local path for the configfile, if not found it
     * searches in the class path of ItnClass
     * 
     * @param inputPath
     * @param outputPath
     * @param fileName
     */
    public static void runSimulation(String inputPath, String outputPath,
	    String fileName) {

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

	runSimulation(DEFAULT_INPUT_DIR, DEFAULT_OUTPUT_DIR,
		KittParams.DEFAULT_FILENAME);

	System.exit(0);
    }

}
