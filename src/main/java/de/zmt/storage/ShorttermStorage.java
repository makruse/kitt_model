package de.zmt.storage;

import static javax.measure.unit.NonSI.HOUR;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Metabolizing;

public class ShorttermStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    private static final double SHORTTERM_UPPER_LIMIT_RMR_HOUR_VALUE = 9;
    /**
     * Short-term maximum storage capacity on RMR.
     * 
     * @see #getUpperLimit()
     */
    private static final Amount<Duration> SHORTTERM_UPPER_LIMIT_RMR = Amount
	    .valueOf(SHORTTERM_UPPER_LIMIT_RMR_HOUR_VALUE, HOUR);

    private final Metabolizing metabolizing;

    public ShorttermStorage(Metabolizing metabolizing) {
	super();
	// short-term is full at startup
	this.metabolizing = metabolizing;
	setAmount(getUpperLimit());
    }

    /**
     * Lower limit as duration that RMR can be maintained:<br>
     * {@value #SHORTTERM_UPPER_LIMIT_RMR_HOUR_VALUE}h &sdot; RMR
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
	return SHORTTERM_UPPER_LIMIT_RMR.times(metabolizing.getRestingMetabolicRate()).to(getAmount().getUnit());
    }

    @Override
    public Type getType() {
	return Type.SHORTTERM;
    }

}