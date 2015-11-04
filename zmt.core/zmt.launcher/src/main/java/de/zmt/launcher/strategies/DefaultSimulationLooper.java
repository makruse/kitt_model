package de.zmt.launcher.strategies;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;
import java.util.logging.*;

import javax.xml.bind.JAXBException;

import de.zmt.util.ParamsUtil;
import sim.engine.*;
import sim.engine.params.SimParams;

class DefaultSimulationLooper implements SimulationLooper {
    @SuppressWarnings("unused")
    static final Logger logger = Logger.getLogger(DefaultSimulationLooper.class.getName());

    private static final String ZERO_PADDED_FORMAT_STRING = "%04d";
    private static final String RESULTS_DIR_PREFIX = "results_";
    private static final String PARAMS_FILENAME_SUFFIX = "_" + SimParams.DEFAULT_FILENAME;

    /**
     * No writing to disk. Should be set to false for tests.
     * 
     * @see SimRun#writeParams(SimParams)
     */
    private final boolean writeEnabled;

    public DefaultSimulationLooper() {
	this(true);
    }

    public DefaultSimulationLooper(boolean writeEnabled) {
	super();
	this.writeEnabled = writeEnabled;
    }

    @Override
    public void loop(ZmtSimState simState, double simTime) {
	runSimulation(simState, simTime);
    }

    /**
     * Iterate combinations apply them to parameters and run a simulation with
     * each of them.
     */
    // TODO report simulation exceptions
    @Override
    public void loop(Class<? extends ZmtSimState> simClass, Iterable<? extends SimParams> simParamsObjects,
	    int maxThreads, double simTime) {
	SimRunContext context = new SimRunContext(simClass, simTime, findResultsPath(), writeEnabled);
	int jobNum = 0;

	ExecutorService executor = new BlockingExecutor(
		maxThreads > 0 ? maxThreads : Runtime.getRuntime().availableProcessors());
	for (SimParams simParams : simParamsObjects) {
	    executor.execute(new SimRun(context, simParams, jobNum));
	    jobNum++;
	}
	executor.shutdown();
	try {
	    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
	} catch (InterruptedException e) {
	    throw new RuntimeException("Interrupt while waiting for simulations to complete.", e);
	}

	if (jobNum == 0) {
	    logger.warning("No combinations given: Could not start any simulation runs.");
	}
    }

    /** @return empty directory to write results to, e.g. "results_0023/" */
    private String findResultsPath() {
	if (!writeEnabled) {
	    return null;
	}

	int resultsDirCount = 0;
	File resultsFile;
	do {
	    resultsFile = new File(RESULTS_DIR_PREFIX + String.format(ZERO_PADDED_FORMAT_STRING, resultsDirCount));
	    resultsDirCount++;
	} while (resultsFile.exists());

	// ... and create it
	resultsFile.mkdir();
	return resultsFile + File.separator;
    }

    /**
     * Run a simulation for a given time.
     * 
     * @param simState
     * @param simTime
     */
    private static void runSimulation(SimState simState, double simTime) {
	long startTime = System.currentTimeMillis();

	// run the simulation
	simState.start();

	while (simState.schedule.step(simState) && simState.schedule.getTime() < simTime) {
	}

	simState.finish();
	long runTime = System.currentTimeMillis() - startTime;

	logger.info("Simulation " + simState.job() + " finished with " + simState.schedule.getSteps() + " steps in "
		+ millisToShortHMS(runTime));
    }

    /**
     * @see <a href="http://www.rgagnon.com/javadetails/java-0585.html">Format a
     *      duration in milliseconds into a human-readable format</a>
     * @param duration
     * @return {@code duration} in human-readable format ("hh:mm:ss.SSS")
     */
    private static String millisToShortHMS(long duration) {
	// use time API from Java 8 when possible
	long hours = TimeUnit.MILLISECONDS.toHours(duration);
	long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hours);
	long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes);
	long millis = duration - TimeUnit.SECONDS.toMillis(seconds);

	return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
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
	public final String resultsPath;
	public final boolean writeEnabled;

	public SimRunContext(Class<? extends ZmtSimState> simClass, double simTime, String resultsPath,
		boolean writeEnabled) {
	    super();
	    this.simClass = simClass;
	    this.simTime = simTime;
	    this.resultsPath = resultsPath;
	    this.writeEnabled = writeEnabled;
	}
    }

    /**
     * Runnable wrapper for running a simulation in its own thread.
     * 
     * @author mey
     *
     */
    private static final class SimRun implements Runnable {
	/**
	 * Thread-local sim state instance. Gets reused after simulation
	 * finished within this thread.
	 */
	private static final ThreadLocal<ZmtSimState> SIM_STATE = new ThreadLocal<ZmtSimState>();

	private final SimRunContext context;
	private final SimParams simParams;
	private final long jobNum;

	public SimRun(SimRunContext context, SimParams simParams, long jobNum) {
	    super();
	    this.context = context;
	    this.simParams = simParams;
	    this.jobNum = jobNum;
	}

	@Override
	public void run() {
	    logger.info("Running simulation " + jobNum);
	    ZmtSimState simState = obtainSimState();
	    simState.setParams(simParams);
	    simState.setOutputPath(context.resultsPath);
	    simState.setJob(jobNum);

	    writeParams(simParams);
	    runSimulation(simState, context.simTime);
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

	/**
	 * Parameters are written into results directory with prefixed number.
	 * 
	 * @param simParams
	 */
	private void writeParams(SimParams simParams) {
	    if (!context.writeEnabled) {
		return;
	    }

	    String numberedResultsPath = context.resultsPath + String.format(ZERO_PADDED_FORMAT_STRING, jobNum)
		    + PARAMS_FILENAME_SUFFIX;
	    try {
		ParamsUtil.writeToXml(simParams, numberedResultsPath);
	    } catch (JAXBException | IOException e) {
		logger.log(Level.WARNING, "Could not write current parameters to " + numberedResultsPath, e);
	    }
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
