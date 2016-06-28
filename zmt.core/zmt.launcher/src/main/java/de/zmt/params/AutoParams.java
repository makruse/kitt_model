package de.zmt.params;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.logging.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.zmt.params.def.AutoDefinition;
import de.zmt.params.def.Locator;
import de.zmt.util.ParamsUtil;

/**
 * Parameters for the automation of simulation runs with varying parameters.
 * 
 * @author mey
 */
@XStreamAlias("AutoParams")
public class AutoParams extends BaseParamsNode {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AutoParams.class.getName());

    public static final String DEFAULT_FILENAME = "autoParams.xml";

    static {
        ParamsUtil.getXStreamInstance().processAnnotations(AutoParams.class);
    }

    /** Duration of one simulation run in simulation time. */
    private double simTime = 1000;

    /** Definitions providing values for automation. */
    @XStreamImplicit
    private final Collection<AutoDefinition> autoDefinitions = new ArrayList<>();

    /**
     * Creates an {@link AutoParams} object containing an {@link AutoDefinition}
     * for every automatable field in the given object's definitions. Each
     * {@link Locator} points to the associated definition's field and the
     * definition's value is used as the only automation value.
     * <p>
     * The resulting object can use as a starting point for creating a
     * customized automation.
     * 
     * @param definition
     *            the definition to be used
     * @return automation parameters from given definition
     */
    public static AutoParams fromParams(ParamDefinition definition) {
        return fromParams(definition, new ArrayDeque<>());
    }

    private static AutoParams fromParams(ParamDefinition definition, Deque<Object> identifiers) {
        AutoParams autoParams = new AutoParams();
        for (Object identifier : definition.accessor().identifiers()) {
            Locator locator = new Locator(definition, identifier);
            Object value = definition.accessor().get(identifier);

            // skip if the value is a definition
            if (value instanceof ParamDefinition) {
                continue;
            }
            autoParams.addDefinition(new AutoDefinition(locator, Collections.singleton(value)));
        }

        // if definition contains other definitions: add recursively
        if (definition instanceof ParamsNode) {
            for (ParamDefinition containedDefinition : ((ParamsNode) definition).getDefinitions()) {
                autoParams.autoDefinitions.addAll(fromParams(containedDefinition, identifiers).getDefinitions());
            }
        }

        return autoParams;
    }

    public boolean addDefinition(AutoDefinition definition) {
        return autoDefinitions.add(definition);
    }

    public double getSimTime() {
        return simTime;
    }

    public void setSimTime(double simTime) {
        this.simTime = simTime;
    }

    public boolean removeDefinition(AutoDefinition autoDef) {
        return autoDefinitions.remove(autoDef);
    }

    @Override
    public Collection<AutoDefinition> getDefinitions() {
        return autoDefinitions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((autoDefinitions == null) ? 0 : autoDefinitions.hashCode());
        long temp;
        temp = Double.doubleToLongBits(simTime);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AutoParams other = (AutoParams) obj;
        if (autoDefinitions == null) {
            if (other.autoDefinitions != null) {
                return false;
            }
        } else if (!autoDefinitions.equals(other.autoDefinitions)) {
            return false;
        }
        if (Double.doubleToLongBits(simTime) != Double.doubleToLongBits(other.simTime)) {
            return false;
        }
        return true;
    }
}
