package de.zmt.kitt.sim.engine.output;

import java.util.*;

import de.zmt.sim.engine.params.def.ParameterDefinition;

/**
 * Abstract base class for {@link Collector} interface. Dumps headers with
 * {@link ParameterDefinition}'s title as prefix, in order to separate data from
 * different agent classes.
 * 
 * @author cmeyer
 * 
 * @param <K>
 *            the agent class' {@link ParameterDefinition}
 * @param <V>
 *            map value type
 */
public abstract class AbstractCollector<K extends ParameterDefinition, V extends Collectable>
	extends EncapsulatedMap<K, V> implements Collector {
    private static final long serialVersionUID = 1L;

    public AbstractCollector(Collection<? extends K> agentClassDefs) {
	for (K def : agentClassDefs) {
	    map.put(def, createCollectable(def));
	}
    }

    /**
     * 
     * @param definition
     * @return {@link Collectable} of type {@code K } that will be associated
     *         with {@code definition}.
     */
    protected abstract V createCollectable(K definition);

    @Override
    public void clear() {
	for (Collectable data : map.values()) {
	    data.clear();
	}
    }

    /** Obtain headers from collectables with the definition's title as prefix. */
    @Override
    public Collection<String> obtainHeaders() {
	Collection<String> headers = obtainColumnList();
	for (K key : map.keySet()) {
	    for (String header : map.get(key).obtainHeaders()) {
		headers.add(key.getTitle() + "_" + header);
	    }

	    headers.addAll(map.get(key).obtainHeaders());
	}

	return headers;
    }

    @Override
    public Collection<?> obtainData() {
	Collection<Object> data = obtainColumnList();
	for (K key : map.keySet()) {
	    data.addAll(map.get(key).obtainData());
	}

	return data;
    }

    /** @return {@link ArrayList} with column count as initial capacity. */
    private <T> ArrayList<T> obtainColumnList() {
	return new ArrayList<T>(getColumnCount());
    }

    /** @return total column count of this collector */
    protected abstract int getColumnCount();

}