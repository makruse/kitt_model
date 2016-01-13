package de.zmt.launcher;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import javax.xml.bind.JAXBException;

import de.zmt.launcher.LauncherArgs.Mode;
import de.zmt.launcher.strategies.*;
import de.zmt.launcher.strategies.CombinationCompiler.Combination;
import de.zmt.launcher.strategies.ParamsLoader.ParamsLoadFailedException;
import de.zmt.util.ParamsUtil;
import sim.display.GUIState;
import sim.engine.*;
import sim.engine.params.*;

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
	    SimParams defaultParams = createDefaultParams(simState.getClass());

	    // export parameters if needed
	    try {
		if (args.getExportAutoParamsFile() != null) {
		    ParamsUtil.writeToXml(AutoParams.fromParams(defaultParams), args.getExportAutoParamsFile());
		}
		if (args.getExportSimParamsFile() != null) {
		    ParamsUtil.writeToXml(defaultParams, args.getExportSimParamsFile());
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
	public void process(LauncherArgs args, ZmtSimState simState, SimParams defaultParams) {
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
	public final void process(LauncherArgs args, ZmtSimState simState, SimParams defaultParams) {
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
	public abstract void process(LauncherArgs args, ZmtSimState simState);

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
		if (loadFailed.getCause() instanceof FileNotFoundException && args.isDefaultSimParamsPath()) {
		    Launcher.logger.log(Level.WARNING,
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
	public void process(LauncherArgs args, ZmtSimState simState) {
	    context.simulationLooper.loop(simState, args.getSimTime());
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
	public void process(LauncherArgs args, ZmtSimState simState) {
	    Class<? extends GUIState> guiStateClass;
	    try {
		guiStateClass = context.classLocator.findGuiStateClass(args.getSimName());
	    } catch (ClassNotFoundException e) {
		throw new ProcessFailedException(e);
	    }

	    // make the gui visible
	    createGuiState(guiStateClass, simState).createController();
	}

	private GUIState createGuiState(Class<? extends GUIState> guiStateClass, ZmtSimState simState) {
	    try {
		// find matching constructor
		for (Constructor<?> constructor : guiStateClass.getConstructors()) {
		    Class<?>[] parameterTypes = constructor.getParameterTypes();
		    if (parameterTypes.length == 1
			    && parameterTypes[0].isAssignableFrom(simState.getClass())) {
			return (GUIState) constructor.newInstance(simState);
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
	public void process(LauncherArgs args, ZmtSimState simState) {
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
	    Iterable<SimParams> simParamsObjects = context.combinationApplier.applyCombinations(combinations,
		    simState.getParams());
	    // run a simulation for every parameter object
	    context.simulationLooper.loop(simState.getClass(), simParamsObjects, autoParams.getMaxThreads(),
		    autoParams.getSimTime());
	}
    }

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
