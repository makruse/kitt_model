package de.zmt.launcher.strategies;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStreamException;

import de.zmt.launcher.strategies.CombinationApplier.AppliedCombination;
import de.zmt.params.SimParams;
import de.zmt.util.ParamsUtil;
import sim.engine.Schedule;
import sim.engine.ZmtSimState;

class DefaultSimulationLooper implements SimulationLooper {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefaultSimulationLooper.class.getName());

    private static final double NANO_FACTOR = 1e-09;
    /** {@link NumberFormat} for simulation step rates. */
    private static final NumberFormat RATE_FORMAT;

    static {
	RATE_FORMAT = NumberFormat.getInstance();
	RATE_FORMAT.setMaximumFractionDigits(5);
	RATE_FORMAT.setMinimumIntegerDigits(1);
    }

    @Override
    public void loop(ZmtSimState simState, double simTime, int printStatusInterval) {
	runSimulation(simState, simTime, 1, printStatusInterval);
    }

    /**
     * Iterate combinations apply them to parameters and run a simulation with
     * each of them.
     */
    // TODO report simulation exceptions
    @Override
    public void loop(Class<? extends ZmtSimState> simClass, Iterable<AppliedCombination> appliedCombinations,
	    int combinationsCount, int maxThreads, double simTime, int printStatusInterval,
	    boolean combinationInFolderNames, Iterable<Path> outputPaths) {
	SimRunContext context = new SimRunContext(simClass, simTime, combinationsCount, printStatusInterval);
	Iterator<Path> outputPathsIterator = outputPaths.iterator();
	int jobNum = 0;

	int availableProcessors = Runtime.getRuntime().availableProcessors();
	int nThreads = maxThreads > 0 ? maxThreads : availableProcessors;
	logger.info("Starting batch run\n" + "total number of jobs: " + combinationsCount + "\n"
		+ "maximum number of jobs running in parallel: " + nThreads + "\n" + "available processor cores: "
		+ availableProcessors);
	ExecutorService executor = new BlockingExecutor(nThreads);
	long startTime = System.currentTimeMillis();
	for (AppliedCombination appliedCombination : appliedCombinations) {
	    Path outputPath = outputPathsIterator.next();
	    if (combinationInFolderNames) {
		// replace run_XXXXX with combination's string representation
		outputPath = outputPath.resolveSibling(appliedCombination.combination.toString());
	    }
	    executor.execute(new SimRun(context, appliedCombination, ++jobNum, outputPath));
	}
	executor.shutdown();
	try {
	    // wait endlessly until all simulation runs are done
	    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
	} catch (InterruptedException e) {
	    throw new RuntimeException("Interrupt while waiting for simulations to complete.", e);
	}

	if (jobNum > 0) {
	    logger.info("Finished " + jobNum + " simulation runs in "
		    + Duration.ofMillis(System.currentTimeMillis() - startTime));
	} else {
	    logger.warning("No combinations given: Could not start any simulation runs.");
	}
    }

    /**
     * Run a simulation for a given time.
     * 
     * @param simState
     *            the simulation to run
     * @param simTime
     *            the time when the simulation stops
     * @param jobCount
     *            the total number of jobs in this simulation run
     * @param printStatusInterval
     *            the step interval in which status messages are printed (<1 to
     *            disable)
     */
    private static void runSimulation(ZmtSimState simState, double simTime, int jobCount, double printStatusInterval) {
	long job = simState.job();
	long startTime = System.currentTimeMillis();

	// run the simulation
	simState.start();
	logger.fine("Simulation " + job + " started.");

	Schedule schedule = simState.schedule;
	long systemTimeLastInterval = System.nanoTime();
	while (schedule.step(simState) && schedule.getTime() < simTime) {
	    long steps = schedule.getSteps();
	    if (printStatusInterval > 0 && steps > 0 && steps % printStatusInterval == 0) {
		double rate = printStatusInterval / (NANO_FACTOR * (System.nanoTime() - systemTimeLastInterval));
		logger.info("Job " + job + "/" + jobCount + ": " + "Steps: " + steps + " Time: "
			+ schedule.getTimestamp("At Start", "Done") + " Rate: " + RATE_FORMAT.format(rate)
			+ " steps/s");
		systemTimeLastInterval = System.nanoTime();
	    }
	}

	simState.finish();
	long runTime = System.currentTimeMillis() - startTime;

	logger.info("Simulation " + job + " finished with " + schedule.getSteps() + " steps in "
		+ Duration.ofMillis(runTime));

	// set params to null to prevent further access
	simState.setParams(null);
    }

    /**
     * Immutable context object shared by all simulation runs managed by a
     * single looper.
     * 
     * @author mey
     *
     */
    private static final class SimRunContext {
	public final Class<? extends ZmtSimState> simClass;
	public final double simTime;
	public final int jobCount;
	public final double printStatusInterval;

	public SimRunContext(Class<? extends ZmtSimState> simClass, double simTime, int jobCount,
		double printStatusInterval) {
	    super();
	    this.simClass = simClass;
	    this.simTime = simTime;
	    this.jobCount = jobCount;
	    this.printStatusInterval = printStatusInterval;
	}

    }

    /**
     * Runnable wrapper for running a simulation in its own thread.
     * 
     * @author mey
     *
     */
    private static final class SimRun implements Runnable {
	private static final String COMBINATION_FILENAME_AFTER_INDEX = "combination.xml";
	private static final String PARAMS_FILENAME_AFTER_INDEX = SimParams.DEFAULT_FILENAME;
	/**
	 * Thread-local sim state instance. Gets reused after simulation
	 * finished within this thread.
	 */
	private static final ThreadLocal<ZmtSimState> SIM_STATE = new ThreadLocal<>();

	private final SimRunContext context;
	private final AppliedCombination appliedCombination;
	private final long jobNum;
	private final Path outputPath;

	public SimRun(SimRunContext context, AppliedCombination appliedCombination, long jobNum, Path outputPath) {
	    super();
	    this.context = context;
	    this.appliedCombination = appliedCombination;
	    this.jobNum = jobNum;
	    this.outputPath = outputPath;
	}

	@Override
	public void run() {
	    logger.info("Running simulation " + jobNum);

	    try {
		Files.createDirectories(outputPath);
	    } catch (IOException e) {
		logger.log(Level.WARNING, "Could not create directory at output path " + outputPath, e);
	    }

	    // write combination and parameters to files
	    try {
		ParamsUtil.writeToXml(appliedCombination.combination,
			outputPath.resolve(COMBINATION_FILENAME_AFTER_INDEX));
		ParamsUtil.writeToXml(appliedCombination.result, outputPath.resolve(PARAMS_FILENAME_AFTER_INDEX));
	    } catch (IOException | XStreamException e) {
		logger.log(Level.WARNING, "Could not save object to XML at " + outputPath, e);
	    }

	    ZmtSimState simState = obtainSimState();
	    simState.setParams(appliedCombination.result);
	    simState.setOutputPath(outputPath);
	    simState.setJob(jobNum);

	    runSimulation(simState, context.simTime, context.jobCount, context.printStatusInterval);
	}

	/**
	 * Either reuse a previously created sim state after the simulation has
	 * finished or create it anew if this is the first run within this
	 * thread.
	 * 
	 * @return thread-local sim state
	 */
	private ZmtSimState obtainSimState() {
	    ZmtSimState simState = SIM_STATE.get();

	    // thread has no sim state associated: instantiate new one
	    if (simState == null) {
		try {
		    simState = context.simClass.getConstructor().newInstance();
		    SIM_STATE.set(simState);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
			| InvocationTargetException | SecurityException e) {
		    throw new RuntimeException(
			    "Failed to instantiate simulation object: " + context.simClass.getSimpleName(), e);
		} catch (NoSuchMethodException e) {
		    throw new RuntimeException(context.simClass.getSimpleName()
			    + " needs a public no-argument constructor for instantiation.", e);
		}
	    }
	    return simState;
	}
    }

    /**
     * An executor which blocks and prevents further tasks from being submitted
     * to the pool when the queue is full. In this way a huge memory consuming
     * queue is prevented from building up.
     * <p>
     * Based on the BoundedExecutor example in: Brian Goetz, 2006. Java
     * Concurrency in Practice. (Listing 8.4)
     * 
     * @see <a href=
     *      "http://www.javacodegeeks.com/2013/11/throttling-task-submission-with-a-blockingexecutor-2.html">
     *      Java Code Geeks: Throttling Task Submission with a
     *      BlockingExecutor</a>
     */
    private static class BlockingExecutor extends ThreadPoolExecutor {

	private final Semaphore semaphore;

	/**
	 * Creates a BlockingExecutor which will block and prevent further
	 * submission to the pool when the specified queue size has been
	 * reached.
	 *
	 * @param nThreads
	 *            the number of the threads in the pool
	 */
	public BlockingExecutor(final int nThreads) {
	    super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
		    /*
		     * Unfortunately SynchronousQueue cannot be used here. Tasks
		     * need to be buffered because afterExecute is called before
		     * the executing thread is marked to be inactive.
		     * 
		     * This leads to RejectedExecutionExceptions sometimes when
		     * the next command is executed and all threads are still
		     * marked active.
		     */
		    new LinkedBlockingQueue<Runnable>());

	    semaphore = new Semaphore(nThreads);
	}

	/**
	 * Executes the given command. This method will block when the semaphore
	 * has no permits i.e. when the queue has reached its capacity.
	 */
	@Override
	public void execute(final Runnable command) {
	    boolean acquired = false;
	    do {
		try {
		    semaphore.acquire();
		    acquired = true;
		} catch (final InterruptedException e) {
		    logger.log(Level.WARNING, InterruptedException.class.getSimpleName() + "while aquiring semaphore.",
			    e);
		}
	    } while (!acquired);

	    try {
		super.execute(command);
	    } catch (final RejectedExecutionException e) {
		semaphore.release();
		throw e;
	    }
	}

	/**
	 * Method invoked upon completion of execution of the given Runnable, by
	 * the thread that executed the task. Releases a semaphore permit.
	 */
	@Override
	protected void afterExecute(final Runnable r, final Throwable t) {
	    if (t != null) {
		logger.log(Level.WARNING, r + " was terminated by exception.", t);
	    }
	    super.afterExecute(r, t);
	    semaphore.release();
	}
    }
}
