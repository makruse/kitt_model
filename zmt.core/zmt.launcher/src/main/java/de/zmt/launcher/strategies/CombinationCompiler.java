package de.zmt.launcher.strategies;

import java.util.Collection;

import de.zmt.params.def.AutoDefinition;

public interface CombinationCompiler extends LauncherStrategy {
    /**
     * Compiles definitions to a batch of combinations.
     * 
     * @param autoDefinitions
     *            automation definitions specifying fields of {@code params}
     *            that are automated
     * @return {@link Collection} of simulation combinations
     */
    Collection<Combination> compileCombinations(Iterable<AutoDefinition> autoDefinitions);
}
