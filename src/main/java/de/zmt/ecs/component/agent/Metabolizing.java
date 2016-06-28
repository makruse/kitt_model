package de.zmt.ecs.component.agent;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.jscience.physics.amount.Amount;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.zmt.ecs.Component;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import sim.util.AmountValuable;
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

    private static final Amount<Energy> ZERO_ENERGY = AmountUtil.zero(UnitConstants.CELLULAR_ENERGY);

    /** Current kind of activity the fish is doing. */
    private BehaviorMode behaviorMode = BehaviorMode.RESTING;
    /** Energy ingested over the last step. */
    private Amount<Energy> ingestedEnergy = ZERO_ENERGY;
    /** Digested energy transferred to other compartments over this step. */
    private Amount<Energy> netEnergy = ZERO_ENERGY;
    /** Energy consumed over the last step. */
    private Amount<Energy> consumedEnergy = ZERO_ENERGY;
    /** Current resting metabolic rate. */
    private Amount<Power> restingMetabolicRate = AmountUtil.zero(UnitConstants.ENERGY_PER_TIME);
    /** <code>true</code> if currently feeding. */
    private boolean feeding = false;

    public Metabolizing(Amount<Power> initialRestingMetabolicRate) {
        this.restingMetabolicRate = initialRestingMetabolicRate;
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

    public boolean isFeeding() {
        return feeding;
    }

    public void setFeeding(boolean feeding) {
        this.feeding = feeding;
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

        public Valuable getIngestedEnergy() {
            return AmountValuable.wrap(ingestedEnergy);
        }

        public Valuable getNetEnergy() {
            return AmountValuable.wrap(netEnergy);
        }

        public Valuable getConsumedEnergy() {
            return AmountValuable.wrap(consumedEnergy);
        }

        public Valuable getRestingMetabolicRate() {
            return AmountValuable.wrap(restingMetabolicRate);
        }

        public boolean isFeeding() {
            return feeding;
        }

        @Override
        public String toString() {
            return Metabolizing.this.getClass().getSimpleName();
        }
    }

    /** Behavioral modes of a simulation object. */
    @XStreamAlias("BehaviorMode")
    public static enum BehaviorMode {
        /** Searches for food, feeds if hungry. */
        FORAGING,
        /** Searches for a certain habitat. Stay there after arrival. */
        MIGRATING,
        /** Regenerates, little to no activity. */
        RESTING;
    }
}
