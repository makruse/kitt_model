package de.zmt.output.message;

import de.zmt.output.Collector;
import sim.engine.SimState;

/**
 * Interface to create an {@link BeforeMessage} for a {@link Collector}.
 * 
 * @author mey
 *
 */
public interface CreatesBeforeMessage {
    /**
     * Creates a {@link BeforeMessage} sent to this collector when at the start
     * of the data collection cycle.
     * 
     * @param state
     * @param defaultMessage
     *            the default messages replaced by calling this method
     * @return {@link BeforeMessage}
     */
    BeforeMessage createBeforeMessage(SimState state, BeforeMessage defaultMessage);
}
