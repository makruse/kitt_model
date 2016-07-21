package de.zmt.ecs.system;

import java.util.logging.Logger;

import de.zmt.ecs.AbstractSystem;
import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.component.environment.AgentWorld;
import de.zmt.params.SpeciesDefinition;

/**
 * Super class for kitt Systems used to update agents.
 * 
 * @author mey
 *
 */
public abstract class AgentSystem extends AbstractSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AgentSystem.class.getName());

    /**
     * Kill the entity with given cause of death.
     * 
     * @param agent
     *            the agent to kill
     * @param agentWorld
     *            the {@link AgentWorld} the agent was set in
     * @param causeOfDeath
     */
    protected void killAgent(Entity agent, AgentWorld agentWorld, CauseOfDeath causeOfDeath) {
        if (agent.has(LifeCycling.class)) {
            // preferably use the species name
            String agentString = agent.has(SpeciesDefinition.class) ? agent.get(SpeciesDefinition.class).getName()
                    : agent.toString();
            logger.fine(agentString + causeOfDeath.getMessage());
            agent.get(LifeCycling.class).die(causeOfDeath);
        }
        agentWorld.removeAgent(agent);
        agent.stop();
    }
}
