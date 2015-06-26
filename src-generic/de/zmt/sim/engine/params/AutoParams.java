package de.zmt.sim.engine.params;

import java.io.Serializable;
import java.util.*;

import javax.xml.bind.annotation.*;

import de.zmt.sim.engine.params.def.AutoDefinition;

/**
 * Parameters for the automation of simulation runs with varying parameters.
 * 
 * @author cmeyer
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AutoParams implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_FILENAME = "automation.xml";

    /** Maximal number of threads running concurrently. */
    private final int maxThreads = 1;
    /** Duration of one simulation run in simulation time. */
    private final double simTime = 1000;

    /** Definitions providing values for automation. */
    @XmlElementWrapper
    @XmlElement(name = "definition")
    private final Collection<AutoDefinition> autoDefinitions = new ArrayList<>();

    public boolean addDefinition(AutoDefinition definition) {
	return autoDefinitions.add(definition);
    }

    public int getMaxThreads() {
	return maxThreads;
    }

    public double getSimTime() {
	return simTime;
    }

    public Collection<AutoDefinition> getDefinitions() {
	return autoDefinitions;
    }

    public boolean removeDefinition(AutoDefinition autoDef) {
	return autoDefinitions.remove(autoDef);
    }
}
