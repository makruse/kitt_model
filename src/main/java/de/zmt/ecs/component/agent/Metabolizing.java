package de.zmt.ecs.component.agent;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import de.zmt.util.ValuableAmountAdapter;
import sim.util.Proxiable;
import sim.util.Valuable;

/**
 * Entities having this component can metabolize energy, i.e. ingest and consume
 * it.
 * 
 * @author mey
 *
 */
public class Metabolizing implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Current kind of activity the fish is doing. */
    private BehaviorMode behaviorMode;
    /**
     * Energy ingested over the last step, including loss when entering the gut.
     */
    private Amount<Energy> netEnergy = AmountUtil.zero(UnitConstants.CELLULAR_ENERGY);
    /** Energy consumed over the last step. */
    private Amount<Energy> consumedEnergy = AmountUtil.zero(UnitConstants.CELLULAR_ENERGY);
    /** Current resting metabolic rate. */
    private Amount<Power> restingMetabolicRate = AmountUtil.zero(UnitConstants.ENERGY_PER_TIME);

    private boolean hungry;

    public Metabolizing(Amount<Power> initialRestingMetabolicRate) {
	this.restingMetabolicRate = initialRestingMetabolicRate;
    }

    public BehaviorMode getBehaviorMode() {
	return behaviorMode;
    }

    public void setBehaviorMode(BehaviorMode behaviorMode) {
	this.behaviorMode = behaviorMode;
    }

    public void setNetEnergy(Amount<Energy> netEnergy) {
	this.netEnergy = netEnergy;
    }

    public void setConsumedEnergy(Amount<Energy> consumedEnergy) {
	this.consumedEnergy = consumedEnergy;
    }

    public Amount<Power> getRestingMetabolicRate() {
	return restingMetabolicRate;
    }

    public void setRestingMetabolicRate(Amount<Power> restingMetabolicRate) {
	this.restingMetabolicRate = restingMetabolicRate;
    }

    public boolean isHungry() {
	return hungry;
    }

    public void setHungry(boolean hungry) {
	this.hungry = hungry;
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + " [behaviorMode=" + behaviorMode + ", restingMetabolicRate="
		+ restingMetabolicRate + "]";
    }

    public class MyPropertiesProxy {
	public BehaviorMode getBehaviorMode() {
	    return behaviorMode;
	}

	public boolean isHungry() {
	    return hungry;
	}

	public Valuable getNetEnergy() {
	    return ValuableAmountAdapter.wrap(netEnergy);
	}

	public Valuable getConsumedEnergy() {
	    return ValuableAmountAdapter.wrap(consumedEnergy);
	}

	public Valuable getRestingMetabolicRate() {
	    return ValuableAmountAdapter.wrap(restingMetabolicRate);
	}
    }

    /** Behavioral modes of a simulation object. */
    public static enum BehaviorMode {
	FORAGING, MIGRATING, RESTING;

	private static final double COST_FACTOR_FORAGING = 4.3;
	// TODO cost factor for migrating
	private static final double COST_FACTOR_RESTING = 1.6;

	/**
	 * Fish needs {@code costFactor * RMR} to maintain this behavior mode.
	 * 
	 * @return cost factor on RMR
	 */
	public double getCostFactor() {
	    switch (this) {
	    case FORAGING:
		return COST_FACTOR_FORAGING;
	    case RESTING:
		return COST_FACTOR_RESTING;
	    default:
		throw new IllegalStateException("No cost factor available for " + this);
	    }
	}
    }
}
