package de.zmt.sim.engine.output;

import de.zmt.sim.engine.ParamAgent;

public interface Collector extends Collectable {
    /**
     * Called before collecting data from agents.
     * 
     * @param message
     */
    void beforeCollect(BeforeMessage message);

    /**
     * Collect data from an {@link Agent}.
     * 
     * @param message
     */
    void collect(CollectMessage message);

    /**
     * Called after collecting data from agents.
     * 
     * @param message
     */
    void afterCollect(AfterMessage message);

    /** Tagging interface for messages sent before collection. */
    public static interface BeforeMessage {

    }

    /** Tagging interface for messages sent during collection. */
    public static interface CollectMessage {
	/** Get agent data is collected from */
	ParamAgent getAgent();
    }

    /** Tagging interface for messages sent after collection. */
    public static interface AfterMessage {

    }
}