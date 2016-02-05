package de.zmt.launcher.strategies;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.zmt.launcher.strategies.CombinationApplier.AppliedCombination;
import sim.engine.BaseZmtSimState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.params.TestParams;

public class DefaultSimulationLooperTest {
    private static final SimulationLooper SIMULATION_LOOPER = new DefaultSimulationLooper();

    // LOOP ON PARALLEL RUNS
    private static final int TIMEOUT_SECONDS = 2;
    private static final int RUN_COUNT = 4;
    private static final int MAX_THREADS = 2;
    private static final double SIM_TIME = 10;
    private static final TestParams SIM_PARAMS = new TestParams();
    private static final Combination COMBINATION = new Combination();
    private static final Collection<AppliedCombination> APPLIED_COMBINATIONS = Collections.nCopies(RUN_COUNT,
	    new AppliedCombination(COMBINATION, SIM_PARAMS));

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Iterable<Path> outputPaths;

    @Before
    public void setUp() throws IOException {
	outputPaths = createOutputPaths(folder.newFolder().toPath());
    }

    @Test
    public void loopOnSingle() {
	TestSimState simState = new TestSimState();
	simState.setParams(SIM_PARAMS);
	SIMULATION_LOOPER.loop(simState, SIM_TIME);
    }

    @Test
    public void loopOnParallelRuns() throws InterruptedException {
	loopOnParallelRuns(false);
    }

    @Test
    public void loopOnParallelRunsWithCombinationInFolderNames() throws InterruptedException {
	loopOnParallelRuns(true);
    }

    private void loopOnParallelRuns(boolean combinationInFolderNames) throws InterruptedException {
	// if this test is already running, we will wait until it is done
	CountDownTestSimState.doneSignal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
	CountDownTestSimState.doneSignal = new CountDownLatch(RUN_COUNT);
	SIMULATION_LOOPER.loop(CountDownTestSimState.class, APPLIED_COMBINATIONS, MAX_THREADS, SIM_TIME,
		combinationInFolderNames, outputPaths);
	waitUntilSimsFinished();

	Iterator<AppliedCombination> iterator = APPLIED_COMBINATIONS.iterator();
	for (Path outputPath : outputPaths) {
	    // 2 files need to be written: params and combination
	    if (combinationInFolderNames) {
		assertThat(outputPath.resolveSibling(iterator.next().combination.toString()).toFile().list(),
			arrayWithSize(2));
	    } else {
		assertThat(outputPath.toFile().list(), arrayWithSize(2));
	    }
	}
    }

    @Test
    public void loopOnThreadLocalSingleton() throws InterruptedException {
	// if this test is already running, we will wait until it is done
	CountDownTestSimState.doneSignal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
	CountDownTestSimState.doneSignal = new CountDownLatch(RUN_COUNT);
	SIMULATION_LOOPER.loop(ThreadLocalSingletonTestSimState.class, APPLIED_COMBINATIONS, MAX_THREADS, SIM_TIME,
		false, outputPaths);
	waitUntilSimsFinished();
    }

    private static Iterable<Path> createOutputPaths(Path directory) {
	List<Path> paths = new ArrayList<>(RUN_COUNT);
	for (int i = 0; i < RUN_COUNT; i++) {
	    paths.add(directory.resolve(Integer.toString(i)));
	}
	return paths;
    }

    /**
     * Let this thread wait until simulations finish, and fail after a timeout.
     */
    private static void waitUntilSimsFinished() {
	// wait until simulations finish
	try {
	    if (!CountDownTestSimState.doneSignal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
		// ... fail on timeout
		fail("Simulations did not finish within " + TIMEOUT_SECONDS + " seconds");
	    }
	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * A test simulation that contains one {@link Steppable} that does nothing
     * but sleep for {@value #STEP_DURATION_MILLIS}ms.
     * 
     * @author mey
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
		fail("Insufficient simulation time passed: " + schedule.getTime());
	    }

	    super.finish();
	}
    }

    /**
     * Counts down the latch when finishing to report back to the test method.
     * 
     * @author mey
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

    /**
     * A simulation class being globally accessible within its thread. Can be
     * used to make simulations automatable that have a SimState with singleton
     * access.
     * 
     * @author mey
     */
    public static class ThreadLocalSingletonTestSimState extends CountDownTestSimState {
	private static final long serialVersionUID = 1L;

	/** Thread local simulation instances. */
	private static final ThreadLocal<ThreadLocalSingletonTestSimState> INSTANCES = new ThreadLocal<>();

	public ThreadLocalSingletonTestSimState() {
	    super();
	    if (INSTANCES.get() != null) {
		throw new IllegalThreadStateException(
			"Can only create ONE " + getClass().getSimpleName() + " object per thread.");
	    }
	    INSTANCES.set(this);
	}

	public static ThreadLocalSingletonTestSimState getInstance() {
	    return INSTANCES.get();
	}

	@Override
	public void start() {
	    super.start();

	    // check if the right instance is accessible
	    schedule.scheduleRepeating(new Steppable() {
		private static final long serialVersionUID = 1L;

		@Override
		public void step(SimState state) {
		    assertThat(getInstance(), is(ThreadLocalSingletonTestSimState.this));
		}
	    });
	}
    }
}
