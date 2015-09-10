package de.zmt.launcher;

import java.io.FileNotFoundException;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import de.zmt.launcher.LauncherArgs.Mode;
import de.zmt.launcher.strategies.CombinationCompiler.Combination;
import de.zmt.launcher.strategies.LauncherStrategyContext;
import de.zmt.launcher.strategies.ParamsLoader.ParamsLoadFailedException;
import de.zmt.sim.engine.ZmtSimState;
import de.zmt.sim.engine.params.*;
import de.zmt.util.ParamsUtil;
import sim.display.GUIState;

/**
 * Launches a simulation run with processing logic provided by context object.
 * 
 * @author cmeyer
 *
 */
public class Launcher {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Launcher.class
	    .getName());

    private final Map<LauncherArgs.Mode, ModeProcessor> processors = new HashMap<>();

    public Launcher(LauncherStrategyContext context) {
	processors.put(Mode.SINGLE, new SingleProcessor(context));
	processors.put(Mode.GUI, new GuiProcessor(context));
	processors.put(Mode.BATCH, new BatchProcessor(context));
    }

    /**
     * Start a simulation run with the provided {@code args}.
     * 
     * @param args
     */
    public void run(LauncherArgs args) {
	// process according to selected mode
	processors.get(args.getMode()).process(args);
    }

    /**
     * Interface to abstract mode processing. Launcher will process according to
     * selected {@link Mode}.
     * 
     * @author cmeyer
     *
     */
    private static interface ModeProcessor {
	void process(LauncherArgs args);
    }

    /**
     * Class providing basic functionality for other ModeProcessors. The
     * {@link ZmtSimState} is created and parameters are loaded and applied.
     * 
     * @author cmeyer
     *
     */
    private abstract static class BaseProcessor implements ModeProcessor {
	protected final LauncherStrategyContext context;
	protected ZmtSimState simState;

	public BaseProcessor(LauncherStrategyContext context) {
	    super();
	    this.context = context;
	}

	@Override
	public final void process(LauncherArgs args) {
	    simState = createSimState(args);
	    SimParams simParams = obtainSimParams(args, simState.getClass());
	    simState.setParams(simParams);
	    childProcess(args);
	}

	/**
	 * Locates sim state class from package path and instantiates it.
	 * 
	 * @param args
	 * @return sim state
	 */
	private final ZmtSimState createSimState(LauncherArgs args) {
	    Class<? extends ZmtSimState> simClass;
	    ZmtSimState simState;

	    try {
		simClass = context.classLocator.findSimStateClass(args
			.getSimName());
	    } catch (ClassNotFoundException e) {
		throw new ProcessFailedException(e);
	    }

	    try {
		simState = simClass.getConstructor().newInstance();
	    } catch (InstantiationException | IllegalAccessException
		    | IllegalArgumentException | InvocationTargetException
		    | SecurityException e) {
		throw new ProcessFailedException(
			"Failed to instantiate simulation object: "
				+ simClass.getSimpleName(), e);
	    } catch (NoSuchMethodException e) {
		throw new ProcessFailedException(
			simClass.getSimpleName()
				+ " needs a non-argument constructor for instantiation.",
			e);
	    }

	    return simState;
	}

	/**
	 * Obtains parameter class for {@code simClass} and loads it from path
	 * given by {@code args}.
	 * 
	 * @param args
	 * @param simClass
	 * @return matching {@link SimParams} object
	 */
	private SimParams obtainSimParams(LauncherArgs args,
		Class<? extends ZmtSimState> simClass) {
	    Class<? extends SimParams> paramsClass = ParamsUtil
		    .obtainParamsClass(simClass);
	    try {
		return context.paramsLoader.loadSimParams(
			args.getSimParamsPath(), paramsClass);
	    } catch (ParamsLoadFailedException loadFailed) {
		// default parameters not found: instantiate new
		if (loadFailed.getCause() instanceof FileNotFoundException && !args.isSimParamsPathSet()) {
		    logger.log(
			    Level.WARNING,
			    "Loading simulation parameters from default path failed. Instantiating new object.",
			    loadFailed);
		    try {
			return paramsClass.newInstance();
		    } catch (InstantiationException | IllegalAccessException
			    | IllegalArgumentException | SecurityException instantiationFailed) {
			throw new ProcessFailedException(instantiationFailed);
		    }
		} else {
		    throw new ProcessFailedException(loadFailed);
		}
	    }
	}

	protected abstract void childProcess(LauncherArgs args);
    }

    /**
     * Runs simulation for a single time.
     * 
     * @author cmeyer
     *
     */
    private static class SingleProcessor extends BaseProcessor {

	public SingleProcessor(LauncherStrategyContext context) {
	    super(context);
	}

	@Override
	public void childProcess(LauncherArgs args) {
	    context.simulationLooper.loop(simState, args.getSimTime());
	}
    }

    /**
     * Opens the gui for the argument-provided ZmtSimState and parameters.
     * 
     * @author cmeyer
     *
     */
    private static class GuiProcessor extends BaseProcessor {

	public GuiProcessor(LauncherStrategyContext context) {
	    super(context);
	}

	@Override
	public void childProcess(LauncherArgs args) {
	    Class<? extends GUIState> guiStateClass;
	    try {
		guiStateClass = context.classLocator.findGuiStateClass(args
			.getSimName());
	    } catch (ClassNotFoundException e) {
		throw new ProcessFailedException(e);
	    }

	    // make the gui visible
	    createGuiState(guiStateClass).createController();
	}

	private GUIState createGuiState(Class<? extends GUIState> guiStateClass) {
	    try {
		// find matching constructor
		for (Constructor<?> constructor : guiStateClass
			.getConstructors()) {
		    if (constructor.getParameterCount() == 1
			    && constructor.getParameterTypes()[0]
				    .isAssignableFrom(simState.getClass())) {
			return (GUIState) constructor.newInstance(simState);
		    }
		}
	    } catch (InstantiationException | IllegalAccessException
		    | IllegalArgumentException | InvocationTargetException
		    | SecurityException e) {
		throw new ProcessFailedException(
			"Failed to instantiate GUI object: "
				+ guiStateClass.getSimpleName(), e);
	    }

	    throw new ProcessFailedException(guiStateClass
		    + " needs a constructor matching parameter type: "
		    + simState.getClass());
	}
    }

    /**
     * Runs an automated batch of simulations with varying parameters from
     * {@link AutoParams}.
     * 
     * @author cmeyer
     *
     */
    private static class BatchProcessor extends BaseProcessor {

	public BatchProcessor(LauncherStrategyContext context) {
	    super(context);
	}

	@Override
	public void childProcess(LauncherArgs args) {
	    AutoParams autoParams;
	    try {
		autoParams = context.paramsLoader.loadAutoParams(args
			.getAutoParamsPath());
	    } catch (ParamsLoadFailedException e) {
		throw new ProcessFailedException(e);
	    }
	    // compile combinations
	    Iterable<Combination> combinations = context.combinationCompiler
		    .compileCombinations(autoParams.getDefinitions());
	    // apply combinations: use params loaded in base as default
	    Iterable<SimParams> simParamsObjects = context.combinationApplier
		    .applyCombinations(combinations, simState.getParams());
	    // run a simulation for every parameter object
	    context.simulationLooper.loop(simState.getClass(),
		    simParamsObjects, autoParams.getMaxThreads(),
		    args.getSimTime());
	}
    }

    static class ProcessFailedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ProcessFailedException(String message) {
	    super(message);
	}

	public ProcessFailedException(String message, Throwable cause) {
	    super(message, cause);
	}

	public ProcessFailedException(Throwable cause) {
	    super(cause);
	}
    }
}
