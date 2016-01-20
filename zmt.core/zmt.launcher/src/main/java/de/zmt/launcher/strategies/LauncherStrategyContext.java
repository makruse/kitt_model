package de.zmt.launcher.strategies;

/**
 * Context for {@link de.zmt.launcher.Launcher}, providing necessary
 * {@link LauncherStrategy} objects.
 * 
 * @author mey
 *
 */
public class LauncherStrategyContext {
    public final ClassLocator classLocator;
    public final ParamsLoader paramsLoader;
    public final OutputPathGenerator outputPathGenerator;
    public final CombinationCompiler combinationCompiler;
    public final CombinationApplier combinationApplier;
    public final SimulationLooper simulationLooper;

    public LauncherStrategyContext(ClassLocator classLocator, ParamsLoader paramsLoader,
	    OutputPathGenerator outputPathGenerator, CombinationCompiler combinationCompiler,
	    CombinationApplier combinationApplier, SimulationLooper simulationLooper) {
	super();
	this.classLocator = classLocator;
	this.paramsLoader = paramsLoader;
	this.outputPathGenerator = outputPathGenerator;
	this.combinationCompiler = combinationCompiler;
	this.combinationApplier = combinationApplier;
	this.simulationLooper = simulationLooper;
    }

    /**
     * Creates a {@link LauncherStrategyContext} with default strategies.
     * 
     * @return default context
     */
    public static LauncherStrategyContext createDefault() {
	return new LauncherStrategyContext(new DefaultClassLocator(), new DefaultParamsLoader(),
		new DefaultOutputPathGenerator(), new DefaultCombinationCompiler(), new DefaultCombinationApplier(),
		new DefaultSimulationLooper());
    }
}
