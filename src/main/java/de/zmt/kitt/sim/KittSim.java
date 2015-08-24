package de.zmt.kitt.sim;

import java.io.*;
import java.util.Arrays;
import java.util.logging.*;

import de.zmt.ecs.*;
import de.zmt.kitt.ecs.EntityFactory;
import de.zmt.kitt.ecs.system.agent.*;
import de.zmt.kitt.ecs.system.environment.*;
import de.zmt.kitt.sim.engine.output.KittOutput;
import de.zmt.kitt.sim.params.KittParams;
import de.zmt.kitt.sim.params.def.EnvironmentDefinition;
import de.zmt.sim.engine.BaseZmtSimState;
import de.zmt.sim.engine.output.Output;

/**
 * main class for running the simulation without gui
 */
public class KittSim extends BaseZmtSimState<KittParams> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittSim.class
	    .getName());

    private static final long serialVersionUID = 1L;

    public static final Class<KittParams> PARAMS_CLASS = KittParams.class;

    /** Output is stepped after everything else */
    private static final int OUTPUT_ORDERING = 2;

    private final EntityFactory entityFactory = new EntityFactory(
	    new EntityManager(), this);
    /** Simulation environment including fields. */
    private Entity environment;
    /** Simulation output (GUI and file) */
    private Output output;
    /** Simulation parameters */
    private KittParams params;

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
    public void setOutputPath(String outputPath) {
	this.outputPath = outputPath;
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
	entityFactory.createFishPopulation(environment, params.getSpeciesDefs());

	output = KittOutput.create(environment, new File(outputPath),
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
}
