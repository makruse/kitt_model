package de.zmt.launcher.strategies;

import java.nio.file.Path;

import de.zmt.launcher.strategies.CombinationApplier.AppliedCombination;
import sim.engine.ZmtSimState;

public interface SimulationLooper extends LauncherStrategy {
    /**
     * Performs a single simulation run for the given time.
     * 
     * @param simState
     * @param simTime
     *            simulation time that needs to pass after a simulation is
     *            stopped
     * @param printStatusInterval
     *            the step interval in which status messages are printed (&lt;1
     *            to disable)
     */
    void loop(ZmtSimState simState, double simTime, int printStatusInterval);

    /**
     * Performs a simulation run for every parameter object.
     * 
     * @param simClass
     *            class used in simulation runs
     * @param appliedCombinations
     *            parameter objects and their applied combinations
     * @param maxThreads
     *            maximum threads used for running simulations in parallel
     * @param simTime
     *            simulation time that needs to pass after a simulation is
     *            stopped
     * @param printStatusInterval
     *            the step interval in which status messages are printed (&lt;1
     *            to disable)
     * @param combinationInFolderNames
     *            use combination to generate inner folder names
     * @param outputPaths
     *            an iterable of output paths that is iterated for every new
     *            simulation run
     */
    void loop(Class<? extends ZmtSimState> simClass, Iterable<AppliedCombination> appliedCombinations, int maxThreads,
	    double simTime, int printStatusInterval, boolean combinationInFolderNames, Iterable<Path> outputPaths);

}
