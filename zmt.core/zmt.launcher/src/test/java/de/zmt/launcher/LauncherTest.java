package de.zmt.launcher;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.zmt.launcher.LauncherArgs.Mode;
import de.zmt.launcher.strategies.ClassLocator;
import de.zmt.launcher.strategies.Combination;
import de.zmt.launcher.strategies.CombinationApplier;
import de.zmt.launcher.strategies.CombinationApplier.AppliedCombination;
import de.zmt.launcher.strategies.CombinationCompiler;
import de.zmt.launcher.strategies.LauncherStrategyContext;
import de.zmt.launcher.strategies.OutputPathGenerator;
import de.zmt.launcher.strategies.ParamsLoader;
import de.zmt.launcher.strategies.SimulationLooper;
import de.zmt.util.ParamsUtil;
import sim.display.Controller;
import sim.display.ZmtGUIState;
import sim.engine.BaseZmtSimState;
import sim.engine.SimState;
import sim.engine.ZmtSimState;
import sim.engine.params.AutoParams;
import sim.engine.params.SimParams;
import sim.engine.params.TestParams;
import sim.engine.params.def.AutoDefinition;
import sim.engine.params.def.FieldLocator;

public class LauncherTest {
    private static final String SIM_PARAMS_STRING_VALUE = "default";
    private static final String COMBINED_PARAMS_STRING_VALUE = "was combined";
    private static final double CMD_LINE_SIM_TIME = 800.3;
    private static final int AUTO_PARAMS_MAX_THREADS = Integer.MAX_VALUE;
    private static final double AUTO_PARAMS_SIM_TIME = 500.8;

    private static final String SIM_PARAMS_EXPORT_PATH = "sim_params_temp.xml";
    private static final String AUTO_PARAMS_EXPORT_PATH = "auto_params_temp.xml";

    private static final LauncherStrategyContext CONTEXT = new LauncherStrategyContext(new TestClassLocator(),
	    new TestParamsLoader(), new TestOutputPathGenerator(), new TestCombinationCompiler(),
	    new TestCombinationApplier(), new TestSimulationLooper());

    private static final TestParams SIM_PARAMS;
    private static final TestParams COMBINED_SIM_PARAMS;
    private static final AutoParams AUTO_PARAMS;
    private static final AutoDefinition AUTO_DEFINITION = new AutoDefinition();
    private static final Set<Combination> COMBINATIONS;
    private static final Set<AppliedCombination> APPLIED_COMBINATIONS;

