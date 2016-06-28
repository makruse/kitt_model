package de.zmt.params;

import java.util.Collection;

public class TestNestedMapParams extends MapParamDefinition.Default<String, TestNestedMapParams.Inner>
        implements ParamsNode {
    private static final long serialVersionUID = 1L;

    public static final String KEY1 = "key 1";
    public static final String KEY2 = "key 2";
    public static final String LEAF_VALUE_1 = "leaf value 1";
    public static final String LEAF_VALUE_2 = "leaf value 2";

    public TestNestedMapParams() {
        super();
        getMap().put(KEY1, new Inner(LEAF_VALUE_1));
        getMap().put(KEY2, new Inner(LEAF_VALUE_2));
    }

    @Override
    public Collection<? extends ParamDefinition> getDefinitions() {
        return getMap().values();
    }

    static class Inner extends MapParamDefinition.Default<String, TestLeafDefinition> implements ParamsNode {
        private static final long serialVersionUID = 1L;

        public static final String KEY = "inner key";

        public Inner(String leafValue) {
            super();
            getMap().put(KEY, new TestLeafDefinition(leafValue));
        }

        @Override
        public Collection<? extends ParamDefinition> getDefinitions() {
            return getMap().values();
        }
    }
}
