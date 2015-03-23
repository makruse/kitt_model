package test;

import static javax.measure.unit.SI.KILOGRAM;
import static org.junit.Assert.*;

import java.util.logging.Logger;

import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

import de.zmt.kitt.util.AmountUtil;
import de.zmt.storage.LimitedStorage;

public class TestLimitedStorage {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(TestLimitedStorage.class.getName());

    private static final Amount<Mass> LOWER_LIMIT = Amount.valueOf(10.5,
	    KILOGRAM);
    private static final Amount<Mass> UPPER_LIMIT = Amount.valueOf(50.7,
	    KILOGRAM);

    private static final double FACTOR_IN = 1.5;
    private static final double FACTOR_OUT = 0.8;

    // TEST STORE AMOUNT
    private static final Amount<Mass> CHANGE_AMOUNT = Amount.valueOf(15.4,
	    KILOGRAM);
    private static final Amount<Mass> STORED_IN = CHANGE_AMOUNT
	    .times(FACTOR_IN);
    private static final Amount<Mass> STORED_OUT = CHANGE_AMOUNT.opposite()
	    .times(FACTOR_OUT);
    private static final Amount<Mass> RETRIEVED = LOWER_LIMIT.plus(STORED_IN)
	    .plus(STORED_OUT).minus(LOWER_LIMIT).times(FACTOR_OUT);

    // TEST REJECT AMOUNT
    private static final Amount<Mass> RANGE = UPPER_LIMIT.minus(LOWER_LIMIT);
    private static final Amount<Mass> EXCESS_CHANGE_AMOUNT = RANGE
	    .plus(CHANGE_AMOUNT);
    private static final Amount<Mass> REJECTED_IN = ((EXCESS_CHANGE_AMOUNT
	    .times(FACTOR_IN)).minus(RANGE)).divide(FACTOR_IN);
    private static final Amount<Mass> REJECTED_OUT = (EXCESS_CHANGE_AMOUNT
	    .opposite().times(FACTOR_OUT).plus(RANGE)).divide(FACTOR_OUT);

    @Test
    public void testWithLimits() {
	LimitedStorage<Mass> storage = new LimitedStorage<Mass>(KILOGRAM, true) {

	    @Override
	    protected Amount<Mass> getLowerLimit() {
		return LOWER_LIMIT;
	    }

	    @Override
	    protected Amount<Mass> getUpperLimit() {
		return UPPER_LIMIT;
	    }

	    @Override
	    protected double getFactorIn() {
		return FACTOR_IN;
	    }

	    @Override
	    protected double getFactorOut() {
		return FACTOR_OUT;
	    }

	};
	// initialize
	logger.fine(storage.toString());
	assertEquals("Storage not initialized to lower limit: " + LOWER_LIMIT,
		LOWER_LIMIT, storage.getAmount());

	// add amount
	logger.fine("adding " + CHANGE_AMOUNT);
	Amount<Mass> storedIn = storage.add(CHANGE_AMOUNT).getStored();
	assertEquals("Storage did not store correct amount: ", STORED_IN,
		storedIn);

	// subtract amount
	logger.fine("subtracting " + CHANGE_AMOUNT);
	Amount<Mass> storedOut = storage.add(CHANGE_AMOUNT.opposite())
		.getStored();
	assertEquals("Storage did not store correct amount: ", STORED_OUT,
		storedOut);

	assertEquals(
		"Amount retrieved when clearing storage was not correct: ",
		RETRIEVED, storage.clear());

	// add excess amount
	logger.fine("adding " + EXCESS_CHANGE_AMOUNT);
	Amount<Mass> rejectedIn = storage.add(EXCESS_CHANGE_AMOUNT)
		.getRejected();
	assertEquals("Storage did not reject correct amount: ", REJECTED_IN,
		rejectedIn);

	// check for upper limit
	logger.fine(storage.toString());
	assertTrue("Storage not at upper limit.", storage.atUpperLimit());

	// subtract excess amount
	logger.fine("subtracting " + EXCESS_CHANGE_AMOUNT);
	Amount<Mass> rejectedOut = storage.add(EXCESS_CHANGE_AMOUNT.opposite())
		.getRejected();
	assertEquals("Storage did not reject correct amount: ", REJECTED_OUT,
		rejectedOut);

	// check for lower limit
	logger.fine(storage.toString());
	assertTrue("Storage not at lower limit", storage.atLowerLimit());
    }

    /*
     * Need to multiply with factor each time. Otherwise actual result differ in
     * error value.
     */
    private static final Amount<Mass> RESULT = AmountUtil.zero(CHANGE_AMOUNT)
	    .plus(CHANGE_AMOUNT.times(1d)).plus(CHANGE_AMOUNT.times(1d))
	    .plus(CHANGE_AMOUNT.opposite().times(1d));

    @Test
    public void testWithoutLimits() {
	LimitedStorage<Mass> storage = new LimitedStorage<Mass>(KILOGRAM, true);

	storage.add(CHANGE_AMOUNT);
	storage.add(CHANGE_AMOUNT);
	storage.add(CHANGE_AMOUNT.opposite());

	assertEquals(RESULT, storage.getAmount());
    }

}
