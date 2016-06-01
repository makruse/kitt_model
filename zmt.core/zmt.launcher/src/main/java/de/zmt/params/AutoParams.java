package de.zmt.params;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlTransient;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.zmt.params.def.AutoDefinition;
import de.zmt.params.def.FieldLocator;
import de.zmt.params.def.ParamDefinition;
import de.zmt.params.def.ParamDefinition.NotAutomatable;
import de.zmt.util.ParamsUtil;

/**
 * Parameters for the automation of simulation runs with varying parameters.
 * 
 * @author mey
 */
@XStreamAlias("AutoParams")
public class AutoParams extends BaseParams {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AutoParams.class.getName());

    public static final String DEFAULT_FILENAME = "automation.xml";

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
     * {@link FieldLocator} points to the associated definition's field and the
     * definition's value is used as the only automation value.
     * <p>
     * The resulting object can use as a starting point for creating a
     * customized automation.
     * 
     * @param params
     * @return automation parameters from {@code params}
     */
    public static AutoParams fromParams(Params params) {
	AutoParams autoParams = new AutoParams();
	for (ParamDefinition definition : params.getDefinitions()) {
	    for (Field field : definition.getClass().getDeclaredFields()) {
		// skip fields that should not be automated
		if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())
			|| field.getAnnotation(NotAutomatable.class) != null
			|| field.getAnnotation(XStreamOmitField.class) != null
			|| field.getAnnotation(XmlTransient.class) != null) {
		    continue;
		}

		FieldLocator fieldLocator = new FieldLocator(field, definition.getTitle());
		Object value;
		try {
		    field.setAccessible(true);
		    value = field.get(definition);
		} catch (IllegalAccessException e) {
		    logger.log(Level.WARNING, "Cannot access field " + field + " for object " + definition, e);
		    continue;
		}
		autoParams.addDefinition(new AutoDefinition(fieldLocator, Collections.singleton(value)));
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
