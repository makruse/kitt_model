package de.zmt.output.message;

import sim.engine.SimState;

/**
 * Default implementation for {@link CollectMessage} containing the simulation
 * state.
 * 
 * @author mey
 *
 */
public class DefaultCollectMessage implements CollectMessage {
    private final SimState state;

    public DefaultCollectMessage(SimState state) {
	super();
	this.state = state;
    }

    public SimState getState() {
	return state;
    }

    @Override
    public String toString() {
	return CollectMessage.class.getSimpleName() + "[state=" + state + "]";
    }

}
