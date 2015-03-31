package test;

import static org.junit.Assert.*;
import static test.resources.LimitedStorage.*;

import java.util.logging.Logger;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

import test.resources.LimitedStorage;
import de.zmt.kitt.util.AmountUtil;
import de.zmt.storage.ConfigurableStorage;

public class TestConfigurableStorage {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(TestConfigurableStorage.class.getName());

    // TEST STORE AMOUNT
    private static final Amount<Dimensionless> CHANGE_AMOUNT = Amount.valueOf(
	    15.4,
 Unit.ONE);
    private static final Amount<Dimensionless> STORED_IN = CHANGE_AMOUNT
	    .times(FACTOR_IN);
    private static final Amount<Dimensionless> STORED_OUT = CHANGE_AMOUNT
	    .opposite()
	    .times(FACTOR_OUT);
    private static final Amount<Dimensionless> RETRIEVED = LOWER_LIMIT
	    .plus(STORED_IN)
	    .plus(STORED_OUT).minus(LOWER_LIMIT).times(FACTOR_OUT);

    // TEST REJECT AMOUNT
    private static final Amount<Dimensionless> RANGE = UPPER_LIMIT
	    .minus(LOWER_LIMIT);
    private static final Amount<Dimensionless> EXCESS_CHANGE_AMOUNT = RANGE
	    .plus(CHANGE_AMOUNT);
    private static final Amount<Dimensionless> REJECTED_IN = ((EXCESS_CHANGE_AMOUNT
	    .times(FACTOR_IN)).minus(RANGE)).divide(FACTOR_IN);
    private static final Amount<Dimensionless> REJECTED_OUT = (EXCESS_CHANGE_AMOUNT
	    .opposite().times(FACTOR_OUT).plus(RANGE)).divide(FACTOR_OUT);

    @Test
    public void testWithLimits() {
	@SuppressWarnings("serial")
	ConfigurableStorage<Dimensionless> storage = new LimitedStorage();

	// initialize
	logger.fine(storage.toString());
	assertEquals("Storage not initialized to lower limit: " + LOWER_LIMIT,
		LOWER_LIMIT, storage.getAmount());

	// add amount
	logger.fine("adding " + CHANGE_AMOUNT);
	Amount<Dimensionless> storedIn = storage.add(CHANGE_AMOUNT).getStored();
	assertEquals("Storage did not store correct amount: ", STORED_IN,
		storedIn);

	// subtract amount
	logger.fine("subtracting " + CHANGE_AMOUNT);
	Amount<Dimensionless> storedOut = storage.add(CHANGE_AMOUNT.opposite())
		.getStored();
	assertEquals("Storage did not store correct amount: ", STORED_OUT,
		storedOut);

	assertEquals(
		"Amount retrieved when clearing storage was not correct: ",
		RETRIEVED, storage.clear());

	// add excess amount
	logger.fine("adding " + EXCESS_CHANGE_AMOUNT);
	Amount<Dimensionless> rejectedIn = storage.add(EXCESS_CHANGE_AMOUNT)
		.getRejected();
	assertEquals("Storage did not reject correct amount: ", REJECTED_IN,
		rejectedIn);

	// check for upper limit
	logger.fine(storage.toString());
	assertTrue("Storage not at upper limit.", storage.atUpperLimit());

	// subtract excess amount
	logger.fine("subtracting " + EXCESS_CHANGE_AMOUNT);
	Amount<Dimensionless> rejectedOut = storage.add(
		EXCESS_CHANGE_AMOUNT.opposite())
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
    private static final Amount<Dimensionless> RESULT = AmountUtil
	    .zero(CHANGE_AMOUNT)
	    .plus(CHANGE_AMOUNT.times(1d)).plus(CHANGE_AMOUNT.times(1d))
	    .plus(CHANGE_AMOUNT.opposite().times(1d));

    @Test
    public void testWithoutLimits() {
	ConfigurableStorage<Dimensionless> storage = new ConfigurableStorage<Dimensionless>(
		Unit.ONE, true);
	storage.add(CHANGE_AMOUNT);
	storage.add(CHANGE_AMOUNT);
	storage.add(CHANGE_AMOUNT.opposite());

	assertEquals(RESULT, storage.getAmount());
    }

}
