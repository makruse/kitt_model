package de.zmt.storage;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Metabolizing;

public class ShorttermStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    private final Metabolizing metabolizing;
    /**
     * Short-term maximum storage capacity on RMR.
     * 
     * @see #getUpperLimit()
     */
    private final Amount<Duration> upperLimitRmr;

    /**
     * Constructs a new {@link ShorttermStorage}.
     * 
     * @param metabolizing
     *            the {@link Metabolizing} of the entity this storage belongs to
     * @param fillLevel
     *            value between 0-1 defining the initial fill level between
     *            lower and upper limit
     * @param upperLimitRmr
     *            the maximum storage capacity on RMR
     */
    public ShorttermStorage(Metabolizing metabolizing, double fillLevel, Amount<Duration> upperLimitRmr) {
        super();

        this.metabolizing = metabolizing;
        this.upperLimitRmr = upperLimitRmr;
        fill(fillLevel);
    }

    /**
     * Upper limit derived from duration that RMR can be maintained.
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
        return upperLimitRmr.times(metabolizing.getRestingMetabolicRate()).to(getAmount().getUnit());
    }

    @Override
    public Type getType() {
        return Type.SHORTTERM;
    }

}