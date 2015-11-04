package de.zmt.launcher.strategies;

import java.util.*;

import sim.engine.params.def.*;
import sim.engine.params.def.AutoDefinition.FieldLocator;

public interface CombinationCompiler extends LauncherStrategy {
    /**
     * Compiles definitions to a batch of combinations.
     * 
     * @param autoDefinitions
     *            automation definitions specifying fields of {@code params}
     *            that are automated
     * @return {@link Iterable} of simulation combinations
     */
    Iterable<Combination> compileCombinations(Iterable<AutoDefinition> autoDefinitions);

    /**
     * Wrapper for {@code Map<FieldLocator, Object>} to provide some type
     * safety.
     * 
     * @author mey
     *
     */
    static final class Combination {
	private final Map<FieldLocator, Object> combination;

	public Combination(Map<FieldLocator, Object> combination) {
	    this.combination = combination;
	}

	public Object get(Object key) {
	    return combination.get(key);
	}

	public Set<FieldLocator> keySet() {
	    return combination.keySet();
	}

	public Collection<Object> values() {
	    return combination.values();
	}

	public int size() {
	    return combination.size();
	}

	@Override
	public String toString() {
	    return combination.toString();
	}
    }
}
