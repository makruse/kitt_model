package de.zmt.sim.engine;

import java.io.*;
import java.util.Arrays;
import java.util.logging.*;

import de.zmt.ecs.*;
import de.zmt.ecs.system.agent.*;
import de.zmt.ecs.system.environment.*;
import de.zmt.sim.engine.output.*;
import de.zmt.sim.params.KittParams;
import de.zmt.sim.params.def.EnvironmentDefinition;

/**
 * Central simulation class of kitt.
 * 
 * @author cmeyer
 *
 */
public class Kitt extends BaseZmtSimState<KittParams> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Kitt.class
	    .getName());

    private static final long serialVersionUID = 1L;

    public static final Class<KittParams> PARAMS_CLASS = KittParams.class;

    /** Output is stepped last in scheduler. */
    private static final int OUTPUT_ORDERING = 2;

    private final EntityFactory entityFactory = new EntityFactory(
	    new EntityManager(), this);
    /** Simulation environment including fields. */
    private Entity environment;
    /** Simulation output (GUI and file) */
    private Output output;

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
	entityFactory.createFishPopulation(environment, getParams().getSpeciesDefs());

	output = KittOutput.create(environment, new File(outputPath),
		getParams());
	schedule.scheduleRepeating(schedule.getTime() + 1, OUTPUT_ORDERING,
		output);

	// add agent systems
	manager.addSystems(Arrays.asList(new BehaviorSystem(this),
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
