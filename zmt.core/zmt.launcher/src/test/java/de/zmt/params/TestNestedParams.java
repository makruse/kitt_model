package de.zmt.params;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

import de.zmt.params.def.BaseParamDefinition;
import de.zmt.params.def.ParamDefinition;

public class TestNestedParams extends BaseParamDefinition implements NestedParams {
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

    public static class NestedDefinition extends BaseParamDefinition {
	private static final long serialVersionUID = 1L;
	public static final String FIELD_NAME_IN_NESTED = "inNested";
	public static final Field FIELD_IN_NESTED;

	static {
	    try {
		FIELD_IN_NESTED = NestedDefinition.class.getDeclaredField(FIELD_NAME_IN_NESTED);
	    } catch (NoSuchFieldException e) {
		throw new RuntimeException();
	    }
	}

	private String inNested = "inside nested";

	public String getValueInNested() {
	    return inNested;
	}

	@Override
	public String getTitle() {
	    return getClass().getSimpleName() + "[" + inNested + "]";
	}
    }
}