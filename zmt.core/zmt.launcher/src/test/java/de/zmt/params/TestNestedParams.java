package de.zmt.params;

import java.util.Collection;
import java.util.Collections;

import de.zmt.params.def.AbstractParamDefinition;
import de.zmt.params.def.ParamDefinition;

public class TestNestedParams extends AbstractParamDefinition implements NestedParams {
    private static final long serialVersionUID = 1L;

    private final TestNestedParams.NestedDefinition nestedDefinition = new NestedDefinition();

    @Override
    public Collection<? extends ParamDefinition> getDefinitions() {
	return Collections.singleton(nestedDefinition);
    }

    @Override
    public String getTitle() {
	return getClass().getSimpleName();
    }

    public static class NestedDefinition extends AbstractParamDefinition {
	private static final long serialVersionUID = 1L;
	public static final String FIELD_NAME_IN_NESTED = "valueInNested";

	private String valueInNested = "inside nested";

	public String getValueInNested() {
	    return valueInNested;
	}

	@Override
	public String getTitle() {
	    return getClass().getSimpleName() + "[" + valueInNested + "]";
	}
    }
}