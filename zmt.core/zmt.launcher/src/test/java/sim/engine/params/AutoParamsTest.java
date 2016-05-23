package sim.engine.params;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import sim.engine.params.def.AutoDefinition;
import sim.engine.params.def.FieldLocator;
import sim.engine.params.def.NotAutomatableFieldDefinition;
import sim.engine.params.def.TestDefinition;

public class AutoParamsTest {
    private static final AutoDefinition INT_DEFINITION;
    private static final AutoDefinition DOUBLE_DEFINITION;
    private static final AutoDefinition NOT_AUTOMATABLE_DEFINITION;

    static {
	TestDefinition definition = new TestDefinition();
	NotAutomatableFieldDefinition notAutomatableDefinition = new NotAutomatableFieldDefinition();
	FieldLocator intFieldLocator = new FieldLocator(TestDefinition.class, TestDefinition.FIELD_NAME_INT,
		definition.getTitle());
	FieldLocator doubleFieldLocator = new FieldLocator(TestDefinition.class, TestDefinition.FIELD_NAME_DOUBLE,
		definition.getTitle());
	FieldLocator notAutomatableFieldLocator = new FieldLocator(NotAutomatableFieldDefinition.class,
		NotAutomatableFieldDefinition.FIELD_NAME_NOT_AUTO, notAutomatableDefinition.getTitle());

	INT_DEFINITION = new AutoDefinition(intFieldLocator,
		Collections.<Object> singletonList(definition.getIntValue()));
	DOUBLE_DEFINITION = new AutoDefinition(doubleFieldLocator,
		Collections.<Object> singletonList(definition.getDoubleValue()));
	NOT_AUTOMATABLE_DEFINITION = new AutoDefinition(notAutomatableFieldLocator,
		Collections.<Object> singletonList(notAutomatableDefinition.getNotAutomatableValue()));
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void fromParams() {
	AutoParams autoParams = AutoParams.fromParams(new TestParams());
	assertThat(autoParams.getDefinitions(), hasItems(INT_DEFINITION, DOUBLE_DEFINITION));
    }

    @Test
    public void fromParamsWithNotAutomatable() {
	TestParams params = new TestParams();
	params.setDefinition(new NotAutomatableFieldDefinition());
	AutoParams autoParams = AutoParams.fromParams(params);
	assertThat(autoParams.getDefinitions(), not(hasItem(NOT_AUTOMATABLE_DEFINITION)));
    }

}
