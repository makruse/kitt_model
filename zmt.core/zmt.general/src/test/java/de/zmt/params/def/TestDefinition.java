package de.zmt.params.def;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class TestDefinition implements ParamDefinition {
    private static final long serialVersionUID = 1L;

    public static final String FIELD_NAME_INT = "intValue";
    public static final String FIELD_NAME_DOUBLE = "doubleValue";

    private String stringValue = "something";
    private int intValue = 3;
    private double doubleValue = 0.9;
    private Collection<String> collectionValue = new ArrayList<>(Arrays.asList("firstString", "secondString"));
    private CustomType customValue = new CustomType(Color.RED, "red");

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

    public Collection<String> getCollectionValue() {
	return collectionValue;
    }

    public void setCollectionValue(Collection<String> collectionValue) {
	this.collectionValue = collectionValue;
    }

    public CustomType getCustomValue() {
	return customValue;
    }

    public void setCustomValue(CustomType customValue) {
	this.customValue = customValue;
    }

    @Override
    public String getTitle() {
	return stringValue;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[stringValue=" + stringValue + ", intValue=" + intValue + ", doubleValue="
		+ doubleValue + ", collectionValue=" + collectionValue + ", customValue=" + customValue + "]";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((collectionValue == null) ? 0 : collectionValue.hashCode());
	result = prime * result + ((customValue == null) ? 0 : customValue.hashCode());
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
	if (collectionValue == null) {
	    if (other.collectionValue != null) {
		return false;
	    }
	} else if (!collectionValue.equals(other.collectionValue)) {
	    return false;
	}
	if (customValue == null) {
	    if (other.customValue != null) {
		return false;
	    }
	} else if (!customValue.equals(other.customValue)) {
	    return false;
	}
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

    public static class CustomType implements Serializable {
	private static final long serialVersionUID = 1L;

	public final Color color;
	public final String name;

	public CustomType(Color color, String name) {
	    super();
	    this.color = color;
	    this.name = name;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((color == null) ? 0 : color.hashCode());
	    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
	    CustomType other = (CustomType) obj;
	    if (color == null) {
		if (other.color != null) {
		    return false;
		}
	    } else if (!color.equals(other.color)) {
		return false;
	    }
	    if (name == null) {
		if (other.name != null) {
		    return false;
		}
	    } else if (!name.equals(other.name)) {
		return false;
	    }
	    return true;
	}

	@Override
	public String toString() {
	    return getClass().getSimpleName() + "[color=" + color + ", name=" + name + "]";
	}
    }

}