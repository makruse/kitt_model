package de.zmt.storage;

import static javax.measure.unit.NonSI.HOUR;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.util.UnitConstants;
import de.zmt.util.ValuableAmountAdapter;
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

    private static final double DESIRED_EXCESS_RMR_VALUE = 5;
    /**
     * Excess desired storage capacity on RMR:<br>
     * {@value #DESIRED_EXCESS_RMR_VALUE}h
     * <p>
     * Fish will be hungry until desired excess is achieved.
     */
    private static final Amount<Duration> DESIRED_EXCESS_RMR = Amount.valueOf(DESIRED_EXCESS_RMR_VALUE, HOUR);

    private final Metabolizing metabolizing;

    public ExcessStorage(Metabolizing metabolizing) {
	super();
	this.metabolizing = metabolizing;
    }

    /**
     * @return <code>true</code> if storage is at or beyond the desired amount
     */
    public boolean atDesired() {
	Amount<Energy> desiredAmount = getDesired();
	return getAmount().compareTo(desiredAmount) >= 0;
    }

    private Amount<Energy> getDesired() {
	Amount<Energy> desiredAmount = ExcessStorage.DESIRED_EXCESS_RMR
		.times(metabolizing.getRestingMetabolicRate()).to(UnitConstants.CELLULAR_ENERGY);
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
	    return ValuableAmountAdapter.wrap(ExcessStorage.this.getDesired());
	}
    }
}