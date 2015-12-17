package sim.engine.output;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import sim.util.Propertied;
import sim.util.Properties;

/**
 * Implementation for the {@link Collector} interface, discriminating between
 * categories. Dumps headers with category {@link #toString()} as prefix, in
 * order to separate data from different groups of simulation objects.
 * <p>
 * Data is stored within an encapsulated map that is used directly for providing
 * {@link Properties}.
 * 
 * @author mey
 * 
 * @param <K>
 *            the category type
 * @param <V>
 *            the {@code Collectable} type stored for every category
 */
public abstract class CategoryCollector<K, V extends Collectable> implements Collector, Propertied, Serializable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CategoryCollector.class.getName());
    private static final long serialVersionUID = 1L;

    /**
     * Separator in header between between category prefix and collectable's
     * header.
     */
    private static final String SEPARATOR = "$";

    private final Map<K, V> collectablePerCategory;
    private final int columnCount;
    private Collectable collectable = new MyCollectable();

    /**
     * Constructs a new {@code DefinitionSeparatedCollector}. Each given
     * category will be associated with a {@code Collectable}.
     * 
     * @see #createCollectable(Object)
     * @param categories
     *            the categories
     */
    public CategoryCollector(Collection<? extends K> categories) {
	// use linked hash map to maintain key insertion order
	collectablePerCategory = new LinkedHashMap<>();

	int columnCount = 0;
	for (K def : categories) {
	    V collectable = createCollectable(def);
	    collectablePerCategory.put(def, collectable);
	    columnCount += collectable.getSize();
	}
	this.columnCount = columnCount;
    }

    protected V getData(K category) {
	return collectablePerCategory.get(category);
    }

    /**
     * 
     * @param category
     * @return {@link Collectable} of type {@code K} that will be associated
     *         with {@code category}.
     */
    protected abstract V createCollectable(K category);

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
	return Properties.getProperties(collectablePerCategory);
    }

    @Override
    public String toString() {
	return collectablePerCategory.toString();
    }

    private class MyCollectable implements Collectable {
	private static final long serialVersionUID = 1L;

	@Override
	public void clear() {
	    for (Collectable data : collectablePerCategory.values()) {
		data.clear();
	    }
	}

	/** Obtain headers from collectables with the category's as prefix. */
	@Override
	public Collection<String> obtainHeaders() {
	    Collection<String> headers = new ArrayList<>(columnCount);
	    for (K key : collectablePerCategory.keySet()) {
		for (String header : collectablePerCategory.get(key).obtainHeaders()) {
		    headers.add(key + SEPARATOR + header);
		}
	    }

	    return headers;
	}

	@Override
	public Collection<?> obtainData() {
	    Collection<Object> data = new ArrayList<>(columnCount);
	    for (K key : collectablePerCategory.keySet()) {
		data.addAll(collectablePerCategory.get(key).obtainData());
	    }

	    return data;
	}

	@Override
	public int getSize() {
	    return columnCount;
	}

    }
}