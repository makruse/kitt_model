package de.zmt.storage;

import static de.zmt.storage.LimitedTestStorage.*;
import static org.hamcrest.AmountCloseTo.amountCloseTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.Test;

import test.util.SerializationUtil;

public class AbstractLimitedStoragePipelineTest implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AbstractLimitedStoragePipelineTest.class.getName());

    private static final long DURATION = 1;

    // TEST STORE AMOUNT
    private static final Amount<Dimensionless> CHANGE = Amount.valueOf(15.4, Unit.ONE);
    private static final Amount<Dimensionless> STORED_IN = CHANGE.times(FACTOR_IN);
    // amount will exceed lower limit, so the maximum will be drained
    private static final Amount<Dimensionless> DRAINED = STORED_IN.divide(FACTOR_OUT);


    private long timePassed;

    @Before
    public void setUp() {
	timePassed = 0;
    }

    @Test
    public void addWithLimits() {
	logger.info("Testing Pipeline with limits.");

	StoragePipeline<Dimensionless> pipeline = new Pipeline(new LimitedTestStorage());

	// add amount
	logger.info("adding " + CHANGE);
	Amount<Dimensionless> storedIn = pipeline.add(CHANGE).getStored();
	assertThat("Storage did not store correct amount: ", storedIn, is(amountCloseTo(STORED_IN)));

	// drain nothing
	assertThat("Could drain an unexpired amount: ", pipeline.drainExpired(), is(Amount.ZERO));

	// time passes
	timePassed++;

	// drain nothing again because of lower limit
	assertThat("Could drain more than lower limit: ", pipeline.drainExpired(), is(Amount.ZERO));

	pipeline.store(LOWER_LIMIT);
	timePassed++;
	
	assertThat("Could not drain the expected amount: ", pipeline.drainExpired(), is(amountCloseTo(DRAINED)));

	logger.info("Final state of pipeline: " + pipeline);
	assertThat("Pipeline is not at lower limit: ", pipeline.getAmount(), is(amountCloseTo(LOWER_LIMIT)));
	assertFalse("No content although amount up to lower limit is left.", pipeline.getContent().isEmpty());
    }

    @Test
    public void addWithoutLimits() {
	logger.info("Testing Pipeline without limits.");

	StoragePipeline<Dimensionless> pipeline = new Pipeline(new ConfigurableStorage<>(Unit.ONE));

	// initialize
	logger.info(pipeline.toString());
	assertThat("Pipeline not initialized to zero.", pipeline.getAmount(), is(amountCloseTo(Amount.ZERO)));

	// add amount
	logger.info("adding " + CHANGE);
	Amount<Dimensionless> storedIn = pipeline.add(CHANGE).getStored();
	assertThat("Storage did not store correct amount: ", storedIn, is(amountCloseTo(CHANGE)));

	// drain nothing
	assertThat("Could drain an unexpired amount: ", pipeline.drainExpired(), is(Amount.ZERO));

	// time passes
	timePassed++;

	// drain element
	// only approximate due to storage added for lower limit
	Amount<Dimensionless> drainedAmount = pipeline.drainExpired();
	assertThat("Drained amount does not approximate returned value: ", drainedAmount, is(amountCloseTo(CHANGE)));
    }

    @Test
    public void serialization() throws IOException, ClassNotFoundException {
	logger.info("Testing Pipeline serialization.");

	Pipeline pipeline = new Pipeline(new ConfigurableStorage<>(Unit.ONE));
	Pipeline restoredPipeline;
	byte[] objData;

	logger.info("serializing / deserializing pipeline with one object");
	pipeline.add(Amount.ONE);
	objData = SerializationUtil.write(pipeline);
	restoredPipeline = (Pipeline) SerializationUtil.read(objData);
	assertEquals("restored queue size does not match", 1, restoredPipeline.getContent().size());

	logger.info("serializing / deserializing pipeline two objects");
	pipeline.add(Amount.ONE);
	objData = SerializationUtil.write(pipeline);
	restoredPipeline = (Pipeline) SerializationUtil.read(objData);
	assertEquals("restored queue size does not match", 2, restoredPipeline.getContent().size());
    }

    private class Pipeline extends AbstractLimitedStoragePipeline<Dimensionless> {
	private static final long serialVersionUID = 1L;

	public Pipeline(LimitedStorage<Dimensionless> sum) {
	    super(sum);
	}

	@Override
	protected AbstractLimitedStoragePipeline.DelayedStorage<Dimensionless> createDelayedStorage(
		Amount<Dimensionless> storedAmount) {
	    return new FixedDelayStorage(storedAmount);
	}
    }

    private class FixedDelayStorage extends AbstractLimitedStoragePipeline.DelayedStorage<Dimensionless> {
	private static final long serialVersionUID = 1L;

	private final long timeFinished;

	public FixedDelayStorage(Amount<Dimensionless> amount) {
	    super(amount);
	    timeFinished = timePassed + DURATION;
	}

	@Override
	public long getDelay(TimeUnit unit) {
	    return unit.convert(timeFinished - timePassed, TimeUnit.MILLISECONDS);
	}

    }

}
