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
        this(0);
    }

    public TestLeafDefinition(int inLeaf) {
        this.inLeaf = inLeaf;
    }

    private final int inLeaf;

    public int getInLeaf() {
        return inLeaf;
    }
}