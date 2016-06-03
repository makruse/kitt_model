package de.zmt.params;

import de.zmt.params.def.TestDefinition;

public class TestParams extends TestParamsGeneric<TestDefinition> {
    private static final long serialVersionUID = 1L;

    public TestParams() {
	this(new TestDefinition());
    }

    public TestParams(TestDefinition testDefinition) {
	super(testDefinition);
    }
}