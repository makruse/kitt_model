package sim.engine.output.message;

/**
 * Default implementation for {@link CollectMessage} containing a simulation
 * object.
 * 
 * @author mey
 * @param <T>
 *            type of simulation object
 *
 */
public class DefaultCollectMessage<T> implements CollectMessage {
    private final T simObject;

    public DefaultCollectMessage(T simObject) {
	super();
	this.simObject = simObject;
    }

    @Override
    public T getSimObject() {
	return simObject;
    }

}
