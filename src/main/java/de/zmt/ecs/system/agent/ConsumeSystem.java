package de.zmt.ecs.system.agent;

import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.Reproducing.CauseOfDeath;
import de.zmt.ecs.system.AbstractAgentSystem;
import de.zmt.sim.KittSim;
import de.zmt.sim.params.def.EnvironmentDefinition;
import de.zmt.util.UnitConstants;

/**
 * Let entities consume needed energy from body compartments.
 * 
 * @author cmeyer
 * 
 */
public class ConsumeSystem extends AbstractAgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ConsumeSystem.class
	    .getName());

    public ConsumeSystem(KittSim sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	Metabolizing metabolizing = entity.get(Metabolizing.class);

	Amount<Energy> consumedEnergy = metabolizing.getStandardMetabolicRate()
		.times(EnvironmentDefinition.STEP_DURATION)
		.times(metabolizing.getActivityType().getCostFactor())
		.to(UnitConstants.CELLULAR_ENERGY);

	// subtract needed energy from compartments
	Amount<Energy> energyNotProvided = entity
		.get(Compartments.class)
		.add(consumedEnergy.opposite()).getRejected();

	metabolizing.setConsumedEnergy(consumedEnergy);

	// if the needed energy is not available the fish has starved to death
	if (energyNotProvided.getEstimatedValue() < 0) {
	    killAgent(entity, CauseOfDeath.STARVATION);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Metabolizing.class,
		Compartments.class);
    }
}
