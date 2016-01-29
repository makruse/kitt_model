package sim.engine.output;

import java.util.Collection;
import java.util.Collections;

public class TestCollectable<V> implements Collectable<V> {
    private static final long serialVersionUID = 1L;

    private final Collection<String> headers;
    private final Collection<V> values;

    public TestCollectable(Collection<String> headers, Collection<V> values) {
	super();
	this.headers = headers;
	this.values = values;
    }

    public TestCollectable(String header, V value) {
	this(Collections.singleton(header), Collections.singleton(value));
    }

    @Override
    public Iterable<V> obtainValues() {
        return values;
    }

    @Override
    public Iterable<String> obtainHeaders() {
        return headers;
    }

    @Override
    public int getSize() {
        return values.size();
    }
}