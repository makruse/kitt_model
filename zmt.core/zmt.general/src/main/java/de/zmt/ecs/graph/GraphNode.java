package de.zmt.ecs.graph;

import java.util.*;

/**
 *
 * It represents the node of the graph. It holds an element that is passed back
 * to the user when a node gets the chance to be evaluated.
 *
 * @author nicolae caralicea
 * @author mey
 *
 * @param <T>
 */
public final class GraphNode<T> {
    private final T element;
    /** Nodes on which this node depends on. */
    final Collection<GraphNode<T>> incomingNodes = new HashSet<>(0);
    /** Nodes which are dependent on this one. */
    final Collection<GraphNode<T>> outgoingNodes = new HashSet<>(0);

    public GraphNode(T value) {
	super();
	this.element = value;
    }

    /**
     * Outgoing nodes. These refer to nodes this node is depending on.
     *
     * @return outgoing nodes
     */
    public Collection<GraphNode<T>> getOutgoingNodes() {
	return Collections.unmodifiableCollection(outgoingNodes);
    }

    /**
     * Incoming nodes. These refer to nodes which are dependent on this node.
     *
     * @return incoming nodes
     */
    public Collection<GraphNode<T>> getIncomingNodes() {
	return Collections.unmodifiableCollection(incomingNodes);
    }

    /**
     * 
     * @return the user set element of this node
     */
    public T getElement() {
	return element;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[element=" + element + "]";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((element == null) ? 0 : element.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	GraphNode<?> other = (GraphNode<?>) obj;
	if (element == null) {
	    if (other.element != null) {
		return false;
	    }
	} else if (!element.equals(other.element)) {
	    return false;
	}
	return true;
    }
}
