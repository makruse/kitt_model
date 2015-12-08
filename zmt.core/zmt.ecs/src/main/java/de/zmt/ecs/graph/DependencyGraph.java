package de.zmt.ecs.graph;

import java.util.*;

/**
 *
 * Represents a graph of nodes. Every node is of GraphNode type and it has set
 * an element of the generic type <T>. It basically derives an evaluation order
 * out of its nodes. A node gets the chance to be evaluated when all the
 * incoming nodes were previously evaluated. The evaluating method of the
 * NodeValueListener is used to notify the outside of the fact that a node just
 * got the chance to be evaluated. An element of the node that is of the generic
 * type <T> is passed as argument to the evaluating method.
 *
 *
 * @author nicolae caralicea
 * @author mey
 *
 * @param <T>
 *            element type
 */
final public class DependencyGraph<T> {
    /**
     * These nodes represent the starting nodes. They are firstly evaluated.
     * They have no incoming nodes. The order they are evaluated does not
     * matter.
     */
    private final HashMap<T, GraphNode<T>> nodes = new HashMap<T, GraphNode<T>>();
    /**
     * The callback interface used to notify that a node was just evaluated
     */
    private final NodeValueListener<T> listener;

    /**
     * The main constructor has one parameter representing the callback
     * mechanism used by this class to notify when a node gets evaluated.
     *
     * @param listener
     *            The callback interface implemented by the user classes
     */
    public DependencyGraph(NodeValueListener<T> listener) {
	this.listener = listener;
    }

    /**
     * Constructs a dependency graph without a callback mechanism. The graph
     * then needs to be traversed manually.
     */
    public DependencyGraph() {
	this.listener = new NodeValueListener<T>() {

	    @Override
	    public void evaluate(T nodeValue) {
	    }
	};
    }

    /**
     * Add a new dependencies to the graph. Both parameters will be added to the
     * graph if not present already.
     * 
     * @param element
     *            element to add a dependency for
     * @param dependencies
     *            dependencies to add for {@code node}. Can be empty for
     *            elements without dependency.
     *
     * @return {@code true} if this graph changed as a result of the call
     */
    public boolean add(T element, @SuppressWarnings("unchecked") T... dependencies) {
	return add(element, Arrays.asList(dependencies));
    }

    /**
     * Add a new dependencies to the graph. Both parameters will be added to the
     * graph if not present already.
     * 
     * @param element
     *            element to add a dependency for
     * @param dependencies
     *            dependencies to add for {@code node}. Can be empty for
     *            elements without dependency.
     *
     * @return {@code true} if this graph changed as a result of the call
     */
    public boolean add(T element, Iterable<T> dependencies) {
	boolean changed = false;

	GraphNode<T> elementNode = null;
	if (nodes.containsKey(element)) {
	    elementNode = nodes.get(element);
	} else {
	    elementNode = new GraphNode<T>(element);
	    nodes.put(element, elementNode);
	    changed = true;
	}

	for (T dependency : dependencies) {
	    if (element.equals(dependency)) {
		throw new IllegalArgumentException(element + " cannot add itself as dependency!");
	    }

	    GraphNode<T> dependencyNode = null;
	    if (nodes.containsKey(dependency)) {
		dependencyNode = nodes.get(dependency);
	    } else {
		dependencyNode = new GraphNode<T>(dependency);
		nodes.put(dependency, dependencyNode);
	    }

	    changed |= dependencyNode.incomingNodes.add(elementNode);
	    changed |= elementNode.outgoingNodes.add(dependencyNode);
	}

	return changed;
    }

    /**
     * Removes an element from this graph. All other elements that depends on
     * this node will lose this dependency.
     * 
     * @param element
     *            the element to remove
     * @return {@code false} if {@code element} is not part of the graph
     */
    public boolean remove(Object element) {
	GraphNode<T> node = nodes.get(element);

	if (node == null) {
	    return false;
	}

	// remove reference to this node at its dependencies
	for (GraphNode<T> incomingNode : node.incomingNodes) {
	    incomingNode.outgoingNodes.remove(node);
	}
	// remove reference to this node at those depending on it
	for (GraphNode<T> outgoingNode : node.outgoingNodes) {
	    outgoingNode.incomingNodes.remove(node);
	}

	nodes.remove(element);
	return true;
    }

    /**
     * Traverses all nodes in correct order and call the
     * {@link NodeValueListener} object each time.
     */
    public void resolve() {
	Collection<GraphNode<T>> unresolvedNodes = new ArrayList<>(nodes.values());
	Set<GraphNode<T>> resolvedNodes = new HashSet<>();

	while (!unresolvedNodes.isEmpty()) {
	    boolean resolved = false;

	    for (Iterator<GraphNode<T>> iterator = unresolvedNodes.iterator(); iterator.hasNext();) {
		GraphNode<T> node = iterator.next();

		// if all dependencies are resolved: evaluate node
		if (resolvedNodes.containsAll(node.outgoingNodes)) {
		    listener.evaluate(node.getElement());
		    iterator.remove();
		    resolvedNodes.add(node);
		    resolved = true;
		}
	    }

	    // unable to resolve at least one node: circular reference
	    if (!resolved) {
		throw new IllegalStateException("Cannot resolve: circular reference detected in: " + unresolvedNodes);
	    }
	}
    }

    /**
     * Remove all elements from graph.
     */
    public void clear() {
	nodes.clear();
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[nodes=" + nodes.values() + "]";
    }
}