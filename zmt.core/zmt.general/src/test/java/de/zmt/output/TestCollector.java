package de.zmt.output;

import java.util.Collection;

import de.zmt.output.message.CollectMessage;

public class TestCollector extends AbstractCollector<Collectable<?>> {
    private static final long serialVersionUID = 1L;

    public TestCollector(Collectable<?> collectable) {
        super(collectable);
    }

    public <T> TestCollector(Collection<String> headers, Collection<T> values) {
        super(new TestCollectable<>(headers, values));
    }

    public <T> TestCollector(String header, T value) {
        super(new TestCollectable<>(header, value));
    }

    @Override
    public void collect(CollectMessage message) {
    }

}