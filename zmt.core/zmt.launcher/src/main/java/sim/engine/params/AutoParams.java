package sim.engine.params;

import java.util.*;

import javax.xml.bind.annotation.*;

import sim.engine.params.Params;
import sim.engine.params.def.AutoDefinition;

/**
 * Parameters for the automation of simulation runs with varying parameters.
 * 
 * @author mey
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AutoParams implements Params {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_FILENAME = "automation.xml";

    /** Maximal number of threads running concurrently. */
    private int maxThreads = 0;
    /** Duration of one simulation run in simulation time. */
    private double simTime = 1000;

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

    public void setMaxThreads(int maxThreads) {
	this.maxThreads = maxThreads;
    }

    public double getSimTime() {
	return simTime;
    }

    public void setSimTime(double simTime) {
	this.simTime = simTime;
    }

    @Override
    public Collection<AutoDefinition> getDefinitions() {
	return autoDefinitions;
    }

    public boolean removeDefinition(AutoDefinition autoDef) {
	return autoDefinitions.remove(autoDef);
    }
}
