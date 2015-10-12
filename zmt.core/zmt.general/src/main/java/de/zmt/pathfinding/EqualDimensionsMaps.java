package de.zmt.pathfinding;

import java.util.*;

/**
 * A collection that allows only maps to be added if they match certain
 * dimensions.
 * 
 * @author mey
 *
 * @param <E>
 *            type of maps in collection
 */
public class EqualDimensionsMaps<E extends PathfindingMap> implements Collection<E> {
    private final Collection<E> maps;
    private final int width;
    private final int height;

    public EqualDimensionsMaps(Collection<E> mapCollectionObject, int width, int height) {
	super();
	this.maps = mapCollectionObject;
	this.width = width;
	this.height = height;
    }

    private void enforceDimensions(E map) {
	if (map.getWidth() != width || map.getHeight() != height) {
	    throw new IllegalArgumentException("Dimensions must match:\n" + "width: " + width + ", height: " + height);
	}
    }

    @Override
    public int size() {
	return maps.size();
    }

    @Override
    public boolean isEmpty() {
	return maps.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
	return maps.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
	return maps.iterator();
    }

    @Override
    public Object[] toArray() {
	return maps.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
	return maps.toArray(a);
    }

    @Override
    public boolean add(E e) {
	enforceDimensions(e);
	return maps.add(e);
    }

    @Override
    public boolean remove(Object o) {
	return maps.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
	return maps.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
	for (E map : c) {
	    enforceDimensions(map);
	}
	return maps.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
	return maps.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	return maps.retainAll(c);
    }

    @Override
    public void clear() {
	maps.clear();
    }

    @Override
    public boolean equals(Object o) {
	return maps.equals(o);
    }

    @Override
    public int hashCode() {
	return maps.hashCode();
    }
}
