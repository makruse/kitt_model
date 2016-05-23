package de.zmt.launcher.strategies;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import sim.engine.params.def.AutoDefinition;
import sim.engine.params.def.FieldLocator;
import sim.engine.params.def.TestDefinition;

public class DefaultCombinationCompilerTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefaultCombinationCompilerTest.class.getName());

    private static final CombinationCompiler BATCH_COMPILER = new DefaultCombinationCompiler();

    // DEFINITIONS
    private static final AutoDefinition DEFINITION_1 = new AutoDefinition(
	    new FieldLocator(TestDefinition.class, TestDefinition.FIELD_NAME_DOUBLE),
	    Arrays.<Object> asList(1.5, 2.5, 0.5));
    private static final AutoDefinition DEFINITION_2 = new AutoDefinition(
	    new FieldLocator(TestDefinition.class, TestDefinition.FIELD_NAME_INT),
	    Arrays.<Object> asList(4, 8, 2));
    private static final List<AutoDefinition> AUTO_DEFS_LIST = Arrays.asList(DEFINITION_1, DEFINITION_2);

    private Collection<Combination> combinations = collectCombinations(AUTO_DEFS_LIST);

    @Test
    public void compileCombinationsOnEmpty() {
	Collection<Combination> combinationsFromEmpty = collectCombinations(new ArrayList<AutoDefinition>());
	assertTrue("Combinations contain: " + combinationsFromEmpty, combinationsFromEmpty.isEmpty());
    }

    @Test
    public void compileCombinations() {
	int expectedResultSize = 1;
	for (AutoDefinition autoDefinition : AUTO_DEFS_LIST) {
	    expectedResultSize *= autoDefinition.getValues().size();
	}

	assertEquals("Resulting combinations \n" + combinations + "\n are not at expected size.", expectedResultSize,
		combinations.size());
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
