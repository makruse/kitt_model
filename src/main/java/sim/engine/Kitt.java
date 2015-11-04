package sim.engine;

import java.io.*;
import java.util.logging.*;

import de.zmt.ecs.*;
import de.zmt.ecs.factory.KittEntityCreationHandler;
import de.zmt.ecs.system.agent.*;
import de.zmt.ecs.system.environment.*;
import sim.engine.output.*;
import sim.params.KittParams;
import sim.params.def.EnvironmentDefinition;

/**
 * Central simulation class of kitt.
 * 
 * @author mey
 *
 */
public class Kitt extends BaseZmtSimState<KittParams> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Kitt.class.getName());

    private static final long serialVersionUID = 1L;

    public static final Class<KittParams> PARAMS_CLASS = KittParams.class;

    /** Output is stepped last in scheduler. */
    private static final int OUTPUT_ORDERING = Integer.MAX_VALUE;

    private final KittEntityCreationHandler entityCreationHandler = new KittEntityCreationHandler(new EntityManager(),
	    random, schedule);
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

    public KittEntityCreationHandler getEntityCreationHandler() {
	return entityCreationHandler;
    }

    @Override
    public void setOutputPath(String outputPath) {
	this.outputPath = outputPath;
    }

    @Override
    public void start() {
	super.start();

	EntityManager manager = entityCreationHandler.getManager();
	EnvironmentDefinition environmentDefinition = getParams().getEnvironmentDefinition();

	manager.clear();

	// create entities
	environment = entityCreationHandler.createEnvironment(environmentDefinition);
	entityCreationHandler.createFishPopulation(environment, getParams().getSpeciesDefs());

	// create output
	output = KittOutput.create(environment, new File(outputPath), getParams());
	schedule.scheduleRepeating(schedule.getTime() + 1, OUTPUT_ORDERING, output);

	// add agent systems
	manager.addSystem(new BehaviorSystem(this));
	manager.addSystem(new AgeSystem(this));
	manager.addSystem(new ConsumeSystem(this));
	manager.addSystem(new FeedSystem(this));
	manager.addSystem(new GrowthSystem(this));
	manager.addSystem(new MortalitySystem(this));
	manager.addSystem(new MoveSystem(this));
	manager.addSystem(new ReproductionSystem(this));

	// add environment systems
	manager.addSystem(new SimulationTimeSystem());
	manager.addSystem(new FoodSystem());
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
