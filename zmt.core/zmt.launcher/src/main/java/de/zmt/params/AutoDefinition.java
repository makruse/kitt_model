package de.zmt.params;

import java.util.ArrayList;
import java.util.Collection;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.zmt.params.BaseParamDefinition;
import de.zmt.params.accessor.Locator;
import sim.portrayal.inspector.ParamsInspector.InspectorRemovable;

/**
 * Represents a parameter of the model parameters that will be changed in a
 * series of simulation runs.
 * 
 * @author mey
 * @see AutoParams
 */
@XStreamAlias("AutoDefinition")
@InspectorRemovable
public class AutoDefinition extends BaseParamDefinition {
    private static final long serialVersionUID = 1L;

    /** Locator for the automated field */
    private final Locator locator;
    /** Values for automating the field */
    @XStreamImplicit
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
    public AutoDefinition(Locator locator) {
	this.locator = locator;
    }

    /**
     * Constructs a new {@code AutoDefinition} with given {@code locator} and
     * {@code values}.
     * 
     * @param locator
     * @param values
     */
    public AutoDefinition(Locator locator, Collection<?> values) {
	this.locator = locator;
	this.values.addAll(values);
    }

    public Locator getLocator() {
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