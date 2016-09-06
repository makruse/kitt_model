package de.zmt.ecs.factory;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.Metamorphic;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.WorldDimension;
import de.zmt.params.AgeDistribution;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.SpeciesDefinition;
import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;

/**
 * Creates fish (agent) and environment (fields / grids) entities. Needed
 * components are added and their values set to an initial state.
 * 
 * @author mey
 *
 */
public class KittEntityCreationHandler implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittEntityCreationHandler.class.getName());

    /**
     * Schedule larvae before agents to make them update within the same step
     * after metamorphosis.
     */
    private static final int LARVA_ORDERING = FishFactory.ORDERING - 1;
    /**
     * Ordering for environment entity in {@link Schedule}. Needs to be updated
     * after agents.
     */
    private static final int ENVIRONMENT_ORDERING = FishFactory.ORDERING + 1;

    private static final EnvironmentFactory ENVIRONMENT_FACTORY = new EnvironmentFactory();
    private static final FishFactory FISH_FACTORY = new FishFactory();
    private static final LarvaFactory LARVA_FACTORY = new LarvaFactory();

    private final EntityManager manager;
    private final Schedule schedule;

    public KittEntityCreationHandler(EntityManager manager, Schedule schedule) {
        super();
        this.manager = manager;
        this.schedule = schedule;
    }

    /**
     * Creates the environment entity from {@code definition}.
     * 
     * @param definition
     *            the environment definition
     * @param random
     *            the random number generator to be used
     * @return environment entity
     */
    public Entity createEnvironment(EnvironmentDefinition definition, MersenneTwisterFast random) {
        Entity environment = ENVIRONMENT_FACTORY.create(getManager(),
                new EnvironmentFactory.MyParam(random, definition));
        environment
                .addStoppable(schedule.scheduleRepeating(schedule.getTime() + 1.0, ENVIRONMENT_ORDERING, environment));
        return environment;
    }

    /**
     * Creates fish population for every species according to its definition
     * with each individual at a random age from the species' distribution.
     * 
     * @see SpeciesDefinition#getInitialNum()
     * @see SpeciesDefinition#createAgeDistribution(MersenneTwisterFast)
     * @param environment
     *            entity representing the environment the fish are placed into
     * @param speciesDefs
     *            the species definition of the population
     * @param random
     *            the random number generator to be used
     */
    public void createFishPopulation(Entity environment, Collection<SpeciesDefinition> speciesDefs,
            MersenneTwisterFast random) {
        for (SpeciesDefinition speciesDefinition : speciesDefs) {
            AgeDistribution ageDistribution = speciesDefinition.createAgeDistribution(random);

            for (int i = 0; i < speciesDefinition.getInitialNum(); i++) {
                createFish(speciesDefinition, environment, ageDistribution.next(), random);
            }
        }
    }

    /**
     * Creates a new fish at a given initial age and a random position within
     * their spawn habitat and add it to schedule and agent field.
     * 
     * @param definition
     *            the {@link SpeciesDefinition} of the fish
     * @param environment
     *            the entity representing the environment the fish is placed
     *            into
     * @param initialAge
     *            the fish's initial age
     * @param random
     *            the random number generator to be used
     * @return fish entity
     */
    public Entity createFish(SpeciesDefinition definition, Entity environment, Amount<Duration> initialAge,
            MersenneTwisterFast random) {
        Entity fish = FISH_FACTORY.create(getManager(),
                new FishFactory.MyParam(definition, environment, initialAge, random));
        schedule.scheduleOnce(fish, FishFactory.ORDERING);
        return fish;
    }

    /**
     * Creates a new larva. Larvae are fish before reaching the post settlement
     * age. After that, metamorphosis is complete and a fish entity will then
     * enter the simulation, while the larva is removed.
     * 
     * @param definition
     *            the {@link SpeciesDefinition} definition of the fish hatching
     *            from the larva
     * @param stepDuration
     *            the simulation time that passes on every step
     * @param random
     *            the random number generator used
     * @return the created larva entity
     */
    public Entity createLarva(SpeciesDefinition definition, Amount<Duration> stepDuration, MersenneTwisterFast random) {
        Entity larva = LARVA_FACTORY.create(getManager(), new LarvaFactory.MyParam(definition,
                stepDuration, schedule.getTime()));
        schedule.scheduleOnce(larva.get(Metamorphic.class).getMetamorphosisTime(), LARVA_ORDERING, larva);
        return larva;
    }

    public Entity loadEntity(UUID uuid) {
        if (getManager().hasComponent(uuid, Moving.class)) {
            return FISH_FACTORY.load(getManager(), uuid);
        } else if (getManager().hasComponent(uuid, WorldDimension.class)) {
            return ENVIRONMENT_FACTORY.load(getManager(), uuid);
        }
        return Entity.loadFromEntityManager(getManager(), uuid);
    }

    public EntityManager getManager() {
        return manager;
    }
}
