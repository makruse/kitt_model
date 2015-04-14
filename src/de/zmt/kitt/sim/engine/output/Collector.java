package de.zmt.kitt.sim.engine.output;

import java.io.Serializable;

import de.zmt.kitt.sim.engine.agent.Agent;

public interface Collector extends Serializable, Clearable {
    /**
     * Collects data from {@code agent}.
     * 
     * @param agent
     * @param message
     *            optional message
     */
    void collect(Agent agent, Object message);
}