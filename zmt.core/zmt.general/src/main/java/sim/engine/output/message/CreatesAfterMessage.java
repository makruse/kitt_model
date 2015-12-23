package sim.engine.output.message;

import sim.engine.SimState;
import sim.engine.output.Collector;

/**
 * Interface to create an {@link AfterMessage} for a {@link Collector}.
 * 
 * @author mey
 *
 */
public interface CreatesAfterMessage {
    /**
     * Creates an {@link AfterMessage} sent to this collector at the end of the
     * data collection cycle.
     * 
     * @param state
     * @param defaultMessage
     *            the default messages replaced by calling this method
     * @return {@link AfterMessage}
     */
    AfterMessage createAfterMessage(SimState state, AfterMessage defaultMessage);
}
