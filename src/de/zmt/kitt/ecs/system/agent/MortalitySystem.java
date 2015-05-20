package de.zmt.kitt.ecs.system.agent;

import java.util.*;
import java.util.logging.Logger;

import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.ecs.component.agent.Reproducing.CauseOfDeath;
import de.zmt.kitt.ecs.component.environment.*;
import de.zmt.kitt.ecs.system.AbstractAgentSystem;
import de.zmt.kitt.sim.KittSim;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import de.zmt.kitt.util.UnitConstants;
import ecs.*;

public class MortalitySystem extends AbstractAgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(MortalitySystem.class
	    .getName());

    public MortalitySystem(KittSim sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	// check for death one time a day
	if (!environment.get(SimulationTime.class).isFirstStepInDay()) {
	    return;
	}

	// random mortality
	if (random.nextBoolean(entity.get(SpeciesDefinition.class)
		.getMortalityRisk().to(UnitConstants.PER_DAY)
		.getEstimatedValue())) {
	    killAgent(entity, CauseOfDeath.RANDOM);
	}
	// habitat mortality
	else if (random.nextBoolean(environment.get(HabitatField.class)
		.obtainHabitat(entity.get(Moving.class).getPosition())
		.getMortalityRisk().to(UnitConstants.PER_DAY)
		.getEstimatedValue())) {
	    killAgent(entity, CauseOfDeath.HABITAT);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Moving.class,
		SpeciesDefinition.class);
    }
}
