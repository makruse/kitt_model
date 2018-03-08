package de.zmt.ecs.system.agent;

import java.util.logging.Logger;

import de.zmt.ecs.AbstractSystem;
import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.factory.FishFactory;
import de.zmt.output.LifeCyclingData;
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
     * @param causeOfDeath
     */
    protected void killAgent(Entity agent, CauseOfDeath causeOfDeath) {
        if (agent.has(LifeCycling.class)) {
            // preferably use the species name
            String agentString = agent.has(SpeciesDefinition.class) ? agent.get(SpeciesDefinition.class).getName()
                    : agent.toString();
            logger.fine(agentString + causeOfDeath.getMessage());
            agent.get(LifeCycling.class).die(causeOfDeath);
            try{
                Growing growing = agent.get(Growing.class);
                Aging aging = agent.get(Aging.class);
                LifeCycling lifeCycling = agent.get(LifeCycling.class);
                LifeCyclingData.registerPhaseChange(FishFactory.getIDForEntity(agent),aging.getAge(),growing.getLength(),
                                                    lifeCycling.getSex(),lifeCycling.getPhase(), causeOfDeath);
            } catch (IllegalArgumentException e){
                e.printStackTrace();
                System.out.println("This agent has no aging or growing component");
            }
        }
        agent.stop();
    }
}
