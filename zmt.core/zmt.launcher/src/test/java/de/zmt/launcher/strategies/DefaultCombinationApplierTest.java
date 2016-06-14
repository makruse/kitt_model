package de.zmt.launcher.strategies;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

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
import de.zmt.params.SimParams;
import de.zmt.params.TestNestedParams;
import de.zmt.params.TestParams;
import de.zmt.params.TestParamsGeneric;
import de.zmt.params.def.Locator;
import de.zmt.params.def.ParamDefinition;
import de.zmt.params.def.TestDefinition;

public class DefaultCombinationApplierTest {
    private static final CombinationApplier COMBINATION_APPLIER = new DefaultCombinationApplier();

    private static final List<Field> VALID_FIELDS = Arrays.asList(TestDefinition.FIELD_INT,
	    TestDefinition.FIELD_DOUBLE);
    private static final List<Object> VALID_FIELD_VALUES = Arrays.<Object> asList(4, 2.5);
    private static final Combination VALID_COMBINATION = createCombination(VALID_FIELDS, VALID_FIELD_VALUES);
    private static final Combination INHERITED_COMBINATION = new Combination(Collections
	    .singletonMap(new Locator(TestDefinitionChild.class, TestDefinitionChild.FIELD_IN_CHILD), (Object) 2));
    private static final Combination EMPTY_COMBINATION = new Combination(Collections.<Locator, Object> emptyMap());
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

    /** Tests if a variable can be set in a definition inside a definition. */
    @Test
    public void applyCombinationOnNested() {
	TestParamsGeneric<TestNestedParams> paramsWithNested = new TestParamsGeneric<>(new TestNestedParams());
	String combinationValue = "changed in nested";
	Combination nestedCombination = new Combination(Collections.singletonMap(
		new Locator(TestNestedParams.NestedDefinition.class, TestNestedParams.NestedDefinition.FIELD_IN_NESTED),
		combinationValue));
	List<AppliedCombination> results = makeList(
		COMBINATION_APPLIER.applyCombinations(Collections.singleton(nestedCombination), paramsWithNested));

	assertThat(results, hasSize(1));
	Collection<? extends ParamDefinition> resultDefinitions = results.get(0).result.getDefinitions();
	assertThat(resultDefinitions, hasSize(1));
	assertThat(resultDefinitions.iterator().next(), is(instanceOf(TestNestedParams.class)));
	TestNestedParams nestingDefinition = (TestNestedParams) resultDefinitions.iterator().next();
	Collection<? extends ParamDefinition> nestedDefinitions = nestingDefinition.getDefinitions();
	assertThat(nestedDefinitions, hasSize(1));
	assertThat(nestedDefinitions.iterator().next(), is(instanceOf(TestNestedParams.NestedDefinition.class)));
	TestNestedParams.NestedDefinition nestedDefinition = (TestNestedParams.NestedDefinition) nestedDefinitions
		.iterator().next();
	assertThat(nestedDefinition.getValueInNested(), is(combinationValue));
    }

    /**
     * Associates {@code fields} to {@code values} to a combination.
     * 
     * @param fields
     * @param values
     * @return combination for {@code declaringClass}
     */
    private static Combination createCombination(List<Field> fields, List<Object> values) {
	Map<Locator, Object> combinationMap = new LinkedHashMap<>();
	for (int i = 0; i < fields.size(); i++) {
	    combinationMap.put(new Locator(TestDefinition.class, fields.get(i)), values.get(i));
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
     * Checks if combination has been applied successfully.
     * 
     * @param resultParams
     */
    private static void validateResultParams(SimParams resultParams) {
	// get field values from definition object
	Collection<Object> fieldValues = new ArrayList<>(VALID_FIELDS.size());
	for (Field field : VALID_FIELDS) {
	    field.setAccessible(true);
	    try {
		fieldValues.add(field.get(((TestParams) resultParams).getDefinition()));
	    } catch (IllegalAccessException e) {
		throw new RuntimeException(e);
	    }
	}

	// compare to those of definitions
	assertThat(fieldValues, is(VALID_FIELD_VALUES));
    }

    private static class TestDefinitionChild extends TestDefinition {
	private static final long serialVersionUID = 1L;
	private static final Field FIELD_IN_CHILD = TestDefinition.getDeclaredField(TestDefinitionChild.class,
		"inChild");

	@SuppressWarnings("unused") // used via reflection
	private final int inChild = 1;
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
