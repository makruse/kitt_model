package test.resources;

import java.util.*;

import javax.xml.bind.annotation.XmlRootElement;

import de.zmt.sim.engine.params.Params;
import de.zmt.sim.engine.params.def.*;

@XmlRootElement(name = "params", namespace = "http://www.zmt-bremen.de/")
@SuppressWarnings({ "unused", "serial" })
public class SimpleParams implements Params {
    private SimpleDefinition simpleDefinition = new SimpleDefinition();

    public SimpleDefinition getDefinition() {
	return simpleDefinition;
    }

    public void setDefinition(SimpleDefinition simpleDefinition) {
	this.simpleDefinition = simpleDefinition;
    }

    @Override
    public Collection<ParamDefinition> getDefinitions() {
	return Arrays.asList((ParamDefinition) simpleDefinition);
    }

    @Override
    public boolean removeOptionalDefinition(OptionalParamDefinition optionalDef) {
	return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ParamDefinition> Collection<T> getDefinitions(
	    Class<T> type) {
	if (type == SimpleDefinition.class) {
	    return (Collection<T>) Collections.singleton(simpleDefinition);
	} else {
	    throw new IllegalArgumentException(type
		    + " is invalid. Valid type : " + SimpleDefinition.class);
	}
    }

    @Override
    public String toString() {
	return "Params [definition=" + simpleDefinition + "]";
    }
}