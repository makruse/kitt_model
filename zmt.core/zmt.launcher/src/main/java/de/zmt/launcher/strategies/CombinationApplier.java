package de.zmt.launcher.strategies;

import de.zmt.launcher.strategies.CombinationCompiler.Combination;
import sim.engine.params.SimParams;

public interface CombinationApplier extends LauncherStrategy {

    /**
     * Applies combinations to a default parameter object.
     * 
     * @param combinations
     * @param defaultSimParams
     * @return {@link Iterable} of parameter objects, each of them with a
     *         combination applied
     */
    <T extends SimParams> Iterable<T> applyCombinations(Iterable<Combination> combinations, T defaultSimParams);
}