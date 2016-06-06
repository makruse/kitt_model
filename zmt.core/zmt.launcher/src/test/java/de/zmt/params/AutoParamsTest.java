package de.zmt.params;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.zmt.params.def.AutoDefinition;
import de.zmt.params.def.FieldLocator;
import de.zmt.params.def.NotAutomatableFieldDefinition;
import de.zmt.params.def.TestDefinition;
import de.zmt.util.ParamsUtilTest;

public class AutoParamsTest {
    private static final AutoDefinition INT_DEFINITION;
    private static final AutoDefinition DOUBLE_DEFINITION;
    private static final AutoDefinition IN_NESTED_DEFINITION;
    private static final AutoDefinition NOT_AUTOMATABLE_DEFINITION;

    static {
	TestDefinition definition = new TestDefinition();
	TestNestedParams.NestedDefinition nestedDefinition = new TestNestedParams.NestedDefinition();
	NotAutomatableFieldDefinition notAutomatableDefinition = new NotAutomatableFieldDefinition();

	FieldLocator intFieldLocator = new FieldLocator(definition.getClass(), TestDefinition.FIELD_NAME_INT,
		definition.getTitle());
	FieldLocator doubleFieldLocator = new FieldLocator(definition.getClass(), TestDefinition.FIELD_NAME_DOUBLE,
		definition.getTitle());
	FieldLocator inNestedFieldLocator = new FieldLocator(nestedDefinition.getClass(),
		TestNestedParams.NestedDefinition.FIELD_NAME_IN_NESTED, nestedDefinition.getTitle());
	FieldLocator notAutomatableFieldLocator = new FieldLocator(NotAutomatableFieldDefinition.class,
		NotAutomatableFieldDefinition.FIELD_NAME_NOT_AUTO, notAutomatableDefinition.getTitle());

	INT_DEFINITION = new AutoDefinition(intFieldLocator, Collections.singletonList(definition.getIntValue()));
	DOUBLE_DEFINITION = new AutoDefinition(doubleFieldLocator,
		Collections.singletonList(definition.getDoubleValue()));
	IN_NESTED_DEFINITION = new AutoDefinition(inNestedFieldLocator,
		Collections.singletonList(nestedDefinition.getValueInNested()));
	NOT_AUTOMATABLE_DEFINITION = new AutoDefinition(notAutomatableFieldLocator,
		Collections.singletonList(notAutomatableDefinition.getNotAutomatableValue()));
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void fromParams() {
	AutoParams autoParams = AutoParams.fromParams(new TestParams());
	assertThat(autoParams.getDefinitions(), hasItems(INT_DEFINITION, DOUBLE_DEFINITION));
    }

    @Test
    public void fromParamsWithInheritance() {
	AutoParams autoParams = AutoParams.fromParams(new TestParams(new TestDefinitionChild()));
	assertThat(autoParams.getDefinitions(), hasItems(INT_DEFINITION, DOUBLE_DEFINITION));
    }

    @Test
    public void fromParamsWithNested() {
	AutoParams autoParams = AutoParams.fromParams(new TestParamsGeneric<>(new TestNestedParams()));
	assertThat(autoParams.getDefinitions(), hasItem(IN_NESTED_DEFINITION));
    }

    @Test
    public void fromParamsWithNotAutomatable() {
	TestParams params = new TestParams();
	params.setDefinition(new NotAutomatableFieldDefinition());
	AutoParams autoParams = AutoParams.fromParams(params);
	assertThat(autoParams.getDefinitions(), not(hasItem(NOT_AUTOMATABLE_DEFINITION)));
    }

    @Test
    public void xmlSerialization() throws IOException {
	AutoParams autoParams = AutoParams.fromParams(new TestParams());
	Path path = folder.newFile("auto-params.xml").toPath();

	ParamsUtilTest.testWriteRead(autoParams, path);
    }

    private static class TestDefinitionChild extends TestDefinition {
	private static final long serialVersionUID = 1L;

    }
}
