package de.zmt.params;

import java.lang.reflect.Field;

import de.zmt.params.accessor.NotAutomatable;

public class TestDefinition implements ParamDefinition {
    private static final long serialVersionUID = 1L;

    public static final Field FIELD_INT = getDeclaredField(TestDefinition.class, "intValue");
    public static final Field FIELD_DOUBLE = getDeclaredField(TestDefinition.class, "doubleValue");
    public static final Field FIELD_NOT_AUTO = TestDefinition.getDeclaredField(TestDefinition.class,
            "notAutomatableValue");

    private String stringValue = "something";
    private int intValue = 3;
    private double doubleValue = 0.9;
    @NotAutomatable
    private String notAutomatableValue = "not automatable";

    public TestDefinition() {
        super();
    }

    public TestDefinition(String stringValue) {
        this.stringValue = stringValue;
    }

    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public String getNotAutomatableValue() {
        return notAutomatableValue;
    }

    @Override
    public String getTitle() {
        return stringValue;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[stringValue=" + stringValue + ", intValue=" + intValue + ", doubleValue="
                + doubleValue + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(doubleValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + intValue;
        result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestDefinition other = (TestDefinition) obj;
        if (Double.doubleToLongBits(doubleValue) != Double.doubleToLongBits(other.doubleValue)) {
            return false;
        }
        if (intValue != other.intValue) {
            return false;
        }
        if (stringValue == null) {
            if (other.stringValue != null) {
                return false;
            }
        } else if (!stringValue.equals(other.stringValue)) {
            return false;
        }
        return true;
    }

}