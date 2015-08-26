package de.zmt.kitt.ecs.system;

import java.util.Arrays;
import java.util.logging.Logger;

import de.zmt.ecs.Entity;
import de.zmt.kitt.ecs.component.agent.Reproducing;
import de.zmt.kitt.ecs.component.agent.Reproducing.CauseOfDeath;
import de.zmt.kitt.ecs.component.environment.*;
import de.zmt.kitt.sim.KittSim;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;

public abstract class AbstractAgentSystem extends AbstractKittSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(AbstractAgentSystem.class.getName());

    // TODO replace class with entity / components
    protected final Entity environment;
    protected final Schedule schedule;
    protected final MersenneTwisterFast random;

    public AbstractAgentSystem(KittSim sim) {
	this.environment = sim.getEnvironment();
	this.schedule = sim.schedule;
	this.random = sim.random;
	// assert we got a real environment here
	assert (environment.has(Arrays.asList(AgentWorld.class, FoodMap.class,
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
	String agentString = agent.has(SpeciesDefinition.class) ? agent.get(
		SpeciesDefinition.class).getSpeciesName() : agent.toString();
	logger.fine(agentString + causeOfDeath.getMessage());
	if (agent.has(Reproducing.class)) {
	    agent.get(Reproducing.class).die(causeOfDeath);
	}
	agent.stop();
    }
}
