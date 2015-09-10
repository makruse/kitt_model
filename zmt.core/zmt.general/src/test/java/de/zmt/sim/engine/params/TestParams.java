package de.zmt.sim.engine.params;

import java.util.*;

import javax.xml.bind.annotation.XmlRootElement;

import de.zmt.sim.engine.params.def.*;

@XmlRootElement(name = "params", namespace = "http://www.zmt-bremen.de/")
@SuppressWarnings({ "unused", "serial" })
public class TestParams implements SimParams {
    private TestDefinition testDefinition = new TestDefinition();

    public TestDefinition getDefinition() {
	return testDefinition;
    }

    public void setDefinition(TestDefinition testDefinition) {
	this.testDefinition = testDefinition;
    }

    @Override
    public Collection<ParamDefinition> getDefinitions() {
	return Arrays.asList((ParamDefinition) testDefinition);
    }

    @Override
    public String toString() {
	return "TestParams [definition=" + testDefinition + "]";
    }

    @Override
    public boolean removeOptionalDefinition(OptionalParamDefinition optionalDef) {
	return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ParamDefinition> Collection<T> getDefinitions(
	    Class<T> type) {
	if (type == TestDefinition.class) {
	    return (Collection<T>) Collections.singleton(testDefinition);
	} else {
	    throw new IllegalArgumentException(type
		    + " is invalid. Valid type : " + TestDefinition.class);
	}
    }

    @Override
    public long getSeed() {
	return 0;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ ((testDefinition == null) ? 0 : testDefinition.hashCode());
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
	TestParams other = (TestParams) obj;
	if (testDefinition == null) {
	    if (other.testDefinition != null) {
		return false;
	    }
	} else if (!testDefinition.equals(other.testDefinition)) {
	    return false;
	}
	return true;
    }

}