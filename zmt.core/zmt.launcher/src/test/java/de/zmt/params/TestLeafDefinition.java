package de.zmt.params;

import java.lang.reflect.Field;

public class TestLeafDefinition extends BaseParamDefinition {
    private static final long serialVersionUID = 1L;

    public static final Field FIELD_IN_LEAF;

    static {
        try {
            FIELD_IN_LEAF = TestLeafDefinition.class.getDeclaredField("inLeaf");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException();
        }
    }

    public TestLeafDefinition() {
        this("in leaf value");
    }

    public TestLeafDefinition(String inLeaf) {
        this.inLeaf = inLeaf;
    }

    private final String inLeaf;

    public String getInLeaf() {
        return inLeaf;
    }
}