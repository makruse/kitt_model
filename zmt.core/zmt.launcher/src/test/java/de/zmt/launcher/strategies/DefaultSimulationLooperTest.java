package de.zmt.launcher.strategies;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;

import org.junit.Test;

import sim.engine.*;
import sim.engine.params.TestParams;

public class DefaultSimulationLooperTest {
    private static final SimulationLooper SIMULATION_LOOPER = new DefaultSimulationLooper(
	    false);

    // LOOP ON PARALLEL RUNS
    private static final int TIMEOUT_SECONDS = 2;
    private static final int RUN_COUNT = 4;
    private static final int MAX_THREADS = 2;
    private static final double SIM_TIME = 10;
    private static final TestParams SIM_PARAMS = new TestParams();

    @Test
    public void loopOnSingle() {
	TestSimState simState = new TestSimState();
	simState.setParams(SIM_PARAMS);
	SIMULATION_LOOPER.loop(simState, SIM_TIME);
    }

    @Test
    public void loopOnParallelRuns() throws InterruptedException {
	// if this test is already running, we will wait until it is done
	CountDownTestSimState.doneSignal.await(TIMEOUT_SECONDS,
		TimeUnit.SECONDS);
	CountDownTestSimState.doneSignal = new CountDownLatch(RUN_COUNT);
	Collection<TestParams> simParamsObjects = Collections.nCopies(
		RUN_COUNT, SIM_PARAMS);
	SIMULATION_LOOPER.loop(CountDownTestSimState.class, simParamsObjects,
		MAX_THREADS, SIM_TIME);
	waitUntilSimsFinished();
    }

    /** Let this thread wait until simulations finish, and fail after a timeout. */
    private static void waitUntilSimsFinished() {
	// wait until simulations finish
	try {
	    if (!CountDownTestSimState.doneSignal.await(TIMEOUT_SECONDS,
		    TimeUnit.SECONDS)) {
		// ... fail on timeout
		fail("Simulations did not finish within " + TIMEOUT_SECONDS
			+ " seconds");
	    }
	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * A test simulation that contains one {@link Steppable} that does nothing
     * but sleep for {@value #STEP_DURATION_MILLIS}ms.
     * 
     * @author cmeyer
     *
     */
    public static class TestSimState extends BaseZmtSimState<TestParams> {
	private static final int STEP_DURATION_MILLIS = 10;
	private static final long serialVersionUID = 1L;

	@Override
	public void start() {
	    super.start();

	    // check if params have been set
	    assertEquals(SIM_PARAMS, getParams());

	    schedule.scheduleRepeating(new Steppable() {
		private static final long serialVersionUID = 1L;

		@Override
		public void step(SimState state) {
		    try {
			Thread.sleep(STEP_DURATION_MILLIS);
		    } catch (InterruptedException e) {
			throw new RuntimeException(e);
		    }
		}
	    });
	}

	@Override
	public void finish() {
	    if (schedule.getTime() < SIM_TIME) {
		fail("Insufficient simulation time passed: "
			+ schedule.getTime());
	    }

	    super.finish();
	}
    }

    /**
     * Counts down the latch when finishing to report back to the test method.
     * 
     * @author cmeyer
     *
     */
    public static class CountDownTestSimState extends TestSimState {
	private static final long serialVersionUID = 1L;

	/** Signals if simulations threads are done. */
	private static CountDownLatch doneSignal = new CountDownLatch(0);
	
	@Override
	public void finish() {
	    super.finish();
	    doneSignal.countDown();
	}

    }
}
