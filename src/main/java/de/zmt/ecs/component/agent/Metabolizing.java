package de.zmt.ecs.component.agent;

import static javax.measure.unit.SI.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.util.*;
import sim.util.Proxiable;

/**
 * Entities having this component can metabolize energy, i.e. ingest and consume
 * it.
 * 
 * @author cmeyer
 *
 */
public class Metabolizing implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Current kind of activity the fish is doing. */
    private BehaviorMode behaviorMode;
    /** Energy ingested over the last step. */
    private Amount<Energy> ingestedEnergy = AmountUtil.zero(UnitConstants.CELLULAR_ENERGY);
    /** Energy consumed over the last step. */
    private Amount<Energy> consumedEnergy = AmountUtil.zero(UnitConstants.CELLULAR_ENERGY);
    /** Current standard metabolic rate. */
    private Amount<Power> standardMetabolicRate = AmountUtil.zero(UnitConstants.ENERGY_PER_TIME);

    private boolean hungry;

    public Metabolizing(Amount<Power> initialStandardMetabolicRate) {
	this.standardMetabolicRate = initialStandardMetabolicRate;
    }

    public BehaviorMode getBehaviorMode() {
	return behaviorMode;
    }

    public void setBehaviorMode(BehaviorMode behaviorMode) {
	this.behaviorMode = behaviorMode;
    }

    public void setIngestedEnergy(Amount<Energy> ingestedEnergy) {
	this.ingestedEnergy = ingestedEnergy;
    }

    public void setConsumedEnergy(Amount<Energy> consumedEnergy) {
	this.consumedEnergy = consumedEnergy;
    }

    public Amount<Power> getStandardMetabolicRate() {
	return standardMetabolicRate;
    }

    public void setStandardMetabolicRate(Amount<Power> standardMetabolicRate) {
	this.standardMetabolicRate = standardMetabolicRate;
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
	return "Metabolizing [behaviorMode=" + behaviorMode + ", standardMetabolicRate=" + standardMetabolicRate + "]";
    }

    public class MyPropertiesProxy {
	public BehaviorMode getBehaviorMode() {
	    return behaviorMode;
	}

	public boolean isHungry() {
	    return hungry;
	}

	public double getIngestedEnergy_kJ() {
	    return ingestedEnergy.doubleValue(KILO(JOULE));
	}

	public double getConsumedEnergy_kJ() {
	    return consumedEnergy.doubleValue(KILO(JOULE));
	}

	public double getStandardMetabolicRate() {
	    return standardMetabolicRate.getEstimatedValue();
	}

	public String nameStandardMetabolicRate() {
	    return "standardMetabolicRate_" + standardMetabolicRate.getUnit();
	}
    }

    /** Behavioral modes of a simulation object. */
    // TODO Add mode MIGRATING
    public static enum BehaviorMode {
	FORAGING, RESTING;

	private static final double COST_FACTOR_FORAGING = 4.3;
	private static final double COST_FACTOR_RESTING = 1.6;

	/**
	 * Fish needs {@code costFactor * SMR} to maintain this behavior mode.
	 * 
	 * @return cost factor on SMR
	 */
	public double getCostFactor() {
	    switch (this) {
	    case FORAGING:
		return COST_FACTOR_FORAGING;
	    case RESTING:
		return COST_FACTOR_RESTING;
	    default:
		return 1;
	    }
	}
    }
}
