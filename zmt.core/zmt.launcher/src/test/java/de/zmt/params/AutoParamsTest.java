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
import de.zmt.params.def.Locator;
import de.zmt.params.def.TestDefinition;
import de.zmt.util.ParamsUtilTest;

public class AutoParamsTest {
    private static final AutoDefinition INT_DEFINITION;
    private static final AutoDefinition DOUBLE_DEFINITION;
    private static final AutoDefinition IN_NESTED_DEFINITION;

    static {
	TestDefinition definition = new TestDefinition();
	TestNestedParams.NestedDefinition nestedDefinition = new TestNestedParams.NestedDefinition();

	Locator intLocator = new Locator(definition.getClass(), TestDefinition.FIELD_INT, definition.getTitle());
	Locator doubleLocator = new Locator(definition.getClass(), TestDefinition.FIELD_DOUBLE, definition.getTitle());
	Locator inNestedLocator = new Locator(nestedDefinition.getClass(),
		TestNestedParams.NestedDefinition.FIELD_IN_NESTED, nestedDefinition.getTitle());

	INT_DEFINITION = new AutoDefinition(intLocator, Collections.singletonList(definition.getIntValue()));
	DOUBLE_DEFINITION = new AutoDefinition(doubleLocator, Collections.singletonList(definition.getDoubleValue()));
	IN_NESTED_DEFINITION = new AutoDefinition(inNestedLocator,
		Collections.singletonList(nestedDefinition.getValueInNested()));
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
	TestDefinitionChild definition = new TestDefinitionChild();
	AutoDefinition intDefinitionInChild = new AutoDefinition(new Locator(definition, TestDefinition.FIELD_INT),
		Collections.singletonList(definition.getIntValue()));
	AutoDefinition doubleDefinitionInChild = new AutoDefinition(
		new Locator(definition, TestDefinition.FIELD_DOUBLE),
		Collections.singletonList(definition.getDoubleValue()));

	AutoParams autoParams = AutoParams.fromParams(new TestParams(definition));
	assertThat(autoParams.getDefinitions(), hasItems(intDefinitionInChild, doubleDefinitionInChild));
    }

    @Test
    public void fromParamsWithNested() {
	AutoParams autoParams = AutoParams.fromParams(new TestParamsGeneric<>(new TestNestedParams()));
	assertThat(autoParams.getDefinitions(), hasItem(IN_NESTED_DEFINITION));
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
