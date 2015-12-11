package de.zmt.launcher.strategies;

import java.util.*;
import java.util.logging.Logger;

import sim.engine.params.def.*;

class DefaultCombinationCompiler implements CombinationCompiler {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefaultCombinationCompiler.class.getName());

    /**
     * Compiles combinations containing all automated values from automation
     * parameters.
     */
    @Override
    public Iterable<Combination> compileCombinations(Iterable<AutoDefinition> autoDefinitions) {
	// map of field locators pointing to their set of automation values
	Map<FieldLocator, Collection<Object>> valuesPerParam = new HashMap<>();

	// iterate through all autoParams and collect values
	for (AutoDefinition autoDef : autoDefinitions) {
	    Collection<Object> paramValues = autoDef.getValues();
	    valuesPerParam.put(autoDef.getLocator(), paramValues);
	}

	if (valuesPerParam.isEmpty()) {
	    return Collections.emptySet();
	}

	// compute all combinations
	final Collection<Map<FieldLocator, Object>> rawCombinations = combineRecursive(valuesPerParam);
	return new Iterable<CombinationCompiler.Combination>() {

	    @Override
	    public Iterator<Combination> iterator() {
		return new WrappingIterator(rawCombinations.iterator());
	    }
	};
    }

    /**
     * Helper method generating parameters.
     * 
     * @see #combineRecursive(Map, Map, Queue, Collection)
     * @param collections
     * @return resulting combinations
     */
    private static <K, V> Collection<Map<K, V>> combineRecursive(Map<K, Collection<V>> collections) {
	return combineRecursive(collections, new HashMap<K, V>(), new ArrayDeque<>(collections.keySet()),
		new ArrayList<Map<K, V>>());
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
     * @param result
     * @return result
     */
    private static <K, V> Collection<Map<K, V>> combineRecursive(Map<K, Collection<V>> collections, Map<K, V> item,
	    Queue<K> remainingKeys, Collection<Map<K, V>> result) {
	// leaf: combination done, add it to result
	if (remainingKeys.isEmpty()) {
	    result.add(new HashMap<>(item));
	    return result;
	}

	// key queue decreases in size for every ongoing recursion
	K key = remainingKeys.poll();
	for (V value : collections.get(key)) {
	    item.put(key, value);
	    combineRecursive(collections, item, new ArrayDeque<>(remainingKeys), result);
	    // go one level up the tree and keep the elements before
	    item.remove(key);
	}

	return result;
    }

    /**
     * Wraps raw combinations into more convenient
     * {@link de.zmt.launcher.strategies.CombinationCompiler.Combination}s while
     * iterating.
     * 
     * @author mey
     *
     */
    private static final class WrappingIterator implements Iterator<CombinationCompiler.Combination> {
	private final Iterator<Map<FieldLocator, Object>> rawIterator;

	public WrappingIterator(Iterator<Map<FieldLocator, Object>> rawIterator) {
	    super();
	    this.rawIterator = rawIterator;
	}

	@Override
	public boolean hasNext() {
	    return rawIterator.hasNext();
	}

	@Override
	public Combination next() {
	    return new Combination(rawIterator.next());
	}

    }
}
