package de.zmt.ecs.factory;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.*;
import de.zmt.ecs.factory.FishComponentsFactory.Parameters;
import ec.util.MersenneTwisterFast;
import sim.engine.*;
import sim.params.def.*;
import sim.portrayal.*;
import sim.util.*;

/**
 * Creates fish (agent) and environment (fields / grids) entities. Needed
 * components are added and their values set to an initial state.
 * 
 * @author cmeyer
 *
 */
public class EntityFactory implements Serializable {
    @SuppressWarnings("unused")
    static final Logger logger = Logger.getLogger(EntityFactory.class.getName());
    private static final long serialVersionUID = 1L;

    /** Ordering for agent entities in {@link Schedule}. */
    private static final int AGENT_ORDERING = 0;
    /**
     * Ordering for environment entities in {@link Schedule}. Needed to be
     * updated after agents.
     */
    private static final int ENVIRONMENT_ORDERING = AGENT_ORDERING + 1;
    private static final String ENVIRONMENT_ENTITY_NAME = "Environment";

    private final ComponentsFactory<EnvironmentComponentsFactory.Parameters> environmentComponentsFactory;
    private final ComponentsFactory<FishComponentsFactory.Parameters> fishComponentsFactory;

    private final EntityManager manager;
    private final MersenneTwisterFast random;
    private final Schedule schedule;

    private final Collection<EntityCreationListener> listeners = new LinkedList<>();

    public EntityFactory(EntityManager entityManager, SimState state) {
	this.manager = entityManager;
	this.random = state.random;
	this.schedule = state.schedule;
	this.environmentComponentsFactory = new EnvironmentComponentsFactory(random);
	this.fishComponentsFactory = new FishComponentsFactory(random);
    }

    /**
     * Creates new environment entity and adds it to the schedule.
     * 
     * @param definition
     * @return environment entity
     */
    public Entity createEnvironment(EnvironmentDefinition definition) {
	Collection<Component> components = environmentComponentsFactory.createComponents(new de.zmt.ecs.factory.EnvironmentComponentsFactory.Parameters(definition));
	Entity environment = new Entity(manager, ENVIRONMENT_ENTITY_NAME, components);
	schedule.scheduleRepeating(schedule.getTime() + 1, environment, ENVIRONMENT_ORDERING);
	return environment;
    }

    /**
     * Create fish population according to SpeciesDefinitions.
     * 
     * @see #createFish(Entity, SpeciesDefinition, Amount)
     * @see SpeciesDefinition#getInitialNum()
     * @param environment
     * @param speciesDefs
     */
    public void createFishPopulation(Entity environment, Collection<SpeciesDefinition> speciesDefs) {
	for (SpeciesDefinition speciesDefinition : speciesDefs) {
	    for (int i = 0; i < speciesDefinition.getInitialNum(); i++) {
		// TODO randomize age to create heterogeneous population
		createFish(environment, speciesDefinition, SpeciesDefinition.getInitialAge());
	    }
	}
    }

    /**
     * Create a new fish at a random position within their spawn habitat and add
     * it to schedule and agent field.
     * 
     * @param environment
     *            entity with agent and habitat field
     * @param definition
     * @param initialAge
     *            initial age of the fish
     * @return fish entity
     */
    public Entity createFish(Entity environment, SpeciesDefinition definition, Amount<Duration> initialAge) {
	Int2D randomHabitatPosition = environment.get(HabitatMap.class).generateRandomPosition(random,
		SpeciesDefinition.getSpawnHabitat());
	Double2D position = environment.get(EnvironmentDefinition.class).mapToWorld(randomHabitatPosition);
	return createFish(environment, definition, position, initialAge);
    }

    /**
     * Create a new fish and add it to schedule and agent field.
     * 
     * @param environment
     *            entity with agent and habitat field
     * @param definition
     * @param position
     *            position where the fish spawns
     * @param initialAge
     *            initial age of the fish
     * @return fish entity
     */
    public Entity createFish(Entity environment, SpeciesDefinition definition, Double2D position,
	    Amount<Duration> initialAge) {
	final AgentWorld agentWorld = environment.get(AgentWorld.class);
	HabitatMap habitatMap = environment.get(HabitatMap.class);

	// gather fish components
	Collection<Component> components = fishComponentsFactory.createComponents(new Parameters(definition, position, initialAge, agentWorld, habitatMap, environment.get(EnvironmentDefinition.class)));
	final Entity fish = new FishEntity(manager, definition.getSpeciesName(), components);

	// add fish to schedule and field
	agentWorld.addAgent(fish);
	final Stoppable scheduleStoppable = schedule.scheduleRepeating(schedule.getTime() + 1.0, AGENT_ORDERING, fish);

	// create stoppable triggering removal of fish from schedule and field
	fish.setStoppable(new Stoppable() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void stop() {
		scheduleStoppable.stop();
		agentWorld.removeAgent(fish);

		// notify listeners of removal
		for (EntityCreationListener listener : listeners) {
		    listener.onRemoveFish(fish);
		}
	    }
	});

	// notify listeners of creation
	for (EntityCreationListener listener : listeners) {
	    listener.onCreateFish(fish);
	}
	return fish;
    }

    public boolean addListener(EntityCreationListener listener) {
	return listeners.add(listener);
    }

    public boolean removeListener(EntityCreationListener listener) {
	return listeners.remove(listener);
    }

    public EntityManager getManager() {
	return manager;
    }

    /**
     * Implements {@link Oriented2D} for display.
     * 
     * @author cmeyer
     * 
     */
    private static class FishEntity extends Entity implements Fixed2D, Oriented2D {
	private static final long serialVersionUID = 1L;

	public FishEntity(EntityManager manager, String internalName, Collection<Component> components) {
	    super(manager, internalName, components);
	}

	@Override
	public double orientation2D() {
	    return get(Moving.class).getVelocity().angle();
	}

	@Override
	public boolean maySetLocation(Object field, Object newObjectLocation) {
	    get(Moving.class).setPosition((Double2D) newObjectLocation);

	    return true;
	}
    }

    /**
     * Listener interface for receiving notifications when entities are created
     * and removed.
     * 
     * @author mey
     *
     */
    public static interface EntityCreationListener {
	/**
	 * Invoked when a fish was created.
	 * 
	 * @param fish
	 *            fish entity
	 */
	void onCreateFish(Entity fish);

	/**
	 * Invoked when a fish was removed.
	 * 
	 * @param fish
	 */
	void onRemoveFish(Entity fish);
    }
}
