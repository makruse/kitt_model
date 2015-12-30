package sim.engine.output;

import java.io.Serializable;
import java.util.*;

import sim.util.Propertied;
import sim.util.Properties;

/**
 * A simple implementation of {@link Collectable} storing data in a {@link List}
 * , which can be cleared and replaced by initial values. Implements
 * {@link Propertied} for the data to be displayed in MASON GUI.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of data to be aggregated
 */
public abstract class AbstractCollectable<T> implements Propertied, Serializable, ClearableCollectable {
    private static final long serialVersionUID = 1L;

    private final List<T> data;

    public AbstractCollectable(List<T> data) {
	this.data = data;
    }

    /** @return the data contained in this collectable */
    protected final List<T> getValues() {
	return data;
    }

    /**
     * @return value {@code data} is to be filled when calling {@link #clear()},
     *         default is <code>null</code>
     */
    protected T obtainInitialValue() {
	return null;
    }

    // returns list in order to be accessed by index for properties
    @Override
    public abstract List<String> obtainHeaders();

    @Override
    public Iterable<T> obtainValues() {
	return Collections.unmodifiableCollection(data);
    }

    @Override
    public void clear() {
	Collections.fill(data, obtainInitialValue());
    }

    @Override
    public int getSize() {
	return data.size();
    }

    @Override
    public Properties properties() {
	return new MyProperties();
    }

    @Override
    public String toString() {
	return data.toString();
    }

    public class MyProperties extends Properties {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isVolatile() {
	    return false;
	}

	@Override
	public int numProperties() {
	    return data.size();
	}

	@Override
	public Object getValue(int index) {
	    return data.get(index);
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
	    return data.get(index).getClass();
	}

	@Override
	protected Object _setValue(int index, Object value) {
	    throw new UnsupportedOperationException("read only");
	}

    }
}
