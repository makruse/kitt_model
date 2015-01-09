package de.zmt.kitt.sim;

import static sim.engine.Schedule.EPOCH;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

import de.zmt.kitt.sim.engine.*;
import de.zmt.kitt.sim.engine.agent.Fish;
import de.zmt.kitt.sim.params.Params;
import de.zmt.sim_base.engine.ParamsSim;

/**
 * main class for running the simulation without gui
 */
public class Sim extends ParamsSim {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Sim.class.getName());

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_INPUT_DIR = "resources" + File.separator;
    public static final String DEFAULT_OUTPUT_DIR = "out" + File.separator;

    /* the environment of the simulation, contains also the fields */
    private final Environment environment;

    /**
     * @param path
     *            the path to the configuration file that initializes the model
     *            constructs the simulation. superclass is first initialized
     *            with seed 0. afterwards when running simulation it gets the
     *            seed from the config file.
     */
    public Sim(String path) {
	super(0);
	try {
	    params = Params.readFromXml(path);
	} catch (Exception e) {
	    logger.log(Level.WARNING, "Could not load parameters from " + path,
		    e);
	    // TODO load default parameter set
	}
	environment = new Environment(this);
    }

    @Deprecated
    // TODO replace with mason internal charting
    public Fish getFishInFocus() {
	if (schedule.getTime() < EPOCH
		|| environment.getFishField().getAllObjects().size() <= 0) {
	    return null;
	}

	return (Fish) environment.getFishField().getAllObjects().get(0);
    }

    public Environment getEnvironment() {
	return environment;
    }

    @Override
    public Params getParams() {
	return (Params) params;
    }

    public void setParams(Params params) {
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

	random.setSeed(getParams().environmentDefinition.seed);
	environment.initialize();
	schedule.scheduleRepeating(environment);
    }

    /**
     * run one simulation with the given configuration-file path Sim searches
     * first in the current local path for the configfile, if not found it
     * searches in the class path of ItnClass
     */
    public static void runSimulation(String inputPath, String outputPath,
	    String fileName) {

	long t1 = System.nanoTime();
	Sim sim = new Sim(inputPath + fileName);

	long steps = 0;

	OutputStepper outputStepper;
	try {
	    // create output file(s)
	    outputStepper = new OutputStepper();
	    outputStepper.prepareFile(outputPath);

	    // run the simulation
	    sim.start();
	    do {
		if (!sim.schedule.step(sim))
		    break;

		steps = sim.schedule.getSteps();
		// write current populations to outputfile(s)
		outputStepper.writeData(steps, sim);
		// if the number of steps exceeds maximum then finish
	    } while (steps < sim.getParams().environmentDefinition.simtime);
	    outputStepper.closeFile();

	} catch (IOException e) {
	    logger.log(Level.SEVERE, "Error while writing to output file to "
		    + outputPath);
	}

	sim.finish();
	long t2 = System.nanoTime();
	Date d = new Date((t2 - t1) / 1000000);
	SimpleDateFormat df = new SimpleDateFormat("mm:ss");
	logger.info("Simulation finished with " + steps + " steps in "
		+ df.format(d) + " (min:sec).");
    }

    /**
     * @param args
     * 
     *            filename of the local configuration file ( if not found,
     *            default configuration file config01.xml
     */
    public static void main(String[] args) {
	// setup logging
	final InputStream inputStream = Sim.class
		.getResourceAsStream("logging.properties");
	try {
	    LogManager.getLogManager().readConfiguration(inputStream);
	} catch (final IOException e) {
	    Logger.getAnonymousLogger().severe(
		    "Could not load default logging.properties file");
	    Logger.getAnonymousLogger().severe(e.getMessage());
	}

	runSimulation(DEFAULT_INPUT_DIR, DEFAULT_OUTPUT_DIR,
		Params.DEFAULT_FILENAME);

	System.exit(0);
    }

}
