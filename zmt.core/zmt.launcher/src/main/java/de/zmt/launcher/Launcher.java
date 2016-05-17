package de.zmt.launcher;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.zmt.launcher.LauncherArgs.Mode;
import de.zmt.launcher.strategies.ClassLocator;
import de.zmt.launcher.strategies.Combination;
import de.zmt.launcher.strategies.CombinationApplier.AppliedCombination;
import de.zmt.launcher.strategies.LauncherStrategyContext;
import de.zmt.launcher.strategies.ParamsLoader;
import de.zmt.launcher.strategies.ParamsLoader.ParamsLoadFailedException;
import de.zmt.util.ParamsUtil;
import sim.display.ZmtGUIState;
import sim.engine.SimState;
import sim.engine.ZmtSimState;
import sim.engine.params.AutoParams;
import sim.engine.params.SimParams;

/**
 * Launches a simulation run with processing logic provided by context object.
 * 
 * @author mey
 *
 */
public class Launcher {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Launcher.class.getName());

    private final Map<LauncherArgs.Mode, ModeProcessor> processors = new HashMap<>();

    private final LauncherStrategyContext context;

    public Launcher(LauncherStrategyContext context) {
	this.context = context;
	processors.put(null, new PreProcessor());
	processors.put(Mode.SINGLE, new SingleProcessor());
	processors.put(Mode.GUI, new GuiProcessor());
	processors.put(Mode.BATCH, new BatchProcessor());
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

    private static Path getWorkingDirectory() {
	return Paths.get("").toAbsolutePath();
    }

    /**
     * Interface to abstract mode processing. Launcher will process according to
     * selected {@link Mode}.
     * 
     * @author mey
     *
     */
    private static interface ModeProcessor {
	/**
	 * Process {@code args} with this {@code ModeProcessor}.
	 * 
	 * @param args
	 */
	void process(LauncherArgs args);
    }

    /**
     * {@link ModeProcessor} doing the task that need to be done before the
     * other processors. It will also export parameters if needed.
     * 
     * @author mey
     *
     */
    private class PreProcessor implements ModeProcessor {
	@Override
	public final void process(LauncherArgs args) {
	    ZmtSimState simState = createSimState(args, context.classLocator);
	    Class<? extends ZmtSimState> simClass = simState.getClass();
	    SimParams defaultParams = createDefaultParams(simClass);
	    Mode mode = args.getMode();

	    if (mode != null) {
		Path outputPath = context.outputPathGenerator.createPaths(simClass, mode, getWorkingDirectory())
			.iterator().next();
		simState.setOutputPath(outputPath);
	    }

	    // export parameters if needed
	    try {
		if (args.getExportAutoParamsPath() != null) {
		    ParamsUtil.writeToXml(AutoParams.fromParams(defaultParams), args.getExportAutoParamsPath());
		}
		if (args.getExportSimParamsPath() != null) {
		    ParamsUtil.writeToXml(defaultParams, args.getExportSimParamsPath());
		}
	    } catch (JAXBException | IOException e) {
		throw new ProcessFailedException(e);
	    }

	    process(args, simState, defaultParams);
	}

	/**
	 * Locates sim state class from package path and instantiates it.
	 * 
	 * @param args
	 * @param classLocator
	 * @return sim state
	 */
	private ZmtSimState createSimState(LauncherArgs args, ClassLocator classLocator) {
	    Class<? extends ZmtSimState> simClass;
	    ZmtSimState simState;

	    try {
		simClass = classLocator.findSimStateClass(args.getSimName());
	    } catch (ClassNotFoundException e) {
		throw new ProcessFailedException(e);
	    }

	    try {
		simState = simClass.getConstructor().newInstance();
	    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
		    | InvocationTargetException | SecurityException e) {
		throw new ProcessFailedException("Failed to instantiate simulation object: " + simClass.getSimpleName(),
			e);
	    } catch (NoSuchMethodException e) {
		throw new ProcessFailedException(
			simClass.getSimpleName() + " needs a non-argument constructor for instantiation.", e);
	    }

	    return simState;
	}

	/**
	 * Create new parameters for {@code simClass}, containing the default
	 * values stated in the associated parameters class.
	 * 
	 * @param simClass
	 *            simulation class the parameters are used for
	 * @return default parameters object
	 */
	private SimParams createDefaultParams(Class<? extends SimState> simClass) {
	    Class<? extends SimParams> paramsClass = ParamsUtil.obtainParamsClass(simClass);
	    try {
		return paramsClass.newInstance();
	    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
		    | SecurityException instantiationFailed) {
		throw new ProcessFailedException(instantiationFailed);
	    }
	}

	/**
	 * Process {@code args} with this {@code ModeProcessor}, using
	 * {@code simState} as simulation object.
	 * 
	 * @param args
	 * @param simState
	 *            raw simulation object not containing parameters
	 * @param defaultParams
	 *            default parameter object with values loaded directly from
	 *            class
	 */
	protected void process(LauncherArgs args, ZmtSimState simState, SimParams defaultParams) {
	    // nothing to do here for the pre processor
	}

    }

    /**
     * Abstract base class that tries to load parameters from file before
     * further processing.
     * 
     * @author mey
     *
     */
    private abstract class LoadParamsProcessor extends PreProcessor {
	@Override
	protected final void process(LauncherArgs args, ZmtSimState simState, SimParams defaultParams) {
	    super.process(args, simState, defaultParams);
	    simState.setParams(loadSimParams(args, context.paramsLoader, defaultParams));
	    process(args, simState);
	}

	/**
	 * Processing after parameters have been loaded into {@code simState}.
	 * 
	 * @param args
	 * @param simState
	 *            simulation object with parameters set
	 */
	protected abstract void process(LauncherArgs args, ZmtSimState simState);

	/**
	 * Obtains parameter class for {@code simClass} and loads it from path
	 * given by {@code args}.
	 * 
	 * @param args
	 * @param paramsLoader
	 * @param defaultParams
	 *            default params object returned when loading failed
	 * @return matching {@link SimParams} object
	 */
	private SimParams loadSimParams(LauncherArgs args, ParamsLoader paramsLoader, SimParams defaultParams) {
	    Class<? extends SimParams> paramsClass = defaultParams.getClass();
	    try {
		return paramsLoader.loadSimParams(args.getSimParamsPath(), paramsClass);
	    } catch (ParamsLoadFailedException loadFailed) {
		// default parameters not found: instantiate new
		if (loadFailed.getCause() instanceof IOException && args.isDefaultSimParamsPath()) {
		    logger.log(Level.WARNING,
			    "Loading simulation parameters from default path failed. Creating new instance.");
		    return defaultParams;
		} else {
		    throw new ProcessFailedException(loadFailed);
		}
	    }
	}

    }

    /**
     * Runs simulation for a single time.
     * 
     * @author mey
     *
     */
    private class SingleProcessor extends LoadParamsProcessor {
	@Override
	protected void process(LauncherArgs args, ZmtSimState simState) {
	    context.simulationLooper.loop(simState, args.getSimTime(), args.getPrintStatusInterval());
	}
    }

    /**
     * Opens the gui for the argument-provided ZmtSimState and parameters.
     * 
     * @author mey
     *
     */
    private class GuiProcessor extends LoadParamsProcessor {
	@Override
	protected void process(LauncherArgs args, final ZmtSimState simState) {
	    Class<? extends ZmtGUIState> guiStateClass;
	    try {
		guiStateClass = context.classLocator.findGuiStateClass(args.getSimName());
	    } catch (ClassNotFoundException e) {
		throw new ProcessFailedException(e);
	    }

	    final ZmtGUIState guiState = createGuiState(guiStateClass, simState);
	    final Iterator<Path> outputPathsIterator = context.outputPathGenerator
		    .createPaths(simState.getClass(), args.getMode(), getWorkingDirectory()).iterator();

	    guiState.addGuiListener(new ZmtGUIState.GuiListener() {

		@Override
		public void onGuiStart() {
		}

		@Override
		public void onGuiFinish() {
		    // set a new output path for the next run
		    simState.setOutputPath(outputPathsIterator.next());
		}
	    });

	    // make the gui visible
	    guiState.createController();
	}

	private ZmtGUIState createGuiState(Class<? extends ZmtGUIState> guiStateClass, ZmtSimState simState) {
	    try {
		// find matching constructor
		for (Constructor<?> constructor : guiStateClass.getConstructors()) {
		    Class<?>[] parameterTypes = constructor.getParameterTypes();
		    if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(simState.getClass())) {
			return (ZmtGUIState) constructor.newInstance(simState);
		    }
		}
	    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
		    | InvocationTargetException | SecurityException e) {
		throw new ProcessFailedException("Failed to instantiate GUI object: " + guiStateClass.getSimpleName(),
			e);
	    }

	    throw new ProcessFailedException(
		    guiStateClass + " needs a constructor matching parameter type: " + simState.getClass());
	}
    }

    /**
     * Runs an automated batch of simulations with varying parameters from
     * {@link AutoParams}.
     * 
     * @author mey
     *
     */
    private class BatchProcessor extends LoadParamsProcessor {
	@Override
	protected void process(LauncherArgs args, ZmtSimState simState) {
	    AutoParams autoParams;
	    try {
		autoParams = context.paramsLoader.loadAutoParams(args.getAutoParamsPath());
	    } catch (ParamsLoadFailedException e) {
		throw new ProcessFailedException(e);
	    }

	    // compile combinations
	    Iterable<Combination> combinations = context.combinationCompiler
		    .compileCombinations(autoParams.getDefinitions());
	    // apply combinations: use params loaded in base as default
	    Iterable<AppliedCombination> appliedCombinations = context.combinationApplier
		    .applyCombinations(combinations, simState.getParams());
	    Iterable<Path> outputPaths = context.outputPathGenerator.createPaths(simState.getClass(),
		    args.getMode(), getWorkingDirectory());
	    // run a simulation for every parameter object
	    context.simulationLooper.loop(simState.getClass(), appliedCombinations, args.getMaxThreads(),
		    autoParams.getSimTime(), args.getPrintStatusInterval(), args.isCombinationInFolderNames(), outputPaths);
	}
    }

    /**
     * {@link RuntimeException} thrown to indicate that the launching process
     * failed.
     * 
     * @author mey
     *
     */
    private static class ProcessFailedException extends RuntimeException {
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
