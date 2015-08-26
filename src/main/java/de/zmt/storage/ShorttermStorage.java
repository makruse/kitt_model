package de.zmt.storage;

import static javax.measure.unit.NonSI.HOUR;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.Compartments.AbstractCompartmentStorage;

public class ShorttermStorage extends AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    private static final double SHORTTERM_MAX_CAPACITY_SMR_VALUE = 9;
    /**
     * Short-term maximum storage capacity on SMR:<br>
     * {@value #SHORTTERM_MAX_CAPACITY_SMR_VALUE}h
     */
    private static final Amount<Duration> SHORTTERM_MAX_CAPACITY_SMR = Amount
	    .valueOf(SHORTTERM_MAX_CAPACITY_SMR_VALUE, HOUR);

    private final Metabolizing smrComp;

    public ShorttermStorage(Metabolizing metabolizing) {
	super();
	// short-term is full at startup
	this.smrComp = metabolizing;
	amount = getUpperLimit();
    }

    @Override
    protected Amount<Energy> getUpperLimit() {
	return smrComp.getStandardMetabolicRate().times(SHORTTERM_MAX_CAPACITY_SMR)
		.to(amount.getUnit());
    }

    @Override
    public Type getType() {
	return Type.SHORTTERM;
    }

}