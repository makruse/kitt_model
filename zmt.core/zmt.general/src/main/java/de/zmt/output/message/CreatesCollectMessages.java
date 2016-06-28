package de.zmt.output.message;

import de.zmt.output.Collector;
import sim.engine.SimState;

/**
 * Interface to create {@link CollectMessage}s for a {@link Collector}.
 * 
 * @author mey
 *
 */
public interface CreatesCollectMessages {
    /**
     * Creates a {@link CollectMessage} iterable. Each created message will be
     * sent during collection between {@link BeforeMessage} and
     * {@link AfterMessage}.
     * 
     * @param state
     * @param defaultMessages
     *            the default messages which are replaced by calling this method
     * @return {@link CollectMessage} iterable
     */
    Iterable<? extends CollectMessage> createCollectMessages(SimState state,
            Iterable<? extends CollectMessage> defaultMessages);
}
