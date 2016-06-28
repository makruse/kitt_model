package de.zmt.launcher.strategies;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.zmt.params.AutoDefinition;
import de.zmt.params.accessor.Locator;

class DefaultCombinationCompiler implements CombinationCompiler {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefaultCombinationCompiler.class.getName());

    @Override
    public Collection<Combination> compileCombinations(Iterable<AutoDefinition> autoDefinitions) {
        // map of field locators pointing to their set of automation values
        Map<Locator, Collection<Object>> valuesPerParam = new LinkedHashMap<>();
        int expectedCombinationCount = 1;

        // iterate through all autoParams and collect values
        for (AutoDefinition autoDef : autoDefinitions) {
            Locator locator = autoDef.getLocator();
            Collection<Object> paramValues = autoDef.getValues();
            if (valuesPerParam.containsKey(locator)) {
                throw new IllegalArgumentException(
                        "Duplicate " + Locator.class.getSimpleName() + " not allowed in definitions: " + locator
                                + ".\nSeveral automation values for a field must be supplied inside a single "
                                + AutoDefinition.class.getSimpleName() + ".");
            }
            expectedCombinationCount *= paramValues.size();
            valuesPerParam.put(locator, paramValues);
        }

        if (valuesPerParam.isEmpty()) {
            logger.warning(autoDefinitions + "is empty. Cannot compile combinations.");
            return Collections.emptySet();
        }

        logger.info("Compiling " + expectedCombinationCount + " combinations.");

        // compute all combinations
        return combineRecursive(valuesPerParam).stream().map(Combination::new).collect(Collectors.toList());
    }

    /**
     * Helper method generating parameters.
     * 
     * @see #combineRecursive(Map, Map, Queue)
     * @param collections
     * @return resulting combinations
     */
    private static <K, V> Collection<Map<K, V>> combineRecursive(Map<K, Collection<V>> collections) {
        return combineRecursive(collections, new LinkedHashMap<K, V>(), new ArrayDeque<>(collections.keySet()));
    }

    /**
     * Computes all possible combinations between {@code collections}, resulting
     * in a collection of maps containing one mapping for each key to a value
     * from a collection. Use {@link #combineRecursive(Map)} as entry point.
     * 
     * @param collections
     *            map of keys pointing to a collection of values
     * @param item
     *            current item
     * @param remainingKeys
     *            keys remaining, recursion ends when empty
     * @return result
     */
    private static <K, V> Collection<Map<K, V>> combineRecursive(Map<K, Collection<V>> collections, Map<K, V> item,
            Queue<K> remainingKeys) {
        // leaf: combination done, return only item
        if (remainingKeys.isEmpty()) {
            return Collections.singleton(new LinkedHashMap<>(item));
        }

        Collection<Map<K, V>> result = new ArrayList<>();
        // key queue decreases in size for every ongoing recursion
        K key = remainingKeys.poll();
        for (V value : collections.get(key)) {
            item.put(key, value);
            result.addAll(combineRecursive(collections, item, new ArrayDeque<>(remainingKeys)));
            // go one level up the tree and keep the elements before
            item.remove(key);
        }

        return result;
    }
}
