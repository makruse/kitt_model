package sim.engine.output;

import java.io.Serializable;

import sim.engine.output.message.*;

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
     * @see CreatesBeforeMessage
     * @param message
     */
    void beforeCollect(BeforeMessage message);

    /**
     * Collects data. Called for <b>each</b> created {@link CollectMessage}.
     * 
     * @see CreatesCollectMessages
     * @param message
     */
    void collect(CollectMessage message);

    /**
     * Called <b>once</b> after collecting data from simulation objects.
     * 
     * @see CreatesAfterMessage
     * @param message
     */
    void afterCollect(AfterMessage message);

    /**
     * 
     * @return the {@link Collectable} processed by this {@code Collector}
     */
    Collectable getCollectable();
}