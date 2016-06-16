package sim.engine;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.factory.KittEntityCreationHandler;
import de.zmt.ecs.system.agent.AgeSystem;
import de.zmt.ecs.system.agent.BehaviorSystem;
import de.zmt.ecs.system.agent.ConsumeSystem;
import de.zmt.ecs.system.agent.FeedSystem;
import de.zmt.ecs.system.agent.GrowthSystem;
import de.zmt.ecs.system.agent.MortalitySystem;
import de.zmt.ecs.system.agent.ReproductionSystem;
import de.zmt.ecs.system.agent.move.MoveSystem;
import de.zmt.ecs.system.environment.FoodSystem;
import de.zmt.ecs.system.environment.SimulationTimeSystem;
import de.zmt.output.KittOutput;
import de.zmt.output.Output;
import de.zmt.params.KittParams;
import de.zmt.params.def.EnvironmentDefinition;

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

    public KittEntityCreationHandler getEntityCreationHandler() {
	return entityCreationHandler;
    }

    @Override
    public Class<? extends KittParams> getParamsClass() {
	return KittParams.class;
    }

    @Override
    public Output getOutput() {
	return output;
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
	output = new KittOutput(getOutputPath(), getParams());
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
