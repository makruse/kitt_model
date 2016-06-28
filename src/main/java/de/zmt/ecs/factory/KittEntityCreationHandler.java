package de.zmt.ecs.factory;

import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
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
public class KittEntityCreationHandler extends EntityCreationHandler implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittEntityCreationHandler.class.getName());

    /** Ordering for agent entities in {@link Schedule}. */
    private static final int AGENT_ORDERING = 0;
    private static final int LARVA_ORDERING = AGENT_ORDERING - 1;
    /**
     * Ordering for environment entities in {@link Schedule}. Needed to be
     * updated after agents.
     */
    private static final int ENVIRONMENT_ORDERING = AGENT_ORDERING + 1;

    private static final EnvironmentFactory ENVIRONMENT_FACTORY = new EnvironmentFactory();
    private static final FishFactory FISH_FACTORY = new FishFactory();
    private static final LarvaFactory LARVA_FACTORY = new LarvaFactory();

    public KittEntityCreationHandler(EntityManager manager, MersenneTwisterFast random, Schedule schedule) {
        super(manager, random, schedule);
    }

    /**
     * Creates the environment entity from {@code definition}.
     * 
     * @param definition
     * @return environment entity
     */
    public Entity createEnvironment(EnvironmentDefinition definition) {
        return addEntity(ENVIRONMENT_FACTORY, definition, ENVIRONMENT_ORDERING);
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
     */
    public void createFishPopulation(Entity environment, Collection<SpeciesDefinition> speciesDefs) {
        for (SpeciesDefinition speciesDefinition : speciesDefs) {
            AgeDistribution ageDistribution = speciesDefinition.createAgeDistribution(getRandom());

            for (int i = 0; i < speciesDefinition.getInitialNum(); i++) {
                createFish(speciesDefinition, environment, ageDistribution.next());
            }
        }
    }

    /**
     * Creates a new fish at a given initial age and a random position within
     * their spawn habitat and add it to schedule and agent field.
     * 
     * @param definition
     *            species definition of the fish
     * @param environment
     *            entity representing the environment the fish is placed into
     * @param initialAge
     *            the fish's initial age
     * @return fish entity
     */
    public Entity createFish(SpeciesDefinition definition, Entity environment, Amount<Duration> initialAge) {
        return addEntity(FISH_FACTORY, new FishFactory.MyParam(definition, environment, initialAge), AGENT_ORDERING);
    }

    public Entity createLarva(SpeciesDefinition definition, KittEntityCreationHandler entityCreationHandler,
            Entity environment) {
        return addEntity(LARVA_FACTORY, new LarvaFactory.MyParam(definition, entityCreationHandler, environment),
                LARVA_ORDERING);
    }
}
