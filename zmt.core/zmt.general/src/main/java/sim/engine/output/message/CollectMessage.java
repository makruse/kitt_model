package sim.engine.output.message;

/**
 * Messages sent during collection to pass necessary data onto the
 * Collector.
 * 
 */
public interface CollectMessage {
    /** @return simulation object from which data is collected from */
    Object getSimObject();
}