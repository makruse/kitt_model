package sim.engine.output;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import sim.engine.output.message.*;
import sim.util.Propertied;
import sim.util.Properties;

/**
 * Implementation for the {@link Collector} interface, discriminating between
 * categories. Dumps headers prefixed with the category in order to separate
 * data.
 * <p>
 * Data is stored within an encapsulated map that is used directly for providing
 * {@link Properties}.
 * 
 * @author mey
 * 
 * @param <K>
 *            the category type
 * @param <V>
 *            the type of stored {@code Collectable} objects for every category
 * @param <U>
 *            the value type contained within the stored collectables
 */
public abstract class CategoryCollector<K, V extends Collectable<U>, U>
	implements Collector<Collectable<U>>, Propertied, Serializable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CategoryCollector.class.getName());
    private static final long serialVersionUID = 1L;

    private final Map<K, V> collectablePerCategory;
    /** Accumulated size of all collectables. */
    private final int totalSize;
    /**
     * Separator in headers between category prefix and collectable's header.
     */
    private String separator = "$";
    private Collectable<U> mergingCollectable = new MergingCollectable();

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

    /**
     * Sets the separator string in headers between category prefix and
     * collectable's header.
     *
     * @param separator
     */
    public void setSeparator(String separator) {
	this.separator = separator;
    }

    @Override
    public void beforeCollect(BeforeMessage message) {
    }

    @Override
    public void afterCollect(AfterMessage message) {
    }

    /**
     * Returns a merged view over all collectables with headers prefixed by
     * categories.
     */
    @Override
    public Collectable<U> getCollectable() {
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
    private class MergingCollectable implements Collectable<U> {
	private static final long serialVersionUID = 1L;

	/** Obtains headers from collectables with the category's as prefix. */
	@Override
	public Iterable<String> obtainHeaders() {
	    Collection<String> headers = new ArrayList<>(totalSize);
	    for (K key : collectablePerCategory.keySet()) {
		for (String header : collectablePerCategory.get(key).obtainHeaders()) {
		    headers.add(createCategoryHeaderPrefix(key) + separator + header);
		}
	    }

	    return headers;
	}

	/** Obtains values from all collectables. */
	@Override
	public Iterable<U> obtainValues() {
	    Collection<U> data = new ArrayList<>(totalSize);
	    for (K key : collectablePerCategory.keySet()) {
		for (U value : collectablePerCategory.get(key).obtainValues()) {
		    data.add(value);
		}
	    }

	    return data;
	}

	@Override
	public int getSize() {
	    return totalSize;
	}

    }
}