package de.zmt.output.message;

/**
 * Simple implementation for {@link CollectMessage} containing a simulation
 * object.
 * 
 * @author mey
 * @param <T>
 *            type of simulation object
 *
 */
public class SimpleCollectMessage<T> implements CollectMessage {
    private final T simObject;

    public SimpleCollectMessage(T simObject) {
	super();
	this.simObject = simObject;
    }

    public T getSimObject() {
	return simObject;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[simObject=" + simObject + "]";
    }
}