    static {
	SIM_PARAMS = new TestParams();
	SIM_PARAMS.getDefinition().setStringValue(SIM_PARAMS_STRING_VALUE);

	COMBINED_SIM_PARAMS = new TestParams();
	COMBINED_SIM_PARAMS.getDefinition().setStringValue(COMBINED_PARAMS_STRING_VALUE);

	AUTO_PARAMS = new AutoParams();
	AUTO_PARAMS.setMaxThreads(AUTO_PARAMS_MAX_THREADS);
	AUTO_PARAMS.setSimTime(AUTO_PARAMS_SIM_TIME);
	AUTO_PARAMS.addDefinition(AUTO_DEFINITION);

	Combination combination = new Combination(Collections.<FieldLocator, Object> emptyMap());
	COMBINATIONS = Collections.singleton(combination);
	APPLIED_COMBINATIONS = Collections.singleton(new AppliedCombination(combination, SIM_PARAMS));
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void runWithExportSimParams() throws IOException, JAXBException {
	LauncherStrategyContext context = new LauncherStrategyContext(new TestClassLocator(), null,
		new TestOutputPathGenerator(), null, null, null);
	final Path paramsExportPath = folder.newFile(SIM_PARAMS_EXPORT_PATH).toPath();

	new Launcher(context).run(new LauncherArgs() {

	    @Override
	    public Mode getMode() {
		return null;
	    }

	    @Override
	    public Path getExportSimParamsPath() {
		return paramsExportPath;
	    }

	});

	TestParams readParams = ParamsUtil.readFromXml(paramsExportPath, TestParams.class);
	assertThat(readParams, is(new TestParams()));
    }

    @Test
    public void runWithExportAutoParams() throws IOException, JAXBException {
	LauncherStrategyContext context = new LauncherStrategyContext(new TestClassLocator(), null,
		new TestOutputPathGenerator(), null, null, null);
	final Path autoParamsExportPath = folder.newFile(AUTO_PARAMS_EXPORT_PATH).toPath();

	new Launcher(context).run(new LauncherArgs() {

	    @Override
	    public Mode getMode() {
		return null;
	    }

	    @Override
	    public Path getExportAutoParamsPath() {
		return autoParamsExportPath;
	    }
	});

	AutoParams readParams = ParamsUtil.readFromXml(autoParamsExportPath, AutoParams.class);
	assertThat(readParams, is(AutoParams.fromParams(new TestParams())));
    }

    @Test
    public void runOnDefaultParamsLoadFailed() {
	LauncherStrategyContext context = new LauncherStrategyContext(new TestClassLocator(), new ParamsLoader() {

	    @Override
	    public <T extends SimParams> T loadSimParams(Path simParamsPath, Class<T> simParamsClass)
		    throws ParamsLoadFailedException {
		// just throw exception to indicate that file was not found
		throw new ParamsLoadFailedException(new IOException("Intentionally thrown to make "
			+ Launcher.class.getSimpleName() + " fall back to object instantiation."));
	    }

	    @Override
	    public AutoParams loadAutoParams(Path autoParamsPath) throws ParamsLoadFailedException {
		fail("Wrong method called.");
		return null;
	    }
	}, new TestOutputPathGenerator(), null, null, new SimulationLooper() {

	    @Override
	    public void loop(Class<? extends ZmtSimState> simClass, Iterable<AppliedCombination> simParamsObjects,
		    int maxThreads, double simTime, Iterable<Path> outputPaths) {
		fail("Wrong method called.");
	    }

	    @Override
	    public void loop(ZmtSimState simState, double simTime) {
		assertEquals(new TestParams(), simState.getParams());
	    }
	});

	new Launcher(context).run(new LauncherArgs() {

	    @Override
	    public Mode getMode() {
		return Mode.SINGLE;
	    }
	});
    }

    @Test
    public void runOnSingle() {
	launch(Mode.SINGLE);
    }

    @Test
    public void runOnGui() {
	launch(Mode.GUI);
    }

    @Test
    public void runOnBatch() {
	launch(Mode.BATCH);
    }

    /**
     * Initiates Launcher run with arguments object containing given
     * {@code mode}.
     * 
     * @param mode
     */
    private static void launch(final Mode mode) {
	LauncherArgs data = new LauncherArgs() {

	    @Override
	    public Mode getMode() {
		return mode;
	    }

	    @Override
	    public double getSimTime() {
		return CMD_LINE_SIM_TIME;
	    }
	};

	switch (mode) {
	case GUI:
	    runAndValidate(data, TestGuiState.CONTROLLER_CREATED);
	    TestGuiState.INSTANCE.start();
	    TestGuiState.INSTANCE.finish();
	case SINGLE:
	case BATCH:
	    runAndValidate(data, TestSimState.CREATED);
	    break;
	}
    }

    /**
     * Runs Launcher and validates creation of test classes.
     * 
     * @param data
     * @param created
     *            {@link MutableBoolean} that is set to true after the test
     *            class has been created
     */
    private static void runAndValidate(LauncherArgs data, MutableBoolean created) {
	/*
	 * Only one thread can enter at a time to prevent interference.
	 * Otherwise tests may interfere if run in parallel.
	 */
	synchronized (created) {
	    created.value = false;
	    new Launcher(CONTEXT).run(data);
	    assertThat(created.value, is(true));
	}
    }

    private static class TestClassLocator implements ClassLocator {

	@Override
	public Class<? extends ZmtSimState> findSimStateClass(String simPackagePath) throws ClassNotFoundException {
	    return TestSimState.class;
	}

	@Override
	public Class<? extends ZmtGUIState> findGuiStateClass(String simPackagePath) throws ClassNotFoundException {
	    return TestGuiState.class;
	}

    }

    /** Returns constant parameter objects. */
    private static class TestParamsLoader implements ParamsLoader {

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SimParams> T loadSimParams(Path simParamsPath, Class<T> simParamsClass)
		throws ParamsLoadFailedException {
	    return (T) SIM_PARAMS;
	}

	@Override
	public AutoParams loadAutoParams(Path autoParamsPath) throws ParamsLoadFailedException {
	    return AUTO_PARAMS;
	}

    }

    public static class TestOutputPathGenerator implements OutputPathGenerator {
	private int index;

	@Override
	public Iterable<Path> createPaths(Class<? extends SimState> simClass, Mode mode, Path directory) {
	    return Collections.singleton(Paths.get(String.valueOf(index++)));
	}

    }

    private static class TestCombinationCompiler implements CombinationCompiler {

	@Override
	public Iterable<Combination> compileCombinations(Iterable<AutoDefinition> autoDefinitions) {
	    assertEquals(AUTO_PARAMS.getDefinitions(), autoDefinitions);
	    return COMBINATIONS;
	}

    }

    private static class TestCombinationApplier implements CombinationApplier {

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<AppliedCombination> applyCombinations(Iterable<Combination> combinations,
		SimParams defaultSimParams) {
	    assertEquals(SIM_PARAMS, defaultSimParams);
	    assertEquals(COMBINATIONS, combinations);
	    // type of SIM_PARAMS and COMBINED_SIM_PARAMS match
	    return APPLIED_COMBINATIONS;
	}

    }

    private static class TestSimulationLooper implements SimulationLooper {

	@Override
	public void loop(ZmtSimState simState, double simTime) {
	    assertEquals(TestSimState.class, simState.getClass());
	    assertEquals(SIM_PARAMS, simState.getParams());
	    assertEquals(CMD_LINE_SIM_TIME, simTime, 0);
	}

	@Override
	public void loop(Class<? extends ZmtSimState> simClass, Iterable<AppliedCombination> appliedCombinations,
		int maxThreads, double simTime, Iterable<Path> outputPaths) {
	    assertEquals(TestSimState.class, simClass);
	    assertEquals(APPLIED_COMBINATIONS, appliedCombinations);
	    assertEquals(AUTO_PARAMS_MAX_THREADS, maxThreads);
	    assertEquals(AUTO_PARAMS_SIM_TIME, simTime, 0);
	}
    }

    public static class TestSimState extends BaseZmtSimState<TestParams> {
	private static final long serialVersionUID = 1L;
	public static final Class<TestParams> PARAMS_CLASS = TestParams.class;

	private static final MutableBoolean CREATED = new MutableBoolean();

	public TestSimState() {
	    super();
	    CREATED.value = true;
	}

	// make public for TestGuiState
	@Override
	public Path getOutputPath() {
	    return super.getOutputPath();
	}
    }

    public static class TestGuiState extends ZmtGUIState {
	private static final MutableBoolean CONTROLLER_CREATED = new MutableBoolean();
	private static TestGuiState INSTANCE;


	public TestGuiState(TestSimState state) {
	    super(state);
	    INSTANCE = this;
	}

	@Override
	public void finish() {
	    TestSimState simState = (TestSimState) state;
	    Path outputPath = simState.getOutputPath();
	    super.finish();
	    assertThat("Output path need to change each time the simulation is started.", outputPath,
		    not(simState.getOutputPath()));
	}

	@Override
	public Controller createController() {
	    CONTROLLER_CREATED.value = true;
	    return null;
	}
    }

    /** Boolean wrapper that allows to be passed as a reference. */
    private static class MutableBoolean {
	public boolean value;
    }
}
