package de.zmt.launcher.strategies;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.zmt.params.def.FieldLocator;
import de.zmt.util.ParamsUtil;

/**
 * Wrapper for {@code Map<FieldLocator, Object>} to provide some type
 * safety.
 * 
 * @author mey
 *
 */
@XStreamAlias("Combination")
public final class Combination {
    private final Map<FieldLocator, Object> combination;

    static {
	ParamsUtil.getXStreamInstance().processAnnotations(Combination.class);
    }

    /**
     * Constructs an empty {@code Combination}.
     */
    public Combination() {
        combination = Collections.emptyMap();
    }

    /**
     * Constructs a new {@code Combination} by wrapping around the given raw
     * combination.
     * 
     * @param combination
     *            the raw combination
     */
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