package de.zmt.ecs;

import java.util.Collection;

/**
 * Entity systems are added to the {@link EntityManager} and called when an
 * {@link Entity} is stepped by the {@link sim.engine.Schedule}.
 * <p>
 * A System's state is global if there is any, because it is used on all
 * entities. Usually Systems are stateless and contain only logic, while
 * {@link Component}s represent simulation objects' individual state.
 * 
 * @author cmeyer
 * 
 */
public interface EntitySystem {
    /**
     * Classes of other systems which must be updated before this system.
     * Systems independent from each other can be updated in parallel. If this
     * system is not dependent on other systems, an empty collection should be
     * returned.
     * 
     * @return dependencies
     */
    Collection<Class<? extends EntitySystem>> getDependencies();

    /**
     * Called when added to an {@link EntityManager}.
     * 
     * @param manager
     */
    void onAdd(EntityManager manager);

    /**
     * Called when removed from an {@link EntityManager}.
     * 
     * @param manager
     */
    void onRemove(EntityManager manager);

    /**
     * @param entity
     *            to be updated with this system
     */
    void update(Entity entity);
}
