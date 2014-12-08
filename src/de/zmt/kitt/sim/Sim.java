package de.zmt.kitt.sim;

import static sim.engine.Schedule.EPOCH;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

import sim.engine.SimState;
import sim.util.Bag;
import de.zmt.kitt.sim.engine.*;
import de.zmt.kitt.sim.engine.agent.Fish;
import de.zmt.kitt.sim.params.ModelParams;

/**
 * main class for running the simulation without gui
 */
public class Sim extends SimState {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Sim.class.getName());

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_INPUT_DIR = "resources" + File.separator;
    public static final String DEFAULT_OUTPUT_DIR = "out" + File.separator;

    /* the environment of the simulation, contains also the fields */
    private final Environment environment;
    private ModelParams params;

    protected long fishInFocus = 5;

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
            params = ModelParams.readFromXml(path);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load parameters from " + path,
        	    e);
            // TODO load default parameter set
        }
        environment = new Environment(this);
    }

    synchronized public void setIdInFocus(long id) {
	fishInFocus = id;
    }

    synchronized public long getIdInFocus() {
	return fishInFocus;
    }

    // FIXME this is called per draw and per step!!!
    public Fish getFishInFocus() {
	// no fish in focus before simulation has started
	if (schedule.getTime() < EPOCH) {
	    return null;
	}

	Bag bag = environment.getFishField().getAllObjects();
	for (Object o : bag) {
	    Fish f = (Fish) o;
	    if (f.getId() == fishInFocus) {
		return f;
	    }
	}
	return null;
    }

    public double getTimeResolutionInMinutes() {
	return params.environmentDefinition.timeResolutionMinutes;
    }

    public Environment getEnvironment() {
	return environment;
    }

    public ModelParams getParams() {
        return params;
    }

    public void setParams(ModelParams params) {
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

	random.setSeed(params.environmentDefinition.seed);
	environment.initPlayground();
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
	    logger.info(sim.params.toString());
	    outputStepper = new OutputStepper();
	    outputStepper.prepareFile(outputPath + fileName);

	    // run the simulation
	    sim.start();
	    do {
		if (!sim.schedule.step(sim))
		    break;

		steps = sim.schedule.getSteps();
		// write current populations to outputfile(s)
		outputStepper.writeData(steps, sim);
		// if the number of steps exceeds maximum then finish
	    } while (steps < sim.params.environmentDefinition.simtime);
	    outputStepper.closeFile();

	} catch (IOException e) {
	    logger.log(Level.SEVERE, "Error while writing to output file "
		    + outputPath + fileName);
	}

	sim.finish();
	long t2 = System.nanoTime();
	Date d = new Date((t2 - t1) / 1000000);
	SimpleDateFormat df = new SimpleDateFormat("mm:ss");
	logger.info("Simulation finished with " + steps + " steps in "
		+ df.format(d) + " (min:sec) \noutput written to " + outputPath
		+ ".csv");
    }

    /**
     * @param args
     * 
     *            filename of the local configuration file ( if not found,
     *            default configuration file config01.xml
     */
    public static void main(String[] args) {
	runSimulation(DEFAULT_INPUT_DIR, DEFAULT_OUTPUT_DIR,
		ModelParams.DEFAULT_FILENAME);

	System.exit(0);
    }

}
