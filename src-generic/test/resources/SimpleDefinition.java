package test.resources;

import de.zmt.sim.engine.params.def.AutoDefinition.NotAutomatable;
import de.zmt.sim.engine.params.def.*;

@SuppressWarnings({ "serial" })
public class SimpleDefinition implements ParamDefinition {
    @NotAutomatable
    private String stringValue = "something";
    private float floatValue;
    private int intValue;
    private double doubleValue;
    private long longValue;

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
	return "Definition [stringValue=" + stringValue + ", floatValue="
		+ floatValue + ", intValue=" + intValue + ", doubleValue="
		+ doubleValue + ", longValue=" + longValue + "]";
    }
}