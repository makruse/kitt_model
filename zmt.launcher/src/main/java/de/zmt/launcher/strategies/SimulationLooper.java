package de.zmt.launcher.strategies;

import de.zmt.sim.engine.ZmtSimState;
import de.zmt.sim.engine.params.SimParams;

public interface SimulationLooper extends LauncherStrategy {
    /**
     * Performs a single simulation run for the given time.
     * 
     * @param simState
     * @param simTime
     */
    void loop(ZmtSimState simState, double simTime);

    /**
     * Performs a simulation run for every parameter object.
     * 
     * @param simClass
     *            class used in simulation runs
     * @param simParamsObjects
     *            parameters the combinations have been applied to
     * @param maxThreads
     *            maximum threads used for running simulations in parallel
     * @param simTime
     *            simulation time that needs to pass after a simulation is
     *            stopped
     */
    void loop(Class<? extends ZmtSimState> simClass,
	    Iterable<? extends SimParams> simParamsObjects, int maxThreads, double simTime);

}
