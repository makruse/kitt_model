package sim.engine;

import static javax.measure.unit.NonSI.DAY;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

import de.zmt.ecs.component.agent.LifeCycling;
import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.SimulationTime;
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
import de.zmt.params.SpeciesDefinition;
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
    /** Interval for checking if there are still agents in the simulation. */
    private static final Amount<Duration> EXTINCTION_CHECK_INTERVAL = Amount.valueOf(1, DAY);

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
    public String createStatusMessage(double stepRatePerS) {
        return "Elapsed: " + environment.get(SimulationTime.class).computeElapsedTime() + " "
                + super.createStatusMessage(stepRatePerS);
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
        //the better way for a full simulation
        //entityCreationHandler.createFishPopulation(environment, getParams().getSpeciesDefs(), random);
        entityCreationHandler.createFishPopulation(environment, getParams().getSpeciesDefs(), random, 1, 1, 1);

        // create output
        output = new KittOutput(getOutputPath(), getParams(), environment.get(HabitatMap.class),
                environment.get(FoodMap.class));
        schedule.scheduleRepeating(schedule.getTime() + 1, OUTPUT_ORDERING, output);

        // schedule extinction check after everything else
        schedule.scheduleRepeating(new ExtinctionCheck(), Integer.MAX_VALUE, EXTINCTION_CHECK_INTERVAL
                .divide(environmentDefinition.getStepDuration()).to(Unit.ONE).getEstimatedValue());

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

    /**
     * Kills the simulation if there are no agents left.
     * 
     * @author mey
     *
     */
    static class ExtinctionCheck implements Steppable {
        private static final long serialVersionUID = 1L;

        @Override
        public void step(SimState state) {
            if (((Kitt) state).getEntityCreationHandler().getManager()
                    .getAllEntitiesPossessingComponent(SpeciesDefinition.class).isEmpty()) {
                state.kill();
                logger.info("Simulation was killed: No agents left.");
            }
        }

    }

}
