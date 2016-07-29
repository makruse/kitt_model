package de.zmt.output;

import de.zmt.output.collectable.Collectable;
import de.zmt.output.message.CollectMessageFactory;
import de.zmt.output.strategy.CollectStrategy;
import de.zmt.output.strategy.MessageCollectStrategy;
import sim.engine.SimState;

/**
 * Abstract base implementation of {@link CollectStrategy} clearing the
 * collectable before further processing with {@link AgentCollectMessage}
 * objects.
 * 
 * @author mey
 *
 * @param <ColT>
 *            the type of {@link Collectable}
 */
abstract class ClearingBeforeStrategy<ColT extends Collectable<?>>
        extends MessageCollectStrategy<ColT, AgentCollectMessage> {
    private static final long serialVersionUID = 1L;

    @Override
    protected CollectMessageFactory<AgentCollectMessage> getCollectMessageFactory() {
        return AgentCollectMessage.FACTORY;
    }

    @Override
    public void process(SimState state, ColT collectable) {
        collectable.clear();
        super.process(state, collectable);
    }

}
