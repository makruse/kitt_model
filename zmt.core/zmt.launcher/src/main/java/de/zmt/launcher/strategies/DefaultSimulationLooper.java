package de.zmt.launcher.strategies;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.zmt.launcher.strategies.CombinationApplier.AppliedCombination;
import de.zmt.util.ParamsUtil;
import sim.engine.ZmtSimState;
import sim.engine.params.SimParams;

class DefaultSimulationLooper implements SimulationLooper {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefaultSimulationLooper.class.getName());

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
    public void loop(Class<? extends ZmtSimState> simClass, Iterable<AppliedCombination> appliedCombinations,
	    int maxThreads, double simTime, Iterable<Path> outputPaths) {
	SimRunContext context = new SimRunContext(simClass, simTime);
	Iterator<Path> outputPathsIterator = outputPaths.iterator();
	int jobNum = 0;

	ExecutorService executor = new BlockingExecutor(
		maxThreads > 0 ? maxThreads : Runtime.getRuntime().availableProcessors());
	for (AppliedCombination appliedCombination : appliedCombinations) {
	    // replace run_XXXXX with combination string representation
	    Path outputPath = outputPathsIterator.next().resolveSibling(appliedCombination.combination.toString());
	    executor.execute(new SimRun(context, appliedCombination, jobNum, outputPath));
	    jobNum++;
	}
	executor.shutdown();
	try {
	    // wait endlessly until all simulation runs are done
	    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
	} catch (InterruptedException e) {
	    throw new RuntimeException("Interrupt while waiting for simulations to complete.", e);
	}

	if (jobNum == 0) {
	    logger.warning("No combinations given: Could not start any simulation runs.");
	}
    }

    /**
     * Run a simulation for a given time.
     * 
     * @param simState
     * @param simTime
     */
    private static void runSimulation(ZmtSimState simState, double simTime) {
	long startTime = System.currentTimeMillis();

	// run the simulation
	simState.start();

	while (simState.schedule.step(simState) && simState.schedule.getTime() < simTime) {
	}

	simState.finish();
	long runTime = System.currentTimeMillis() - startTime;

	logger.info("Simulation " + simState.job() + " finished with " + simState.schedule.getSteps() + " steps in "
		+ millisToShortHMS(runTime));

	// set params to null to prevent further access
	simState.setParams(null);
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

	public SimRunContext(Class<? extends ZmtSimState> simClass, double simTime) {
	    super();
	    this.simClass = simClass;
	    this.simTime = simTime;
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

	public SimRun(SimRunContext context, AppliedCombination appliedCombination, long jobNum,
		Path outputPath) {
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
	    } catch (JAXBException | IOException e) {
		logger.log(Level.WARNING, "Could not save object to XML at " + outputPath, e);
	    }

	    ZmtSimState simState = obtainSimState();
	    simState.setParams(appliedCombination.result);
	    simState.setOutputPath(outputPath);
	    simState.setJob(jobNum);

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
