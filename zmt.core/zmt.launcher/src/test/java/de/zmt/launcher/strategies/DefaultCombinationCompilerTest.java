package de.zmt.launcher.strategies;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.zmt.params.AutoDefinition;
import de.zmt.params.TestDefinition;
import de.zmt.params.accessor.Locator;

public class DefaultCombinationCompilerTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefaultCombinationCompilerTest.class.getName());

    private static final CombinationCompiler BATCH_COMPILER = new DefaultCombinationCompiler();

    // DEFINITIONS
    private static final AutoDefinition DEFINITION_1 = new AutoDefinition(
	    new Locator(TestDefinition.class, TestDefinition.FIELD_DOUBLE), Arrays.asList(1.5, 2.5, 0.5));
    private static final AutoDefinition DEFINITION_2 = new AutoDefinition(
	    new Locator(TestDefinition.class, TestDefinition.FIELD_INT), Arrays.asList(4, 8, 2));

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void compileCombinationsOnEmpty() {
	compileCombinations();
    }

    @Test
    public void compileCombinationsOnSingle() {
	compileCombinations(DEFINITION_1);
    }

    @Test
    public void compileCombinationsOnMultiple() {
	compileCombinations(DEFINITION_1, DEFINITION_2);
    }

    @Test
    public void compileCombinationsOnDuplicate() {
	thrown.expect(IllegalArgumentException.class);
	compileCombinations(DEFINITION_1, DEFINITION_1);
    }

    private static void compileCombinations(AutoDefinition... autoDefinitions) {
	List<AutoDefinition> definitions = Arrays.asList(autoDefinitions);
	Locator[] locators = Arrays.stream(autoDefinitions).map(definition -> definition.getLocator())
		.toArray(Locator[]::new);
	Collection<Combination> combinations = collectCombinations(definitions);

	assertThat(combinations, hasSize(computeExpectedResultsNumber(definitions)));
	for (Combination combination : combinations) {
	    assertThat(combination.keySet(), contains(locators));
	}
    }

    /**
     * 
     * @param autoDefinitions
     * @return expected number of combination results for given auto definitions
     */
    private static int computeExpectedResultsNumber(List<AutoDefinition> autoDefinitions) {
	if (autoDefinitions.isEmpty()) {
	    return 0;
	}

	int expectedResultSize = 1;
	for (AutoDefinition autoDefinition : autoDefinitions) {
	    expectedResultSize *= autoDefinition.getValues().size();
	}
	return expectedResultSize;
    }

    /**
     * @param autoDefinitions
     * @return returned combinations in a collection
     */
    private static Collection<Combination> collectCombinations(Iterable<AutoDefinition> autoDefinitions) {
	Collection<Combination> combinations = new ArrayList<>();
	for (Combination combination : BATCH_COMPILER.compileCombinations(autoDefinitions)) {
	    combinations.add(combination);
	}

	return combinations;
    }
}
