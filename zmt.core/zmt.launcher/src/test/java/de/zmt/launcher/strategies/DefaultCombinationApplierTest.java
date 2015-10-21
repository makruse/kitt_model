package de.zmt.launcher.strategies;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import de.zmt.launcher.strategies.CombinationCompiler.Combination;
import sim.engine.params.*;
import sim.engine.params.def.AutoDefinition.FieldLocator;
import sim.engine.params.def.ParamDefinition.NotAutomatable.IllegalAutomationException;

public class DefaultCombinationApplierTest {
    private static final CombinationApplier COMBINATION_APPLIER = new DefaultCombinationApplier();

    private static final List<String> VALID_FIELD_NAMES = Arrays.asList(
	    TestDefinition.FIELD_NAME_INT, TestDefinition.FIELD_NAME_DOUBLE);
    private static final List<Object> VALID_FIELD_VALUES = Arrays
	    .<Object> asList(4, 2.5);
    private static final Combination VALID_COMBINATION = DefaultCombinationApplierTest
	    .createCombination(VALID_FIELD_NAMES, VALID_FIELD_VALUES);
    private static final Combination EMPTY_COMBINATION = new Combination(
	    Collections.<FieldLocator, Object> emptyMap());
    private static final Combination INVALID_COMBINATION = new Combination(
	    Collections.singletonMap(new FieldLocator(
		    NotAutomatableFieldDefinition.class,
		    NotAutomatableFieldDefinition.FIELD_NAME_NOT_AUTO),
		    (Object) "value"));

    private static final TestParams SIM_PARAMS = new TestParams();

    /**
     * Associates {@code fieldNames} to {@code values} to a combination.
     * 
     * @param fieldNames
     * @param values
     * @return combination for {@link TestDefinition}
     */
    private static Combination createCombination(List<String> fieldNames,
	    List<Object> values) {
	Map<FieldLocator, Object> combinationMap = new LinkedHashMap<>();
	for (int i = 0; i < fieldNames.size(); i++) {
	    combinationMap.put(new FieldLocator(TestDefinition.class,
		    fieldNames.get(i)), values.get(i));
	}
	return new Combination(combinationMap);
    }

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
    private static void validateResultParams(TestParams resultParams) {
	// get field values from definition object
	Collection<Object> fieldValues = new ArrayList<>(
		VALID_FIELD_NAMES.size());
	try {
	    for (String fieldName : VALID_FIELD_NAMES) {
		Field declaredField = TestDefinition.class
			.getDeclaredField(fieldName);
		declaredField.setAccessible(true);
		fieldValues
			.add(declaredField.get(resultParams.getDefinition()));
	    }
	} catch (NoSuchFieldException | SecurityException
		| IllegalArgumentException | IllegalAccessException e) {
	    throw new RuntimeException(e);
	}

	// compare to those of definitions
	assertTrue(
		"Incorrect values set to fields.\nvalues: " + fieldValues
			+ "\ncombination: " + VALID_COMBINATION.values(),
		fieldValues.size() == VALID_COMBINATION.size()
			&& fieldValues.containsAll(VALID_FIELD_VALUES));
    }

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void applyCombinationOnEmpty() {
	List<TestParams> resultParams = makeList(COMBINATION_APPLIER
		.applyCombinations(Collections.singleton(EMPTY_COMBINATION),
			SIM_PARAMS));
	assertEquals(1, resultParams.size());
	assertEquals(
		"Applying an empty combination should not alter any parameters",
		SIM_PARAMS, resultParams.get(0));
    }

    @Test
    public void applyCombinationOnValid() {
	List<TestParams> resultParams = makeList(COMBINATION_APPLIER
		.applyCombinations(Collections.singleton(VALID_COMBINATION),
			SIM_PARAMS));
	assertEquals(1, resultParams.size());
	validateResultParams(resultParams.get(0));
    }

    @Test
    public void applyCombinationOnInvalid() {
	TestParams params = SIM_PARAMS;
	params.setDefinition(new NotAutomatableFieldDefinition());

	thrown.expect(IllegalAutomationException.class);
	// makeList is used to iterate here, combinations are applied on the fly
	makeList(COMBINATION_APPLIER.applyCombinations(
		Collections.singleton(INVALID_COMBINATION), params));
    }

}
