package de.zmt.params;

import java.util.Collection;
import java.util.Collections;

public class TestParamsGeneric<T extends ParamDefinition> extends BaseParamsNode implements SimParams {
    private static final long serialVersionUID = 1L;

    private T testDefinition;

    public TestParamsGeneric() {
	super();
    }

    public TestParamsGeneric(T testDefinition) {
	super();
	this.testDefinition = testDefinition;
    }

    public T getDefinition() {
        return testDefinition;
    }

    public void setDefinition(T testDefinition) {
        this.testDefinition = testDefinition;
    }

    @Override
    public Collection<ParamDefinition> getDefinitions() {
        return Collections.singleton(testDefinition);
    }

    @Override
    public long getSeed() {
        return 0;
    }

}