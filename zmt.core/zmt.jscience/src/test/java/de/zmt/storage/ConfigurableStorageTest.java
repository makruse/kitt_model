package de.zmt.storage;

import static de.zmt.storage.LimitedTestStorage.*;
import static org.hamcrest.AmountCloseTo.amountCloseTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.logging.Logger;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

public class ConfigurableStorageTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ConfigurableStorageTest.class.getName());

    // TEST STORE AMOUNT
    private static final Amount<Dimensionless> CHANGE_AMOUNT = Amount.valueOf(15.4, Unit.ONE);
    private static final Amount<Dimensionless> STORED_IN = CHANGE_AMOUNT.times(FACTOR_IN);
    private static final Amount<Dimensionless> STORED_OUT = CHANGE_AMOUNT.opposite().times(FACTOR_OUT);
    private static final Amount<Dimensionless> RETRIEVED = Amount.ZERO.plus(STORED_IN).plus(STORED_OUT)
            .minus(LOWER_LIMIT).times(FACTOR_OUT);

    // TEST REJECT AMOUNT
    private static final Amount<Dimensionless> RANGE = UPPER_LIMIT.minus(LOWER_LIMIT);
    private static final Amount<Dimensionless> EXCESS_CHANGE_AMOUNT = RANGE.plus(CHANGE_AMOUNT);
    private static final Amount<Dimensionless> REJECTED_IN = ((EXCESS_CHANGE_AMOUNT.times(FACTOR_IN)).minus(RANGE))
            .divide(FACTOR_IN);
    private static final Amount<Dimensionless> REJECTED_OUT = (EXCESS_CHANGE_AMOUNT.opposite().times(FACTOR_OUT)
            .plus(RANGE)).divide(FACTOR_OUT);

    @Test
    public void add() {
        ConfigurableStorage<Dimensionless> storage = new ConfigurableStorage<>(Unit.ONE);
        storage.add(CHANGE_AMOUNT);
        storage.add(CHANGE_AMOUNT);
        storage.add(CHANGE_AMOUNT.opposite());

        assertThat(storage.getValue(), is(CHANGE_AMOUNT.getEstimatedValue()));
    }

    @Test
    public void addOnFactorsAndLimits() {
        ConfigurableStorage<Dimensionless> storage = new LimitedTestStorage();

        // add amount
        logger.info("adding " + CHANGE_AMOUNT);
        Amount<Dimensionless> storedIn = storage.add(CHANGE_AMOUNT).getStored();
        assertThat("Storage did not store correct amount: ", storedIn, is(amountCloseTo(STORED_IN)));

        // subtract amount
        logger.info("subtracting " + CHANGE_AMOUNT);
        Amount<Dimensionless> storedOut = storage.add(CHANGE_AMOUNT.opposite()).getStored();
        assertThat("Storage did not store correct amount: ", STORED_OUT, is(amountCloseTo(storedOut)));

        assertThat("Amount retrieved when clearing storage was not correct: ", RETRIEVED,
                is(amountCloseTo(storage.clear())));

        // add excess amount
        logger.info("adding " + EXCESS_CHANGE_AMOUNT);
        Amount<Dimensionless> rejectedIn = storage.add(EXCESS_CHANGE_AMOUNT).getRejected();
        assertThat("Storage did not reject correct amount: ", rejectedIn, is(amountCloseTo(REJECTED_IN)));

        // check for upper limit
        logger.info(storage.toString());
        assertTrue("Storage not at upper limit.", storage.atUpperLimit());

        // subtract excess amount
        logger.info("subtracting " + EXCESS_CHANGE_AMOUNT);
        Amount<Dimensionless> rejectedOut = storage.add(EXCESS_CHANGE_AMOUNT.opposite()).getRejected();
        assertThat("Storage did not reject correct amount: ", rejectedOut, is(amountCloseTo(REJECTED_OUT)));

        // check for lower limit
        logger.info(storage.toString());
        assertTrue("Storage not at lower limit", storage.atLowerLimit());
    }

    @Test
    public void store() {
        ConfigurableStorage<Dimensionless> storage = new ConfigurableStorage<>(Unit.ONE);
        storage.store(CHANGE_AMOUNT);
        storage.store(CHANGE_AMOUNT);
        storage.store(CHANGE_AMOUNT.opposite());

        assertThat(storage.getValue(), is(CHANGE_AMOUNT.getEstimatedValue()));

    }

    @Test
    public void storeOnFactors() {
        ConfigurableStorage<Dimensionless> storage = new LimitedTestStorage();

        assertThat("Storage did not report the correct required amount: ", storage.store(STORED_IN),
                is(amountCloseTo(CHANGE_AMOUNT)));
        assertThat("Storage did not store the correct amount: ", storage.getAmount(), is(amountCloseTo(STORED_IN)));

        assertThat("Storage did not report the correct required amount: ", storage.store(STORED_OUT),
                is(amountCloseTo(CHANGE_AMOUNT.opposite())));
        assertThat("Storage did not store the correct amount: ", storage.getAmount(),
                is(amountCloseTo(STORED_IN.plus(STORED_OUT))));

    }

    @Test
    public void storeOnLimits() {
        ConfigurableStorage<Dimensionless> storage = new LimitedTestStorage();

        assertNotNull(storage.store(UPPER_LIMIT));
        assertNull("Storage did not reject an offer exceeding upper limit: ", storage.store(CHANGE_AMOUNT));
        assertNull("Storage did not reject an offer exceeding lower limit: ", storage.store(UPPER_LIMIT.opposite()));
    }

    @Test
    public void atLimitOnAdd() {
        ConfigurableStorage<Dimensionless> storage = new LimitedTestStorage();

        assertTrue("Storage is not at lower limit when below: ", storage.atLowerLimit());
        storage.add(UPPER_LIMIT.times(2));
        assertTrue("Storage is not at upper limit when above: ", storage.atUpperLimit());
    }

    @Test
    public void atLowerLimitOnExactStore() {
        ConfigurableStorage<Dimensionless> storage = new LimitedTestStorage();

        storage.store(LOWER_LIMIT);
        assertTrue("Storage is not at lower limit when exactly at that amount: ", storage.atLowerLimit());
    }

    @Test
    public void atUpperLimitOnExactStore() {
        ConfigurableStorage<Dimensionless> storage = new LimitedTestStorage();

        storage.store(UPPER_LIMIT);
        assertTrue("Storage is not at upper limit after adding that amount: ", storage.atUpperLimit());
    }
}