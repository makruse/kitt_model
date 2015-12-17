package sim.engine.output;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import sim.engine.params.def.ParamDefinition;
import sim.util.Propertied;
import sim.util.Properties;

/**
 * Implementation for the {@link Collector} interface, discriminating between
 * parameter definitions. Dumps headers with {@link ParamDefinition}'s title as
 * prefix, in order to separate data from different groups of simulation
 * objects.
 * <p>
 * Data is stored within an encapsulated map that is used directly for providing
 * {@link Properties}.
 * 
 * @author mey
 * 
 * @param <K>
 *            the agent class' {@link ParamDefinition}
 * @param <V>
 *            map value type
 */
public abstract class DefinitionSeparatedCollector<K extends ParamDefinition, V extends Collectable>
	implements Collector, Propertied, Serializable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefinitionSeparatedCollector.class.getName());
    private static final long serialVersionUID = 1L;

    /**
     * Separator in header between between definition title prefix and
     * collectable's header.
     */
    private static final String SEPARATOR = "$";

    private final Map<K, V> dataPerDefinition;
    private final int columnCount;
    private Collectable collectable = new MyCollectable();

    /**
     * Constructs a new {@code DefinitionSeparatedCollector}.
     * 
     * @param simObjectDefs
     *            the parameter definitions, each of them associated with a
     *            group of simulation objects
     */
    public DefinitionSeparatedCollector(Collection<? extends K> simObjectDefs) {
	// use linked hash map to maintain key insertion order
	dataPerDefinition = new LinkedHashMap<>();

	int columnCount = 0;
	for (K def : simObjectDefs) {
	    V collectable = createCollectable(def);
	    dataPerDefinition.put(def, collectable);
	    columnCount += collectable.getSize();
	}
	this.columnCount = columnCount;
    }

    protected V getData(K definition) {
	return dataPerDefinition.get(definition);
    }

    /**
     * 
     * @param definition
     * @return {@link Collectable} of type {@code K} that will be associated
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
    public Collectable getCollectable() {
	return collectable;
    }

    @Override
    public Properties properties() {
	return Properties.getProperties(dataPerDefinition);
    }

    @Override
    public String toString() {
	return dataPerDefinition.toString();
    }

    private class MyCollectable implements Collectable {
	private static final long serialVersionUID = 1L;

	@Override
	public void clear() {
	    for (Collectable data : dataPerDefinition.values()) {
		data.clear();
	    }
	}

	/**
	 * Obtain headers from collectables with the definition's title as
	 * prefix.
	 */
	@Override
	public Collection<String> obtainHeaders() {
	    Collection<String> headers = new ArrayList<>(columnCount);
	    for (K key : dataPerDefinition.keySet()) {
		for (String header : dataPerDefinition.get(key).obtainHeaders()) {
		    headers.add(key.getTitle() + SEPARATOR + header);
		}
	    }

	    return headers;
	}

	@Override
	public Collection<?> obtainData() {
	    Collection<Object> data = new ArrayList<>(columnCount);
	    for (K key : dataPerDefinition.keySet()) {
		data.addAll(dataPerDefinition.get(key).obtainData());
	    }

	    return data;
	}

	@Override
	public int getSize() {
	    return columnCount;
	}

    }
}