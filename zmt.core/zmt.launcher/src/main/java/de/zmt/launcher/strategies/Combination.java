package de.zmt.launcher.strategies;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import sim.engine.params.def.FieldLocator;

/**
 * Wrapper for {@code Map<FieldLocator, Object>} to provide some type
 * safety.
 * 
 * @author mey
 *
 */
@XmlRootElement(name = "combination", namespace = "http://www.zmt-bremen.de/")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Combination {
    private final Map<FieldLocator, Object> combination;

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