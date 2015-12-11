package sim.engine.params.def;

import java.util.*;

import javax.xml.bind.annotation.*;

import sim.engine.params.AutoParams;

/**
 * Represents a parameter of the model parameters that will be changed in a
 * series of simulation runs.
 * 
 * @author mey
 * @see AutoParams
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AutoDefinition extends AbstractParamDefinition implements OptionalParamDefinition {
    private static final long serialVersionUID = 1L;

    /** Locator for the automated field */
    private final FieldLocator locator;
    /** Values for automating the field */
    @XmlElementWrapper
    @XmlElement(name = "value")
    private final Collection<Object> values = new ArrayList<>();

    /** No-argument constructor needed for reading from XML. */
    public AutoDefinition() {
	locator = null;
    }

    /**
     * Constructs a new {@code AutoDefinition} with given {@code locator} and an
     * empty collection of automation values.
     * 
     * @param locator
     */
    public AutoDefinition(FieldLocator locator) {
	this.locator = locator;
    }

    /**
     * Constructs a new {@code AutoDefinition} with given {@code locator} and
     * {@code values}.
     * 
     * @param locator
     * @param values
     */
    public AutoDefinition(FieldLocator locator, Collection<Object> values) {
	this.locator = locator;
	this.values.addAll(values);
    }

    public FieldLocator getLocator() {
	return locator;
    }

    public Collection<Object> getValues() {
	return values;
    }

    public boolean addValue(Object value) {
	return values.add(value);
    }

    @Override
    public String getTitle() {
	return locator.toString();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((locator == null) ? 0 : locator.hashCode());
	result = prime * result + ((values == null) ? 0 : values.hashCode());
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
	AutoDefinition other = (AutoDefinition) obj;
	if (locator == null) {
	    if (other.locator != null) {
		return false;
	    }
	} else if (!locator.equals(other.locator)) {
	    return false;
	}
	if (values == null) {
	    if (other.values != null) {
		return false;
	    }
	} else if (!values.equals(other.values)) {
	    return false;
	}
	return true;
    }

}