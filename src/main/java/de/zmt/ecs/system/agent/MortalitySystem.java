package de.zmt.ecs.system.agent;

import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.component.environment.*;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.sim.*;
import de.zmt.sim.params.def.*;
import de.zmt.util.UnitConstants;

/**
 * This system kills agents according to mortality risks.
 * 
 * @author cmeyer
 *
 */
public class MortalitySystem extends AgentSystem {
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
	 * NOTE: Habitat mortality risks were converted from per day to per
	 * step. This will lead to a slightly different number of deaths per
	 * day, because dead fish are subtracted from total number immediately.
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
