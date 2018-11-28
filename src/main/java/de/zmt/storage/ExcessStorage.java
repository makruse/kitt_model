package de.zmt.storage;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.util.UnitConstants;
import sim.util.AmountValuable;
import sim.util.Valuable;

/**
 * Excess storage having no limit but a desired amount which is used for
 * determining if the agent is hungry.
 * 
 * @author mey
 *
 */
public class ExcessStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    private final Metabolizing metabolizing;
    /**
     * Excess desired storage capacity on RMR. Fish will be hungry until desired
     * excess is achieved.
     */
    private final Amount<Duration> desiredExcessRmr;

    public ExcessStorage(Metabolizing metabolizing, Amount<Duration> desiredExcessRmr) {
        super();
        this.metabolizing = metabolizing;
        this.desiredExcessRmr = desiredExcessRmr;
    }

    /**
     * @return <code>true</code> if storage is at or beyond the desired amount
     */
    public boolean atDesired() {
        Amount<Energy> desiredAmount = getDesired();
        return getAmount().compareTo(desiredAmount) >= 0;
    }

    @Override
    public Amount<Energy> getUpperLimit(){
        return getDesired();
    }

    private Amount<Energy> getDesired() {
        Amount<Energy> desiredAmount = desiredExcessRmr.times(metabolizing.getRestingMetabolicRate())
                .to(UnitConstants.CELLULAR_ENERGY);
        return desiredAmount;
    }

    @Override
    public Type getType() {
        return Type.EXCESS;
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy extends ConfigurableStorage<Energy>.MyPropertiesProxy {
        public Valuable getDesired() {
            return AmountValuable.wrap(ExcessStorage.this.getDesired());
        }
    }
}