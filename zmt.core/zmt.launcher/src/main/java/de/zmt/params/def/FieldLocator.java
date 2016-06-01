package de.zmt.params.def;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Contains data to locate a field inside a class. Used to automate field
 * values.
 * 
 * @author mey
 *
 */
public class FieldLocator implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Class object that declares the field. */
    private final Class<?> declaringClass;

    /** Name of the field this definition belongs to. */
    private final String fieldName;

    /**
     * Title of the definition. Used if there are several instances of the same
     * class to discriminate between them.
     * 
     * @see ParamDefinition#getTitle()
     */
    private final String objectTitle;

    /** No-argument constructor needed for reading from XML. */
    @SuppressWarnings("unused")
    private FieldLocator() {
	declaringClass = null;
	fieldName = null;
	objectTitle = null;
    }
    public FieldLocator(Class<?> classContaining, String fieldName, String objectTitle) {
	this.declaringClass = classContaining;
	this.fieldName = fieldName;
	this.objectTitle = objectTitle;
    }

    public FieldLocator(Class<?> clazz, String fieldName) {
	this(clazz, fieldName, null);
    }

    public FieldLocator(Field field, String objectTitle) {
	this(field.getDeclaringClass(), field.getName(), objectTitle);
    }

    public FieldLocator(Field field) {
	this(field, null);
    }

    public Class<?> getDeclaringClass() {
	return declaringClass;
    }

    public String getFieldName() {
	return fieldName;
    }

    public String getObjectTitle() {
	return objectTitle;
    }

    @Override
    public String toString() {
	if (objectTitle == null) {
	    return declaringClass.getSimpleName() + "$" + fieldName;
	}
	return declaringClass.getSimpleName() + "(" + objectTitle + ")$" + fieldName;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
	result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
	result = prime * result + ((objectTitle == null) ? 0 : objectTitle.hashCode());
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
	FieldLocator other = (FieldLocator) obj;
	if (declaringClass == null) {
	    if (other.declaringClass != null) {
		return false;
	    }
	} else if (!declaringClass.equals(other.declaringClass)) {
	    return false;
	}
	if (fieldName == null) {
	    if (other.fieldName != null) {
		return false;
	    }
	} else if (!fieldName.equals(other.fieldName)) {
	    return false;
	}
	if (objectTitle == null) {
	    if (other.objectTitle != null) {
		return false;
	    }
	} else if (!objectTitle.equals(other.objectTitle)) {
	    return false;
	}
	return true;
    }
}