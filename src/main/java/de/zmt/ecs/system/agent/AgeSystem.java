package de.zmt.ecs.system.agent;

import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.environment.SimulationTimeSystem;
import de.zmt.sim.engine.Kitt;
import de.zmt.sim.params.def.*;

/**
 * Adds passed time to age. If the agent is beyond maximum age it will die.
 * @author mey
 *
 */
public class AgeSystem extends AgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AgeSystem.class
	    .getName());

    public AgeSystem(Kitt sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	Amount<Duration> delta = EnvironmentDefinition.STEP_DURATION;

	// increase age
	Aging aging = entity.get(Aging.class);
	Amount<Duration> newAge = aging.addAge(delta);
	if (newAge.isGreaterThan(entity.get(SpeciesDefinition.class)
		.getMaxAge())) {
	    killAgent(entity, CauseOfDeath.OLD_AGE);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Aging.class,
		SpeciesDefinition.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(SimulationTimeSystem.class);
    }

}
