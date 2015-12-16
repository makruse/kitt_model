package sim.engine.output;

import java.io.Serializable;
import java.util.*;

import sim.util.Propertied;
import sim.util.Properties;

/**
 * An abstract implementation of {@link Collectable} storing data in a
 * {@link List}. {@link Propertied} for the data to be displayed in MASON GUI.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of data to be aggregated
 */
public abstract class AbstractCollectable<T> implements Propertied, Serializable, Collectable {
    private static final long serialVersionUID = 1L;

    protected final List<T> data;

    public AbstractCollectable(List<T> data) {
	this.data = data;
    }

    /**
     * @return value {@code data} is to be filled when calling {@link #clear()}
     */
    protected abstract T obtainInitialValue();

    @Override
    public abstract List<String> obtainHeaders();

    @Override
    public Collection<T> obtainData() {
	return Collections.unmodifiableCollection(data);
    }

    @Override
    public void clear() {
	Collections.fill(data, obtainInitialValue());
    }

    @Override
    public int getColumnCount() {
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
