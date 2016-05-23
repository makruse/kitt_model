package de.zmt.output;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import sim.util.Propertied;
import sim.util.Properties;

/**
 * A simple implementation of {@link Collectable} storing data in a {@link List}
 * , which can be cleared and replaced by initial values. Implements
 * {@link Propertied} for the data to be displayed in MASON GUI.
 * 
 * @author mey
 *
 * @param <V>
 *            the type of data to be aggregated
 */
public abstract class AbstractCollectable<V> implements Propertied, Serializable, ClearableCollectable<V> {
    private static final long serialVersionUID = 1L;

    private final List<V> values;

    public AbstractCollectable(List<V> values) {
	this.values = values;
    }

    /**
     * Internal getter for the values contained in this collectable. Can be
     * overriden in subclasses to generate a custom list of values.
     * 
     * @return the data contained in this collectable
     */
    protected List<V> getValues() {
	return values;
    }

    /**
     * @return value {@code data} is to be filled when calling {@link #clear()},
     *         default is <code>null</code>
     */
    protected V obtainInitialValue() {
	return null;
    }

    // returns list in order to be accessed by index for properties
    @Override
    public abstract List<String> obtainHeaders();

    @Override
    public List<V> obtainValues() {
	return Collections.unmodifiableList(getValues());
    }

    @Override
    public void clear() {
	Collections.fill(getValues(), obtainInitialValue());
    }

    @Override
    public int getSize() {
	return obtainHeaders().size();
    }

    @Override
    public Properties properties() {
	return new MyProperties();
    }

    @Override
    public String toString() {
	return getValues().toString();
    }

    public class MyProperties extends Properties {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isVolatile() {
	    return false;
	}

	@Override
	public int numProperties() {
	    return getValues().size();
	}

	@Override
	public Object getValue(int index) {
	    return getValues().get(index);
	}

	@Override
	public boolean isReadWrite(int index) {
	    return false;
	}

	@Override
	public String getName(int index) {
	    return obtainHeaders().get(index);
	}

	@Override
	public Class<?> getType(int index) {
	    return getValues().get(index).getClass();
	}

	@Override
	protected Object _setValue(int index, Object value) {
	    throw new UnsupportedOperationException("read only");
	}

    }
}
