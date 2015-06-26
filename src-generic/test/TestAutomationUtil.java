package test;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

import org.junit.*;
import org.junit.rules.ExpectedException;

import test.resources.*;
import de.zmt.sim.engine.params.def.*;
import de.zmt.sim.engine.params.def.AutoDefinition.NotAutomatable.IllegalAutomationException;
import de.zmt.util.AutomationUtil;

public class TestAutomationUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(TestAutomationUtil.class.getName());

    private static final String FIELD_NAME_DOUBLE = "doubleValue";
    private static final String FIELD_NAME_INT = "intValue";

    // DEFINITIONS
    private static final AutoDefinition DEFINITION_1 = new AutoDefinition(
	    new AutoDefinition.FieldLocator(SimpleDefinition.class, FIELD_NAME_DOUBLE),
	    Arrays.<Object> asList(1.5, 2.5, 0.5));
    private static final AutoDefinition DEFINITION_2 = new AutoDefinition(
	    new AutoDefinition.FieldLocator(SimpleDefinition.class, FIELD_NAME_INT),
	    Arrays.<Object> asList(4, 8, 2));
    private static final List<AutoDefinition> AUTO_DEFS_LIST = Arrays.asList(
	    DEFINITION_1, DEFINITION_2);

    // INVALID DEFINITION
    private static final String FIELD_NAME_STRING = "stringValue";
    private static final AutoDefinition INVALID_DEFINITION = new AutoDefinition(
	    new AutoDefinition.FieldLocator(SimpleDefinition.class, FIELD_NAME_STRING),
	    Collections.<Object> singleton("value"));

    private Collection<Map<AutoDefinition.FieldLocator, Object>> combinations;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
	combinations = AutomationUtil.combineAutoDefs(AUTO_DEFS_LIST);
    }

    @Test
    public void testCombineDefs() {
	int expectedResultSize = 1;
	for (AutoDefinition autoDefinitionNumber : AUTO_DEFS_LIST) {
	    expectedResultSize *= autoDefinitionNumber.getValues().size();
	}

	assertEquals(expectedResultSize, combinations.size());
	logger.info("Resulting combinations are at expected size: "
		+ combinations.size());
    }

    @Test
    public void testApplyCombination() throws IllegalArgumentException,
	    IllegalAccessException, NoSuchFieldException, SecurityException {
	SimpleParams simpleParams = new SimpleParams();
	simpleParams.setDefinition(new SimpleDefinition());

	for (Map<AutoDefinition.FieldLocator, Object> combination : combinations) {
	    AutomationUtil.applyCombination(combination, simpleParams);

	    // get field values from definition object
	    List<String> fieldNames = Arrays.asList(FIELD_NAME_DOUBLE,
		    FIELD_NAME_INT);
	    Collection<Object> fieldValues = new ArrayList<>(fieldNames.size());
	    for (String fieldName : fieldNames) {
		Field declaredField = SimpleDefinition.class
			.getDeclaredField(fieldName);
		declaredField.setAccessible(true);
		fieldValues.add(declaredField.get(simpleParams.getDefinition()));
	    }

	    // check values
	    assertTrue("Incorrect values set to fields.",
		    fieldValues.size() == combination.values().size()
			    && fieldValues.containsAll(combination.values()));
	}
    }

    @Test
    public void testApplyInvalidCombination() {
	Collection<Map<AutoDefinition.FieldLocator, Object>> invalidCombinations = AutomationUtil
		.combineAutoDefs(Collections.singleton(INVALID_DEFINITION));
	SimpleParams simpleParams = new SimpleParams();
	simpleParams.setDefinition(new SimpleDefinition());

	for (Map<AutoDefinition.FieldLocator, Object> combination : invalidCombinations) {
	    thrown.expect(IllegalAutomationException.class);
	    AutomationUtil.applyCombination(combination, simpleParams);
	}
    }
}
