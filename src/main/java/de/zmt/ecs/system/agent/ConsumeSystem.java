package de.zmt.ecs.system.agent;

import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.params.def.EnvironmentDefinition;

/**
 * Let entities consume needed energy from body compartments.
 * 
 * @author mey
 * 
 */
public class ConsumeSystem extends AgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ConsumeSystem.class.getName());

    public ConsumeSystem(Kitt sim) {
	super(sim);
    }

    /**
     * Calculate consumed energy from SMR and cost factor of behavior. Subtract
     * that energy from compartments and kill the agent if they lacks available
     * energy.
     */
    @Override
    protected void systemUpdate(Entity entity) {
	Metabolizing metabolizing = entity.get(Metabolizing.class);

	Amount<Energy> consumedEnergy = metabolizing.getStandardMetabolicRate()
		.times(EnvironmentDefinition.STEP_DURATION).times(metabolizing.getBehaviorMode().getCostFactor())
		.to(UnitConstants.CELLULAR_ENERGY);

	// subtract needed energy from compartments
	Amount<Energy> energyNotProvided = entity.get(Compartments.class).add(consumedEnergy.opposite()).getRejected();

	metabolizing.setConsumedEnergy(consumedEnergy);

	// if the needed energy is not available the fish has starved to death
	if (energyNotProvided.getEstimatedValue() < 0) {
	    killAgent(entity, CauseOfDeath.STARVATION);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Metabolizing.class, Compartments.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(FeedSystem.class);
    }
}
