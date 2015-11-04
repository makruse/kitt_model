package sim.engine.params.def;

import java.util.Collection;

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
    private FieldLocator locator;
    /** Values for automating the field */
    @XmlElementWrapper
    @XmlElement(name = "value")
    private Collection<Object> values;

    public AutoDefinition() {

    }

    public AutoDefinition(FieldLocator locator, Collection<Object> values) {
	this.locator = locator;
	this.values = values;
    }

    public FieldLocator getLocator() {
	return locator;
    }

    public void setLocator(FieldLocator locator) {
	this.locator = locator;
    }

    public Collection<Object> getValues() {
	return values;
    }

    public void setValues(Collection<Object> values) {
	this.values = values;
    }

    @Override
    public String getTitle() {
	return locator.toString();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FieldLocator {
	/** Class object this definition belongs to. */
	private Class<? extends ParamDefinition> clazz;

	/** Name of the field this definition belongs to. */
	private String fieldName;

	/**
	 * Title of the definition. Used if there are several instances of the
	 * same class to discriminate between them.
	 * 
	 * @see ParamDefinition#getTitle()
	 */
	private String objectTitle;

	public FieldLocator() {

	}

	public FieldLocator(Class<? extends ParamDefinition> clazz, String fieldName, String objectTitle) {
	    this.clazz = clazz;
	    this.fieldName = fieldName;
	    this.objectTitle = objectTitle;
	}

	public FieldLocator(Class<? extends ParamDefinition> clazz, String fieldName) {
	    this(clazz, fieldName, null);
	}

	public Class<? extends ParamDefinition> getClazz() {
	    return clazz;
	}

	public void setClazz(Class<? extends ParamDefinition> clazz) {
	    this.clazz = clazz;
	}

	public String getFieldName() {
	    return fieldName;
	}

	public void setFieldName(String fieldName) {
	    this.fieldName = fieldName;
	}

	public String getObjectTitle() {
	    return objectTitle;
	}

	public void setObjectTitle(String objectTitle) {
	    this.objectTitle = objectTitle;
	}

	@Override
	public String toString() {
	    if (objectTitle == null) {
		return clazz + "$" + fieldName;
	    }
	    return clazz + "(" + objectTitle + ")$" + fieldName;
	}
    }

}