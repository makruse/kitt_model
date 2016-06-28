package de.zmt.params.def;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.zmt.params.ParamDefinition;

/**
 * Abstract implementation for locating a parameter value that can be automated
 * inside a {@link ParamDefinition}.
 * 
 * @author mey
 *
 */
@XStreamAlias("Locator")
public class Locator implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Class that contains the data to be automated. */
    private final Class<? extends ParamDefinition> targetClass;
    /**
     * Title of the object. Used <strong>only</strong> if there are several
     * instances of the same class to discriminate between them. Not considered
     * in {@link #equals(Object)} and {@link #hashCode()} methods.
     * 
     * @see ParamDefinition#getTitle()
     */
    private final String objectTitle;
    /** Object to identify the data within the target class. */
    private final Object identifier;

    /** No-argument constructor needed for reading from XML. */
    @SuppressWarnings("unused")
    private Locator() {
	this(null, null, null);
    }

    public Locator(Class<? extends ParamDefinition> targetClass, String objectTitle, Object identifier) {
	super();
	this.targetClass = targetClass;
	this.identifier = identifier;
	this.objectTitle = objectTitle;
    }

    public Locator(Class<? extends ParamDefinition> targetClass, Object identifier) {
	this(targetClass, null, identifier);
    }

    public Locator(ParamDefinition definition, Object identifier) {
	this(definition.getClass(), definition.getTitle(), identifier);
    }

    /**
     * Returns the class that contains the data to be automated.
     *
     * @return the class that contains the data to be automated
     */
    public Class<? extends ParamDefinition> getTargetClass() {
	return targetClass;
    }

    /**
     * Returns the title of the object. Used <strong>only</strong> if there are
     * several instances of the same class to discriminate between them. Not
     * considered in {@link #equals(Object)} and {@link #hashCode()} methods.
     *
     * @return the title of the object
     */
    public String getObjectTitle() {
	return objectTitle;
    }

    /**
     * Returns the object to identify the data within the target class.
     * 
     * @return the data identifier
     */
    public Object getIdentifier() {
	return identifier;
    }

    @Override
    public String toString() {
	if (objectTitle == null) {
	    return targetClass.getSimpleName() + "$" + identifier;
	}
	return targetClass.getSimpleName() + "(" + objectTitle + ")$" + identifier;
    }

    /**
     * Final implementation taking {@link #getTargetClass()} and
     * {@link #getIdentifier()} into account. {@link #getObjectTitle()} is
     * omitted because it is optional.
     */
    @Override
    public final int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((targetClass == null) ? 0 : targetClass.hashCode());
	result = prime * result + ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
	return result;
    }

    /**
     * Final implementation taking {@link #getTargetClass()} and
     * {@link #getIdentifier()} into account. Classes do not need to match and
     * {@link #getObjectTitle()} is omitted because it is optional.
     */
    @Override
    public final boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	// given object must be a locator but not the same class
	if (!Locator.class.isAssignableFrom(obj.getClass())) {
	    return false;
	}
	Locator other = (Locator) obj;
	if (targetClass == null) {
	    if (other.targetClass != null) {
		return false;
	    }
	} else if (!targetClass.equals(other.targetClass)) {
	    return false;
	}
	if (getIdentifier() == null) {
	    if (other.getIdentifier() != null) {
		return false;
	    }
	} else if (!getIdentifier().equals(other.getIdentifier())) {
	    return false;
	}
	return true;
    }

}