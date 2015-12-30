package sim.engine.output;

import java.util.*;

public class TestCollectable implements Collectable {
    private static final long serialVersionUID = 1L;

    private final Collection<String> headers;
    private final Collection<?> values;

    public TestCollectable(Collection<String> headers, Collection<?> values) {
	super();
	this.headers = headers;
	this.values = values;
    }

    public TestCollectable(String header, Object value) {
	this(Collections.singleton(header), Collections.singleton(value));
    }

    @Override
    public Iterable<?> obtainValues() {
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