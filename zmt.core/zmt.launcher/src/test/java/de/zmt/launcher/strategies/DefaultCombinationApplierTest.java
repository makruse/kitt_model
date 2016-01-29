package de.zmt.launcher.strategies;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.zmt.launcher.strategies.CombinationApplier.AppliedCombination;
import sim.engine.params.NotAutomatableFieldDefinition;
import sim.engine.params.SimParams;
import sim.engine.params.TestDefinition;
import sim.engine.params.TestParams;
import sim.engine.params.def.FieldLocator;
import sim.engine.params.def.ParamDefinition;
import sim.engine.params.def.ParamDefinition.NotAutomatable.IllegalAutomationException;

public class DefaultCombinationApplierTest {
    private static final CombinationApplier COMBINATION_APPLIER = new DefaultCombinationApplier();

    private static final List<String> VALID_FIELD_NAMES = Arrays.asList(TestDefinition.FIELD_NAME_INT,
	    TestDefinition.FIELD_NAME_DOUBLE);
    private static final List<Object> VALID_FIELD_VALUES = Arrays.<Object> asList(4, 2.5);
    private static final Combination VALID_COMBINATION = DefaultCombinationApplierTest
	    .createCombination(VALID_FIELD_NAMES, VALID_FIELD_VALUES);
    private static final Combination INHERITED_COMBINATION = new Combination(Collections.singletonMap(
	    new FieldLocator(TestDefinitionChild.class, TestDefinitionChild.FIELD_NAME_VALUE_IN_CHILD), (Object) 2));
    private static final Combination EMPTY_COMBINATION = new Combination(Collections.<FieldLocator, Object> emptyMap());
    private static final Combination INVALID_COMBINATION = new Combination(Collections.singletonMap(
	    new FieldLocator(NotAutomatableFieldDefinition.class, NotAutomatableFieldDefinition.FIELD_NAME_NOT_AUTO),
	    (Object) "value"));

    private TestParams simParams;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
	simParams = new TestParams();
    }

    @Test
    public void applyCombinationOnEmpty() {
	List<AppliedCombination> results = makeList(
		COMBINATION_APPLIER.applyCombinations(Collections.singleton(EMPTY_COMBINATION), simParams));
	assertThat(results, hasSize(1));
	assertThat(results.get(0).combination, is(EMPTY_COMBINATION));
	assertThat("Applying an empty combination should not alter any parameters", (TestParams) results.get(0).result,
		is(simParams));
    }

    @Test
    public void applyCombinationOnValid() {
	List<AppliedCombination> results = makeList(
		COMBINATION_APPLIER.applyCombinations(Collections.singleton(VALID_COMBINATION), simParams));
	assertThat(results, hasSize(1));
	assertThat(results.get(0).combination, is(VALID_COMBINATION));
	validateResultParams(results.get(0).result);
    }

    /** Tests if a field can be set that was declared in a parent class. */
    @Test
    public void applyCombinationOnInherited() {
	simParams.setDefinition(new TestDefinitionChild());
	List<AppliedCombination> results = makeList(
		COMBINATION_APPLIER.applyCombinations(Collections.singleton(VALID_COMBINATION), simParams));
	assertThat(results, hasSize(1));
	validateResultParams(results.get(0).result);
    }

    @Test
    public void applyCombinationOnNotAutomatable() {
	simParams.setDefinition(new NotAutomatableFieldDefinition());

	thrown.expect(IllegalAutomationException.class);
	/*
	 * makeList is just used to iterate here, combinations are applied on
	 * the fly.
	 */
	makeList(COMBINATION_APPLIER.applyCombinations(Collections.singleton(INVALID_COMBINATION), simParams));
    }

    @Test
    public void applyCombinationWithoutMatch() {
	thrown.expect(IllegalArgumentException.class);
	/*
	 * makeList is just used to iterate here, combinations are applied on
	 * the fly.
	 */
	makeList(COMBINATION_APPLIER.applyCombinations(Collections.singleton(INHERITED_COMBINATION), simParams));
    }

    @Test
    public void applyCombinationOnAmbigousMatch() {
	simParams = new TestParamsMulti();
	thrown.expect(IllegalArgumentException.class);
	/*
	 * makeList is just used to iterate here, combinations are applied on
	 * the fly.
	 */
	makeList(COMBINATION_APPLIER.applyCombinations(Collections.singleton(VALID_COMBINATION), simParams));
    }

    /**
     * Associates {@code fieldNames} to {@code values} to a combination.
     * 
     * @param fieldNames
     * @param values
     * @return combination for {@code declaringClass}
     */
    private static Combination createCombination(List<String> fieldNames, List<Object> values) {
	Map<FieldLocator, Object> combinationMap = new LinkedHashMap<>();
	for (int i = 0; i < fieldNames.size(); i++) {
	    combinationMap.put(new FieldLocator(TestDefinition.class, fieldNames.get(i)), values.get(i));
	}
	return new Combination(combinationMap);
    }

    /**
     * Make a {@code List} from an {@code Iterable}, which could be a stream.
     * 
     * @param iterable
     * @return {@code List} from {@code Iterable}
     */
    private static <T> List<T> makeList(Iterable<T> iterable) {
	List<T> list = new ArrayList<>();
	for (T element : iterable) {
	    list.add(element);
	}

	return list;
    }

    /**
     * Check if combination has been applied successfully
     * 
     * @param resultParams
     */
    private static void validateResultParams(SimParams resultParams) {
	// get field values from definition object
	Collection<Object> fieldValues = new ArrayList<>(VALID_FIELD_NAMES.size());
	try {
	    for (String fieldName : VALID_FIELD_NAMES) {
		Field declaredField = TestDefinition.class.getDeclaredField(fieldName);
		declaredField.setAccessible(true);
		fieldValues.add(declaredField.get(((TestParams) resultParams).getDefinition()));
	    }
	} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
	    throw new RuntimeException(e);
	}

	// compare to those of definitions
	assertTrue(
		"Incorrect values set to fields.\nvalues: " + fieldValues + "\ncombination: "
			+ VALID_COMBINATION.values(),
		fieldValues.size() == VALID_COMBINATION.size() && fieldValues.containsAll(VALID_FIELD_VALUES));
    }

    private static class TestDefinitionChild extends TestDefinition {
	private static final long serialVersionUID = 1L;
	private static final String FIELD_NAME_VALUE_IN_CHILD = "valueInChild";

	@SuppressWarnings("unused") // used via reflection
	private final int valueInChild = 1;
    }

    private static class TestParamsMulti extends TestParams {
	private static final long serialVersionUID = 1L;

	@Override
	public Collection<ParamDefinition> getDefinitions() {
	    Collection<ParamDefinition> definitions = new ArrayList<>(super.getDefinitions());
	    definitions.add(new TestDefinition());
	    return definitions;
	}

    }
}
