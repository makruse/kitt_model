package test;

import static org.junit.Assert.*;
import static test.resources.LimitedTestStorage.LOWER_LIMIT;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.junit.*;

import test.resources.LimitedTestStorage;
import de.zmt.storage.*;
import de.zmt.storage.pipeline.*;
import de.zmt.storage.pipeline.StoragePipeline.DelayedStorage;

public class TestStoragePipeline implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(TestStoragePipeline.class.getName());

    private static final long DURATION = 1;

    // TEST STORE AMOUNT
    private static final Amount<Dimensionless> CHANGE = Amount.valueOf(15.4,
	    Unit.ONE);
    private static final Amount<Dimensionless> STORED_IN = CHANGE
	    .times(LimitedTestStorage.FACTOR_IN);
    private static final Amount<Dimensionless> DRAINED = STORED_IN
	    .divide(LimitedTestStorage.FACTOR_OUT);

    private long timePassed;

    @Before
    public void setUp() {
	timePassed = 0;
    }

    @Test
    public void testWithLimits() {
	logger.info("Testing Pipeline with limits.");

	StoragePipeline<Dimensionless> pipeline = new Pipeline(
		new LimitedTestStorage());

	// initialize
	logger.info("Initial state of pipeline: " + pipeline);
	Amount<Dimensionless> initialAmount = pipeline.getAmount();
	assertEquals("Pipeline not initialized to lower limit: " + LOWER_LIMIT,
		LOWER_LIMIT, pipeline.getAmount());

	// add amount
	logger.info("adding " + CHANGE);
	Amount<Dimensionless> storedIn = pipeline.add(CHANGE).getStored();
	assertEquals("Storage did not store correct amount: ", STORED_IN,
		storedIn);

	// drain nothing
	assertEquals("Could drain an unexpired amount: ", 0, pipeline
		.drainExpired().getExactValue());

	// time passes
	timePassed++;

	// drain element
	// only approximate due to storage added for lower limit
	Amount<Dimensionless> drainedAmount = pipeline.drainExpired();
	logger.info("Drained " + drainedAmount + " from pipeline.");
	assertTrue(
		"Drained amount does not approximate returned value. expected: <"
			+ DRAINED + "> but was:<" + drainedAmount + ">",
		drainedAmount.approximates(DRAINED));

	logger.info("Final state of pipeline: " + pipeline);
	assertEquals(
		"Final state differs from initial although pipeline should be at lower limit in both.",
		initialAmount, pipeline.getAmount());
	assertFalse("No content although amount up to lower limit is left.",
		pipeline.getContent().isEmpty());
    }

    @Test
    public void testWithoutLimits() {
	logger.info("Testing Pipeline without limits.");

	StoragePipeline<Dimensionless> pipeline = new Pipeline(
		new ConfigurableStorage<Dimensionless>(Unit.ONE));

	// initialize
	logger.info(pipeline.toString());
	assertEquals("Pipeline not initialized to zero.", Amount.ZERO,
		pipeline.getAmount());

	// add amount
	logger.info("adding " + CHANGE);
	Amount<Dimensionless> storedIn = pipeline.add(CHANGE).getStored();
	assertEquals("Storage did not store correct amount: ",
		CHANGE.times(1d), storedIn);

	// drain nothing
	assertEquals("Could drain an unexpired amount: ", 0, pipeline
		.drainExpired().getExactValue());

	// time passes
	timePassed++;

	// drain element
	// only approximate due to storage added for lower limit
	Amount<Dimensionless> drainedAmount = pipeline.drainExpired();
	assertTrue(
		"Drained amount does not approximate returned value. expected: <"
			+ CHANGE + "> but was:<" + drainedAmount + ">",
		drainedAmount.approximates(CHANGE));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
	logger.info("Testing Pipeline serialization.");

	Pipeline pipeline = new Pipeline(
		new ConfigurableStorage<Dimensionless>(Unit.ONE));
	Pipeline restoredPipeline;
	byte[] objData;

	logger.info("serializing / deserializing pipeline with one object");
	pipeline.add(Amount.ONE);
	objData = write(pipeline);
	restoredPipeline = (Pipeline) read(objData);
	assertEquals("restored queue size does not match", 1, restoredPipeline
		.getContent().size());

	logger.info("serializing / deserializing pipeline two objects");
	pipeline.add(Amount.ONE);
	objData = write(pipeline);
	restoredPipeline = (Pipeline) read(objData);
	assertEquals("restored queue size does not match", 2, restoredPipeline
		.getContent().size());
    }

    private byte[] write(Object obj) throws IOException {
	ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
	ObjectOutputStream output = new ObjectOutputStream(byteOutputStream);
	output.writeObject(obj);
	output.close();

	return byteOutputStream.toByteArray();
    }

    private Object read(byte[] objData) throws IOException,
	    ClassNotFoundException {
	ByteArrayInputStream byteInputStream = new ByteArrayInputStream(objData);
	ObjectInputStream input = new ObjectInputStream(byteInputStream);
	Object object = input.readObject();
	input.close();

	return object;
    }

    private class Pipeline extends AbstractStoragePipeline<Dimensionless> {
	private static final long serialVersionUID = 1L;

	public Pipeline(MutableStorage<Dimensionless> sum) {
	    super(sum);
	}

	@Override
	protected DelayedStorage<Dimensionless> createDelayedStorage(
		Amount<Dimensionless> storedAmount) {
	    return new FixedDelayStorage(storedAmount);
	}
    }

    private class FixedDelayStorage extends DelayedStorage<Dimensionless> {
	private static final long serialVersionUID = 1L;

	private final long timeFinished;

	public FixedDelayStorage(Amount<Dimensionless> amount) {
	    super(amount);
	    timeFinished = timePassed + DURATION;
	}

	@Override
	public long getDelay(TimeUnit unit) {
	    return unit.convert(timeFinished - timePassed,
		    TimeUnit.MILLISECONDS);
	}

    }

}
