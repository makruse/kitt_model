package sim.engine.output;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import sim.util.Propertied;
import sim.util.Properties;

/**
 * Implementation for the {@link Collector} interface, discriminating between
 * categories. Dumps headers with category {@link #toString()} as prefix, in
 * order to separate data.
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
    /** Accumulated size of all collectables. */
    private final int totalSize;
    private Collectable mergingCollectable = new MergingCollectable();

    /**
     * Constructs a new {@code DefinitionSeparatedCollector}. Each given
     * category will be associated with a {@code Collectable}. The iteration
     * order of {@code categories} will be preserved.
     * 
     * @see #createCollectable(Object)
     * @param categories
     *            the categories
     */
    public CategoryCollector(Set<? extends K> categories) {
	// use linked hash map to maintain key insertion order
	collectablePerCategory = new LinkedHashMap<>();

	int totalSize = 0;
	for (K def : categories) {
	    V collectable = createCollectable(def);
	    collectablePerCategory.put(def, collectable);
	    totalSize += collectable.getSize();
	}
	this.totalSize = totalSize;
    }

    /** @return set of contained categories */
    protected final Set<K> getCategories() {
	return collectablePerCategory.keySet();
    }

    /**
     * @param category
     * @return the associated {@code Collectable}
     */
    protected final V getCollectable(K category) {
	return collectablePerCategory.get(category);
    }

    /**
     * Called when creating headers from categories and collectables. The
     * default is to use the category's {@link Object#toString()} method.
     * Subclasses can override this to change default behavior.
     * 
     * @param category
     * @return header prefix string from this category
     */
    protected String createCategoryHeaderPrefix(K category) {
	return category.toString();
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
	return mergingCollectable;
    }

    @Override
    public Properties properties() {
	return Properties.getProperties(collectablePerCategory);
    }

    @Override
    public String toString() {
	return collectablePerCategory.toString();
    }

    /**
     * {@code Collectable} that merges all the individual collectables from the
     * different categories.
     * 
     * @author mey
     *
     */
    private class MergingCollectable implements Collectable {
	private static final long serialVersionUID = 1L;

	/** Obtain headers from collectables with the category's as prefix. */
	@Override
	public Collection<String> obtainHeaders() {
	    Collection<String> headers = new ArrayList<>(totalSize);
	    for (K key : collectablePerCategory.keySet()) {
		for (String header : collectablePerCategory.get(key).obtainHeaders()) {
		    headers.add(createCategoryHeaderPrefix(key) + SEPARATOR + header);
		}
	    }

	    return headers;
	}

	@Override
	public Collection<?> obtainValues() {
	    Collection<Object> data = new ArrayList<>(totalSize);
	    for (K key : collectablePerCategory.keySet()) {
		data.addAll(collectablePerCategory.get(key).obtainValues());
	    }

	    return data;
	}

	@Override
	public int getSize() {
	    return totalSize;
	}

    }
}