package de.zmt.ecs.factory;

import de.zmt.ecs.Entity;

/**
 * Listener interface for receiving notifications when entities are created and
 * removed.
 * 
 * @author mey
 *
 */
public interface EntityCreationListener {
    /**
     * Invoked when an entity was created.
     * 
     * @param entity
     *            entity that was created
     */
    void onCreateEntity(Entity entity);

    /**
     * Invoked when a an entity was removed.
     * 
     * @param entity
     *            entity that was removed
     */
    void onRemoveEntity(Entity entity);
}