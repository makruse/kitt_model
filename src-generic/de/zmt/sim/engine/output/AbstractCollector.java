package de.zmt.sim.engine.output;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import sim.util.*;
import sim.util.Properties;
import de.zmt.sim.engine.params.def.ParameterDefinition;

/**
 * Abstract base class for {@link Collector} interface. Dumps headers with
 * {@link ParameterDefinition}'s title as prefix, in order to separate data from
 * different agent classes.
 * <p>
 * 
 * Data is stored within an encapsulated map that is used directly for providing
 * {@link Properties}.
 * 
 * @author cmeyer
 * 
 * @param <K>
 *            the agent class' {@link ParameterDefinition}
 * @param <V>
 *            map value type
 */
public abstract class AbstractCollector<K extends ParameterDefinition, V extends Collectable>
	implements Collector, Propertied, Serializable {
    private static final String AGENT_CLASS_SEPERATOR = "$";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(AbstractCollector.class.getName());
    private static final long serialVersionUID = 1L;

    protected final Map<K, V> map;

    public AbstractCollector(Collection<? extends K> agentClassDefs) {
	// use linked hash map to maintain key insertion order
	map = new LinkedHashMap<K, V>();

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
    public void beforeCollect(BeforeMessage message) {
    }

    @Override
    public void afterCollect(AfterMessage message) {
    }

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
		headers.add(key.getTitle() + AGENT_CLASS_SEPERATOR + header);
	    }
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

    @Override
    public Properties properties() {
	return Properties.getProperties(map);
    }

    @Override
    public String toString() {
	return map.toString();
    }

}