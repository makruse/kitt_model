package de.zmt.kitt.sim;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

import javax.xml.bind.JAXBException;

import sim.engine.SimState;
import de.zmt.kitt.ecs.EntityFactory;
import de.zmt.kitt.ecs.system.agent.*;
import de.zmt.kitt.ecs.system.environment.*;
import de.zmt.kitt.sim.engine.output.KittOutput;
import de.zmt.kitt.sim.params.KittParams;
import de.zmt.kitt.sim.params.def.EnvironmentDefinition;
import de.zmt.sim.engine.Parameterizable;
import de.zmt.sim.engine.output.Output;
import de.zmt.sim.engine.params.AbstractParams;
import ecs.*;

/**
 * main class for running the simulation without gui
 */
public class KittSim extends SimState implements Parameterizable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittSim.class
	    .getName());

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_INPUT_DIR = "resources" + File.separator;
    public static final String DEFAULT_OUTPUT_DIR = "out" + File.separator;

    /** Output is stepped after everything else */
    private static final int OUTPUT_ORDERING = 2;

    private final EntityFactory entityFactory;
    /** Simulation environment including fields. */
    private Entity environment;
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

	entityFactory = new EntityFactory(new EntityManager(), this);
    }

    public Entity getEnvironment() {
	return environment;
    }

    public Output getOutput() {
	return output;
    }

    public EntityFactory getEntityFactory() {
	return entityFactory;
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

	EntityManager manager = entityFactory.getManager();
	EnvironmentDefinition environmentDefinition = getParams()
		.getEnvironmentDefinition();

	manager.clear();
	setSeed(environmentDefinition.getSeed());

	environment = entityFactory.createEnvironment(environmentDefinition);
	entityFactory.createInitialFish(environment, params.getSpeciesDefs());

	output = KittOutput.create(environment, new File(DEFAULT_OUTPUT_DIR),
		getParams());
	schedule.scheduleRepeating(schedule.getTime() + 1, OUTPUT_ORDERING,
		output);

	// add agent systems
	manager.addSystems(Arrays.asList(new ActivitySystem(this),
		new AgeSystem(this), new CompartmentsSystem(this),
		new ConsumeSystem(this), new FeedSystem(this),
		new GrowthSystem(this), new MortalitySystem(this),
		new MoveSystem(this), new ReproductionSystem(this)));
	// add environment systems
	manager.addSystems(Arrays
		.asList(new SimulationTimeSystem(), new GrowFoodSystem()));

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
     *            filename of the local configuration file (
     *            {@link KittParams#DEFAULT_FILENAME} if empty)
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

	String paramsFileName = args.length > 0 ? args[0]
		: KittParams.DEFAULT_FILENAME;
	runSimulation(DEFAULT_INPUT_DIR, paramsFileName);
    }
}
