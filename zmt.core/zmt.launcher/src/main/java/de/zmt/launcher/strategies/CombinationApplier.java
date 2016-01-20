package de.zmt.launcher.strategies;

import sim.engine.params.SimParams;

public interface CombinationApplier extends LauncherStrategy {

    /**
     * Applies combinations to a default parameter object.
     * 
     * @param combinations
     * @param defaultSimParams
     * @return {@link Iterable} of applied combinations, each of them with their
     *         resulting parameter object
     */
    Iterable<AppliedCombination> applyCombinations(Iterable<Combination> combinations, SimParams defaultSimParams);

    /**
     * Struct-like class providing a {@link Combination} and the
     * {@link SimParams} resulted from applying that combination.
     * 
     * @author mey
     *
     */
    public static final class AppliedCombination {
	public final Combination combination;
	public final SimParams result;

	public AppliedCombination(Combination combination, SimParams result) {
	    super();
	    this.combination = combination;
	    this.result = result;
	}
    }
}