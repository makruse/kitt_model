package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.Compartments.TransferDigestedResult;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.agent.move.MoveSystem;
import de.zmt.util.FormulaUtil;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.params.def.EnvironmentDefinition;

/**
 * Compute consumed energy for this update cycle.
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
     * Calculate consumed energy from RMR and cost factor of behavior. Subtract
     * that energy from compartments and kill the agent if they lack available
     * energy.
     */
    @Override
    protected void systemUpdate(Entity entity) {
	Metabolizing metabolizing = entity.get(Metabolizing.class);
	LifeCycling lifeCycling = entity.get(LifeCycling.class);
	Compartments compartments = entity.get(Compartments.class);
	Moving moving = entity.get(Moving.class);

	Amount<Energy> consumedFromRMR = metabolizing.getRestingMetabolicRate()
		.times(EnvironmentDefinition.STEP_DURATION).to(UnitConstants.CELLULAR_ENERGY);
	Amount<Energy> consumedFromSwimming = FormulaUtil.netCostOfSwimming(moving.getSpeed())
		.times(EnvironmentDefinition.STEP_DURATION).to(UnitConstants.CELLULAR_ENERGY);
	Amount<Energy> consumedEnergy = consumedFromRMR.plus(consumedFromSwimming);

	metabolizing.setConsumedEnergy(consumedEnergy);

	// subtract needed energy from compartments
	TransferDigestedResult transferDigestedResult = compartments.transferDigested(lifeCycling.isReproductive(),
		consumedEnergy.opposite());
	metabolizing.setNetEnergy(transferDigestedResult.getNet());

	// if the needed energy is not available the fish starves to death
	if (transferDigestedResult.getRejected().getEstimatedValue() < 0) {
	    killAgent(entity, CauseOfDeath.STARVATION);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Metabolizing.class, Compartments.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(
		// for the current behavior mode
		BehaviorSystem.class,
		// for movement speed
		MoveSystem.class);
    }
}
