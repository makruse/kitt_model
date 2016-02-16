package de.zmt.storage;

import static javax.measure.unit.NonSI.HOUR;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Metabolizing;

public class ShorttermStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    private static final double UPPER_LIMIT_RMR_HOUR_VALUE = 9;
    /**
     * Short-term maximum storage capacity on RMR.
     * 
     * @see #getUpperLimit()
     */
    private static final Amount<Duration> UPPER_LIMIT_RMR = Amount
	    .valueOf(UPPER_LIMIT_RMR_HOUR_VALUE, HOUR);

    private final Metabolizing metabolizing;

    /**
     * Constructs a new {@link ShorttermStorage}.
     * 
     * @param metabolizing
     *            the {@link Metabolizing} of the entity this storage belongs to
     * @param fillLevel
     *            value between 0-1 defining the initial fill level between
     *            lower and upper limit
     */
    public ShorttermStorage(Metabolizing metabolizing, double fillLevel) {
	super();

	this.metabolizing = metabolizing;
	fill(fillLevel);
    }

    /**
     * Lower limit as duration that RMR can be maintained:<br>
     * {@value #UPPER_LIMIT_RMR_HOUR_VALUE}h &sdot; RMR
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
	return UPPER_LIMIT_RMR.times(metabolizing.getRestingMetabolicRate()).to(getAmount().getUnit());
    }

    @Override
    public Type getType() {
	return Type.SHORTTERM;
    }

}