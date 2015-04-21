package de.zmt.kitt.sim.engine.output;

import de.zmt.kitt.sim.engine.agent.Agent;

public interface Collector extends Collectable {
    /**
     * Collects data from {@code agent}.
     * 
     * @param agent
     * @param message
     *            optional message
     */
    void collect(Agent agent, Object message);
}