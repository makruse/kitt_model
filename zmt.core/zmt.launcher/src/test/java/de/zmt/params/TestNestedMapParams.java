package de.zmt.params;

import java.util.Collection;
import java.util.HashMap;

public class TestNestedMapParams extends MapParamDefinition<String, TestNestedMapParams.Inner> implements ParamsNode {
    private static final long serialVersionUID = 1L;

    public static final String KEY1 = "key 1";
    public static final String KEY2 = "key 2";
    public static final int LEAF_VALUE_1 = 1;
    public static final int LEAF_VALUE_2 = 2;

    public TestNestedMapParams() {
        super(new HashMap<>());
        getMap().put(KEY1, new Inner(LEAF_VALUE_1));
        getMap().put(KEY2, new Inner(LEAF_VALUE_2));
    }

    @Override
    public Collection<? extends ParamDefinition> getDefinitions() {
        return getMap().values();
    }

    static class Inner extends MapParamDefinition<String, TestLeafDefinition> implements ParamsNode {
        private static final long serialVersionUID = 1L;

        public static final String KEY = "inner key";

        public Inner(int leafValue) {
            super(new HashMap<>());
            getMap().put(KEY, new TestLeafDefinition(leafValue));
        }

        @Override
        public Collection<? extends ParamDefinition> getDefinitions() {
            return getMap().values();
        }
    }
}
