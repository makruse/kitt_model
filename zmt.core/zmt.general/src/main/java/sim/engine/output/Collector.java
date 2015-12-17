package sim.engine.output;

import java.io.Serializable;

/**
 * A data collector that collects data from simulation objects to be further
 * processed in {@link Output}.
 * 
 * @author mey
 *
 */
public interface Collector extends Serializable {
    /**
     * Called <b>once</b> before collecting data from simulation objects.
     * 
     * @param message
     */
    void beforeCollect(BeforeMessage message);

    /**
     * Collect data from a simulation object. Called for <b>each</b> simulation
     * object.
     * 
     * @param message
     */
    void collect(CollectMessage message);

    /**
     * Called <b>once</b> after collecting data from simulation objects.
     * 
     * @param message
     */
    void afterCollect(AfterMessage message);

    /**
     * 
     * @return the {@link Collectable} processed by this {@code Collector}
     */
    Collectable getCollectable();

    /** Tagging interface for messages sent before collection. */
    public static interface BeforeMessage {

    }

    /**
     * Messages sent during collection to pass necessary data onto the
     * Collector.
     * 
     */
    public static interface CollectMessage {
	/** @return simulation object from which data is collected from */
	Object getSimObject();
    }

    /** Tagging interface for messages sent after collection. */
    public static interface AfterMessage {
	long getSteps();
    }
}