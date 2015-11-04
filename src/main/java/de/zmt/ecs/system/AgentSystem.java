package de.zmt.ecs.system;

import java.util.Arrays;
import java.util.logging.Logger;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.component.environment.*;
import ec.util.MersenneTwisterFast;
import sim.engine.Kitt;
import sim.params.def.SpeciesDefinition;

/**
 * Super class for kitt Systems used to update agents.
 * 
 * @author mey
 *
 */
public abstract class AgentSystem extends AbstractSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AgentSystem.class.getName());

    /** Entity representing the environment the agents are set into. */
    private final Entity environment;
    /** Random number generator for this simulation. */
    private final MersenneTwisterFast random;

    public AgentSystem(Kitt sim) {
	this.environment = sim.getEnvironment();
	this.random = sim.random;
	// assert we got a real environment here
	assert (getEnvironment().has(Arrays.asList(AgentWorld.class, EnvironmentalFlowMap.class, FoodMap.class,
		HabitatMap.class, SimulationTime.class)));
    }

    /**
     * Kill the entity with given cause of death.
     * 
     * @param agent
     * @param causeOfDeath
     */
    protected void killAgent(Entity agent, CauseOfDeath causeOfDeath) {
	// preferably use the species name
	String agentString = agent.has(SpeciesDefinition.class) ? agent.get(SpeciesDefinition.class).getSpeciesName()
		: agent.toString();
	logger.fine(agentString + causeOfDeath.getMessage());
	if (agent.has(LifeCycling.class)) {
	    agent.get(LifeCycling.class).die(causeOfDeath);
	}
	agent.stop();
    }

    /**
     * @return entity representing the environment the agents are set into
     */
    protected Entity getEnvironment() {
	return environment;
    }

    /**
     * @return random number generator
     */
    protected MersenneTwisterFast getRandom() {
	return random;
    }
}
