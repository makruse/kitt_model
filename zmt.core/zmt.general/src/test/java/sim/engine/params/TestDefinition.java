package sim.engine.params;

import sim.engine.params.def.ParamDefinition;

@SuppressWarnings({ "serial" })
public class TestDefinition implements ParamDefinition {
    public static final String FIELD_NAME_INT = "intValue";
    public static final String FIELD_NAME_DOUBLE = "doubleValue";

    private String stringValue = "something";
    private float floatValue = 0.5f;
    private int intValue = 3;
    private double doubleValue = 0.9;
    private long longValue = 55;

    public String getStringValue() {
	return stringValue;
    }

    public void setStringValue(String stringValue) {
	this.stringValue = stringValue;
    }

    public float getFloatValue() {
	return floatValue;
    }

    public void setFloatValue(float floatValue) {
	this.floatValue = floatValue;
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

    public long getLongValue() {
	return longValue;
    }

    public void setLongValue(long longValue) {
	this.longValue = longValue;
    }

    @Override
    public String getTitle() {
	return stringValue;
    }

    @Override
    public String toString() {
	return "TestDefinition [stringValue=" + stringValue + ", floatValue="
		+ floatValue + ", intValue=" + intValue + ", doubleValue="
		+ doubleValue + ", longValue=" + longValue + "]";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	long temp;
	temp = Double.doubleToLongBits(doubleValue);
	result = prime * result + (int) (temp ^ (temp >>> 32));
	result = prime * result + Float.floatToIntBits(floatValue);
	result = prime * result + intValue;
	result = prime * result + (int) (longValue ^ (longValue >>> 32));
	result = prime * result
		+ ((stringValue == null) ? 0 : stringValue.hashCode());
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
	if (Double.doubleToLongBits(doubleValue) != Double
		.doubleToLongBits(other.doubleValue)) {
	    return false;
	}
	if (Float.floatToIntBits(floatValue) != Float
		.floatToIntBits(other.floatValue)) {
	    return false;
	}
	if (intValue != other.intValue) {
	    return false;
	}
	if (longValue != other.longValue) {
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