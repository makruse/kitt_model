package de.zmt.launcher;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.*;

import org.junit.*;
import org.junit.runners.MethodSorters;

import de.zmt.launcher.LauncherArgs.Mode;
import de.zmt.launcher.strategies.*;
import de.zmt.launcher.strategies.CombinationCompiler.Combination;
import de.zmt.sim.engine.*;
import de.zmt.sim.engine.params.*;
import de.zmt.sim.engine.params.def.AutoDefinition;
import de.zmt.sim.engine.params.def.AutoDefinition.FieldLocator;
import sim.display.*;
import sim.engine.SimState;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LauncherTest {
    private static final String SIM_PARAMS_STRING_VALUE = "default";
    private static final String COMBINED_PARAMS_STRING_VALUE = "was combined";
    private static final int AUTO_PARAMS_MAX_THREADS = Integer.MAX_VALUE;
    private static final double AUTO_PARAMS_SIM_TIME = 500.8;

    private static final LauncherStrategyContext CONTEXT = new LauncherStrategyContext(new TestClassLocator(),
	    new TestParamsLoader(), new TestCombinationCompiler(), new TestCombinationApplier(),
	    new TestSimulationLooper());

    private static final TestParams SIM_PARAMS;
    private static final TestParams COMBINED_SIM_PARAMS;
    private static final AutoParams AUTO_PARAMS;
    private static final AutoDefinition AUTO_DEFINITION = new AutoDefinition();
    private static final Set<Combination> COMBINATIONS = Collections
	    .singleton(new Combination(Collections.<FieldLocator, Object> emptyMap()));

    static {
	SIM_PARAMS = new TestParams();
	SIM_PARAMS.getDefinition().setStringValue(SIM_PARAMS_STRING_VALUE);

	COMBINED_SIM_PARAMS = new TestParams();
	COMBINED_SIM_PARAMS.getDefinition().setStringValue(COMBINED_PARAMS_STRING_VALUE);

	AUTO_PARAMS = new AutoParams();
	AUTO_PARAMS.setMaxThreads(AUTO_PARAMS_MAX_THREADS);
	AUTO_PARAMS.addDefinition(AUTO_DEFINITION);
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

    @Test
    public void runOnDefaultParamsLoadFailed() {
	LauncherStrategyContext context = new LauncherStrategyContext(new TestClassLocator(), new ParamsLoader() {

	    @Override
	    public <T extends SimParams> T loadSimParams(String simParamsPath, Class<T> simParamsClass)
		    throws ParamsLoadFailedException {
		// just throw exception to indicate that file was not found
		throw new ParamsLoadFailedException(new FileNotFoundException("Intentionally thrown to make "
			+ Launcher.class.getSimpleName() + " fall back to object instantiation."));
	    }

	    @Override
	    public AutoParams loadAutoParams(String autoParamsPath) throws ParamsLoadFailedException {
		fail("Wrong method called.");
		return null;
	    }
	}, null, null, new SimulationLooper() {

	    @Override
	    public void loop(Class<? extends ZmtSimState> simClass, Iterable<? extends SimParams> simParamsObjects,
		    int maxThreads, double simTime) {
		fail("Wrong method called.");
	    }

	    @Override
	    public void loop(ZmtSimState simState, double simTime) {
		assertEquals(new TestParams(), simState.getParams());
	    }
	});

	new Launcher(context).run(new LauncherArgs() {

	    @Override
	    Mode getMode() {
		return Mode.SINGLE;
	    }
	});
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
	    Mode getMode() {
		return mode;
	    }

	    @Override
	    double getSimTime() {
		return AUTO_PARAMS_SIM_TIME;
	    }
	};

	switch (mode) {
	case GUI:
	    runAndValidate(data, TestGuiState.CREATED);
	    break;
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
	 * Otherwise tests may interfere if run in parallel, although it doesn't
	 * make sense to run the same tests in parallel.
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
	public Class<? extends GUIState> findGuiStateClass(String simPackagePath) throws ClassNotFoundException {
	    return TestGuiState.class;
	}

    }

    /** Returns constant parameter objects. */
    private static class TestParamsLoader implements ParamsLoader {

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SimParams> T loadSimParams(String simParamsPath, Class<T> simParamsClass)
		throws ParamsLoadFailedException {
	    return (T) SIM_PARAMS;
	}

	@Override
	public AutoParams loadAutoParams(String autoParamsPath) throws ParamsLoadFailedException {
	    return AUTO_PARAMS;
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
	public <T extends SimParams> Iterable<T> applyCombinations(Iterable<Combination> combinations,
		T defaultSimParams) {
	    assertEquals(SIM_PARAMS, defaultSimParams);
	    assertEquals(COMBINATIONS, combinations);
	    // type of SIM_PARAMS and COMBINED_SIM_PARAMS match
	    return (Iterable<T>) Collections.singleton(COMBINED_SIM_PARAMS);
	}

    }

    private static class TestSimulationLooper implements SimulationLooper {

	@Override
	public void loop(ZmtSimState simState, double simTime) {
	    assertEquals(TestSimState.class, simState.getClass());
	    assertEquals(SIM_PARAMS, simState.getParams());
	}

	@Override
	public void loop(Class<? extends ZmtSimState> simClass, Iterable<? extends SimParams> simParamsObjects,
		int maxThreads, double simTime) {
	    assertEquals(TestSimState.class, simClass);
	    assertEquals(Collections.singleton(COMBINED_SIM_PARAMS), simParamsObjects);
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
    }

    public static class TestGuiState extends GUIState {
	private static final MutableBoolean CREATED = new MutableBoolean();

	public TestGuiState(SimState state) {
	    super(state);
	}

	@Override
	public Controller createController() {
	    CREATED.value = true;
	    return null;
	}
    }

    /** Boolean wrapper that allows to be passed as a reference. */
    private static class MutableBoolean {
	public boolean value;
    }
}
