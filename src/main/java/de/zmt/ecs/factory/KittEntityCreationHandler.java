package de.zmt.ecs.factory;

import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Logger;

import de.zmt.ecs.*;
import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;
import sim.params.def.*;

/**
 * Creates fish (agent) and environment (fields / grids) entities. Needed
 * components are added and their values set to an initial state.
 * 
 * @author cmeyer
 *
 */
public class KittEntityCreationHandler extends EntityCreationHandler implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittEntityCreationHandler.class.getName());

    /** Ordering for agent entities in {@link Schedule}. */
    private static final int AGENT_ORDERING = 0;
    /**
     * Ordering for environment entities in {@link Schedule}. Needed to be
     * updated after agents.
     */
    private static final int ENVIRONMENT_ORDERING = AGENT_ORDERING + 1;

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
	return addEntity(new EnvironmentFactory(definition), ENVIRONMENT_ORDERING);
    }

    /**
     * Creates fish population according to SpeciesDefinitions.
     * 
     * @see #createFish(SpeciesDefinition, Entity)
     * @see SpeciesDefinition#getInitialNum()
     * @param environment
     *            entity representing the environment the fish are placed into
     * @param speciesDefs
     */
    public void createFishPopulation(Entity environment, Collection<SpeciesDefinition> speciesDefs) {
	for (SpeciesDefinition speciesDefinition : speciesDefs) {
	    for (int i = 0; i < speciesDefinition.getInitialNum(); i++) {
		// TODO randomize age to create heterogeneous population
		createFish(speciesDefinition, environment);
	    }
	}
    }

    /**
     * Create a new fish at a random position within their spawn habitat and add
     * it to schedule and agent field.
     * 
     * @param definition
     *            species definition of the fish
     * @param environment
     *            entity representing the environment the fish is placed into
     * @return fish entity
     */
    public Entity createFish(SpeciesDefinition definition, Entity environment) {
	return addEntity(new FishFactory(definition, environment), AGENT_ORDERING);
    }
}
