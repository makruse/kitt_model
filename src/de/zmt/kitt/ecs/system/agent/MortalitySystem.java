package de.zmt.kitt.ecs.system.agent;

import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.ecs.component.agent.Reproducing.CauseOfDeath;
import de.zmt.kitt.ecs.component.environment.*;
import de.zmt.kitt.ecs.system.AbstractAgentSystem;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.params.def.*;
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
	// habitat mortality per step (because it changes all the time)
	/*
	 * NOTE: Mortality risks were converted from per day to per step. This
	 * will lead to a slightly different number of deaths per day, because
	 * dead fish are subtracted from total number immediately.
	 */
	Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(
		entity.get(Moving.class).getPosition(),
		environment.get(EnvironmentDefinition.class));
	Amount<Frequency> habitatMortalityRisk = habitat.getMortalityRisk().to(
		UnitConstants.PER_STEP);
	if (random.nextBoolean(habitatMortalityRisk.getEstimatedValue())) {
	    killAgent(entity, CauseOfDeath.HABITAT);
	}
	// check for random mortality just once per day
	else if (environment.get(SimulationTime.class).isFirstStepInDay()
		&& random.nextBoolean(entity.get(SpeciesDefinition.class)
			.getMortalityRisk().to(UnitConstants.PER_DAY)
			.getEstimatedValue())) {
	    killAgent(entity, CauseOfDeath.RANDOM);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Moving.class,
		SpeciesDefinition.class);
    }
}
