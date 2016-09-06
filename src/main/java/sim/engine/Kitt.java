package sim.engine;

import java.util.Optional;
import java.util.logging.Logger;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.factory.KittEntityCreationHandler;
import de.zmt.ecs.system.MetamorphosisSystem;
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
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.KittParams;
import sim.util.Int2DCache;

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
            schedule);
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
    public Optional<Output> getOutput() {
        return Optional.ofNullable(output);
    }

    @Override
    public void start() {
        super.start();

        EntityManager manager = entityCreationHandler.getManager();
        EnvironmentDefinition environmentDefinition = getParams().getEnvironmentDefinition();

        manager.clear();

        // create entities
        environment = entityCreationHandler.createEnvironment(environmentDefinition, random);
        entityCreationHandler.createFishPopulation(environment, getParams().getSpeciesDefs(), random);

        // create output
        output = new KittOutput(getOutputPath(), getParams(), environment.get(HabitatMap.class));
        schedule.scheduleRepeating(schedule.getTime() + 1, OUTPUT_ORDERING, output);

        // add agent systems
        manager.addSystem(new BehaviorSystem());
        manager.addSystem(new AgeSystem());
        manager.addSystem(new ConsumeSystem());
        manager.addSystem(new FeedSystem());
        manager.addSystem(new GrowthSystem());
        manager.addSystem(new MortalitySystem());
        manager.addSystem(new MoveSystem());
        manager.addSystem(new ReproductionSystem());

        // add larva systems
        manager.addSystem(new MetamorphosisSystem());

        // add environment systems
        manager.addSystem(new SimulationTimeSystem());
        manager.addSystem(new FoodSystem());
    }

    @Override
    public void awakeFromCheckpoint() {
        HabitatMap habitatMap = getEnvironment().get(HabitatMap.class);
        Int2DCache.adjustCacheSize(habitatMap.getWidth(), habitatMap.getHeight());
        super.awakeFromCheckpoint();
    }

}
