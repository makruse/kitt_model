package de.zmt.ecs.system;

import java.util.logging.Logger;

import de.zmt.ecs.AbstractSystem;
import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.params.SpeciesDefinition;
import ec.util.MersenneTwisterFast;
import sim.engine.Kitt;

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
    }

    /**
     * Kill the entity with given cause of death.
     * 
     * @param agent
     * @param causeOfDeath
     */
    protected void killAgent(Entity agent, CauseOfDeath causeOfDeath) {
        if (agent.has(LifeCycling.class)) {
            // preferably use the species name
            String agentString = agent.has(SpeciesDefinition.class) ? agent.get(SpeciesDefinition.class).getName()
                    : agent.toString();
            logger.fine(agentString + causeOfDeath.getMessage());
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
