package sim.engine.output;

import java.util.Collection;

import sim.engine.output.message.CollectMessage;

public class TestCollector extends AbstractCollector<Collectable> {
    private static final long serialVersionUID = 1L;


    public TestCollector(Collectable collectable) {
        super(collectable);
    }

    public TestCollector(Collection<String> headers, Collection<?> values) {
        super(new TestCollectable(headers, values));
    }

    public TestCollector(String header, Object value) {
	super(new TestCollectable(header, value));
    }
    
    @Override
    public void collect(CollectMessage message) {
    }

}