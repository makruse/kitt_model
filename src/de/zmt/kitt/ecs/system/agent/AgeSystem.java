package de.zmt.kitt.ecs.system.agent;

import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.ecs.component.agent.Reproducing.CauseOfDeath;
import de.zmt.kitt.ecs.system.AbstractAgentSystem;
import de.zmt.kitt.sim.KittSim;
import de.zmt.kitt.sim.params.def.*;
import ecs.*;

public class AgeSystem extends AbstractAgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AgeSystem.class
	    .getName());

    public AgeSystem(KittSim sim) {
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

}
