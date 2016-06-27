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
import de.zmt.util.ParamsUtilTest;

public class AutoParamsTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void fromParams() {
        TestDefinition definition = new TestDefinition();
        Locator intLocator = new Locator(TestParams.FIELD_DEFINITION, TestDefinition.FIELD_INT);
        Locator doubleLocator = new Locator(TestParams.FIELD_DEFINITION, TestDefinition.FIELD_DOUBLE);
        AutoDefinition intDefinition = new AutoDefinition(intLocator,
                Collections.singletonList(definition.getIntValue()));
        AutoDefinition doubleDefinition = new AutoDefinition(doubleLocator,
                Collections.singletonList(definition.getDoubleValue()));

        AutoParams autoParams = AutoParams.fromParams(new TestParams());
        assertThat(autoParams.getDefinitions(), hasItems(intDefinition, doubleDefinition));
    }

    @Test
    public void fromParamsWithInheritance() {
        TestDefinition childDefinition = new TestDefinition() {
            private static final long serialVersionUID = 1L;
        };
        AutoDefinition intDefinitionInChild = new AutoDefinition(
                new Locator(TestParams.FIELD_DEFINITION, TestDefinition.FIELD_INT),
                Collections.singletonList(childDefinition.getIntValue()));
        AutoDefinition doubleDefinitionInChild = new AutoDefinition(
                new Locator(TestParams.FIELD_DEFINITION, TestDefinition.FIELD_DOUBLE),
                Collections.singletonList(childDefinition.getDoubleValue()));

        AutoParams autoParams = AutoParams.fromParams(new TestParams(childDefinition));
        assertThat(autoParams.getDefinitions(), hasItems(intDefinitionInChild, doubleDefinitionInChild));
    }

    @Test
    public void fromParamsWithNested() {
        TestNestedParams testNestedParams = new TestNestedParams();
        TestLeafDefinition testLeafDefinition1 = testNestedParams.getTestLeafDefinition1();
        Locator inLeafLocator1 = new Locator(TestParamsGeneric.FIELD_DEFINITION,
                TestNestedParams.FIELD_LEAF_DEFINITION_1,
                TestLeafDefinition.FIELD_IN_LEAF);
        AutoDefinition inLeafDefinition = new AutoDefinition(inLeafLocator1,
                Collections.singletonList(testLeafDefinition1.getInLeaf()));

        AutoParams autoParams = AutoParams.fromParams(new TestParamsGeneric<>(new TestNestedParams()));
        assertThat(autoParams.getDefinitions(), hasItem(inLeafDefinition));
    }

    @Test
    public void fromParamsWithNestedMap() {
        Locator inNestedMapLocator = new Locator(TestParamsGeneric.FIELD_DEFINITION, TestNestedMapParams.KEY1,
                TestNestedMapParams.Inner.KEY, TestLeafDefinition.FIELD_IN_LEAF);
        AutoDefinition inNestedMapDefinition = new AutoDefinition(inNestedMapLocator,
                Collections.singletonList(TestNestedMapParams.LEAF_VALUE_1));

        AutoParams autoParams = AutoParams.fromParams(new TestParamsGeneric<>(new TestNestedMapParams()));
        assertThat(autoParams.getDefinitions(), hasItem(inNestedMapDefinition));
    }

    @Test
    public void xmlSerialization() throws IOException {
        AutoParams autoParams = AutoParams.fromParams(new TestParams());
        Path path = folder.newFile("auto-params.xml").toPath();

        ParamsUtilTest.testWriteRead(autoParams, path);
    }
}
