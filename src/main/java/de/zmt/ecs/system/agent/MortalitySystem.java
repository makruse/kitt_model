package de.zmt.ecs.system.agent;

import java.util.*;
import java.util.logging.Logger;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.*;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.util.*;
import sim.engine.Kitt;
import sim.params.def.*;

/**
 * This system kills agents according to mortality risks.
 * 
 * @author cmeyer
 *
 */
public class MortalitySystem extends AgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(MortalitySystem.class.getName());

    public MortalitySystem(Kitt sim) {
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
	Habitat habitat = getEnvironment().get(HabitatMap.class).obtainHabitat(entity.get(Moving.class).getPosition(),
		getEnvironment().get(EnvironmentDefinition.class));
	if (getRandom().nextBoolean(habitat.getMortalityRisk().doubleValue(UnitConstants.PER_STEP))) {
	    killAgent(entity, CauseOfDeath.HABITAT);
	}
	// check for random mortality just once per day
	else if (getEnvironment().get(SimulationTime.class).isFirstStepInDay() && getRandom().nextBoolean(
		entity.get(SpeciesDefinition.class).getMortalityRisk().doubleValue(UnitConstants.PER_DAY))) {
	    killAgent(entity, CauseOfDeath.RANDOM);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Moving.class, SpeciesDefinition.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(MoveSystem.class);
    }
}
