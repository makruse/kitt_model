package de.zmt.params;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

public class TestNestedParams extends BaseParamsNode {
    private static final long serialVersionUID = 1L;

    public static final Field FIELD_LEAF_DEFINITION_1 = TestDefinition.getDeclaredField(TestNestedParams.class,
            "testLeafDefinition1");

    private final TestLeafDefinition testLeafDefinition1 = new TestLeafDefinition("leaf value 1");
    private final TestLeafDefinition testLeafDefinition2 = new TestLeafDefinition("leaf value 2");

    public TestLeafDefinition getTestLeafDefinition1() {
        return testLeafDefinition1;
    }

    public TestLeafDefinition getTestLeafDefinition2() {
        return testLeafDefinition2;
    }

    @Override
    public Collection<? extends ParamDefinition> getDefinitions() {
        return Arrays.asList(testLeafDefinition1, testLeafDefinition2);
    }

    @Override
    public String getTitle() {
	return getClass().getSimpleName();
    }
}