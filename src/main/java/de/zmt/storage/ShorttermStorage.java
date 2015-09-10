package de.zmt.storage;

import static javax.measure.unit.NonSI.HOUR;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Metabolizing;

public class ShorttermStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    private static final double SHORTTERM_UPPER_LIMIT_SMR_HOUR_VALUE = 9;
    /**
     * Short-term maximum storage capacity on SMR.
     * 
     * @see #getUpperLimit()
     */
    private static final Amount<Duration> SHORTTERM_UPPER_LIMIT_SMR = Amount
	    .valueOf(SHORTTERM_UPPER_LIMIT_SMR_HOUR_VALUE, HOUR);

    private final Metabolizing metabolizing;

    public ShorttermStorage(Metabolizing metabolizing) {
	super();
	// short-term is full at startup
	this.metabolizing = metabolizing;
	amount = getUpperLimit();
    }

    /**
     * Lower limit as duration that SMR can be maintained:<br>
     * {@value #SHORTTERM_UPPER_LIMIT_SMR_HOUR_VALUE}h &sdot; SMR
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
	return SHORTTERM_UPPER_LIMIT_SMR.times(metabolizing.getStandardMetabolicRate()).to(amount.getUnit());
    }

    @Override
    public Type getType() {
	return Type.SHORTTERM;
    }

}