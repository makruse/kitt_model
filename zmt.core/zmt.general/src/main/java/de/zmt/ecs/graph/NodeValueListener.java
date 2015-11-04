package de.zmt.ecs.graph;

/**
 * The main mechanism used for notifying the outside of the fact that a node
 * just got its evaluation.
 *
 * @author nicolae caralicea
 *
 * @param <T>
 *            element type
 */
public interface NodeValueListener<T> {
    /**
     *
     * The callback method used to notify the fact that a node that has assigned
     * the {@code nodeElement} value just got its evaluation.
     *
     * @param nodeElement
     *            The user set element of the node that is evaluated
     */
    void evaluate(T nodeElement);
}