package sim.engine;

import java.io.*;
import java.util.logging.*;

import de.zmt.ecs.*;
import de.zmt.ecs.factory.EntityFactory;
import de.zmt.ecs.system.agent.*;
import de.zmt.ecs.system.environment.*;
import sim.engine.BaseZmtSimState;
import sim.engine.output.*;
import sim.params.KittParams;
import sim.params.def.EnvironmentDefinition;

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

	environment = entityFactory.createEnvironment(environmentDefinition);
	entityFactory.createFishPopulation(environment, getParams().getSpeciesDefs());

	output = KittOutput.create(environment, new File(outputPath),
		getParams());
	schedule.scheduleRepeating(schedule.getTime() + 1, OUTPUT_ORDERING,
		output);

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
