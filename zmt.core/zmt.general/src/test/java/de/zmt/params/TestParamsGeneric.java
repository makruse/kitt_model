package de.zmt.params;

import java.util.Collection;
import java.util.Collections;

import de.zmt.params.def.OptionalParamDefinition;
import de.zmt.params.def.ParamDefinition;

public class TestParamsGeneric<T extends ParamDefinition> extends BaseParams implements SimParams {
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
    public boolean addOptionalDefinition(OptionalParamDefinition optionalDef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeOptionalDefinition(OptionalParamDefinition optionalDef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getSeed() {
        return 0;
    }

}