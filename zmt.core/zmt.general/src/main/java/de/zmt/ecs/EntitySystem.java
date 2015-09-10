package de.zmt.ecs;

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
     * Subsystems are updated according to their ordering. Same orderings could
     * run in parallel.
     * 
     * @return ordering number
     */
    int getOrdering();

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
     *            to be updated with this system, if it has the necessary
     *            components.
     */
    void update(Entity entity);
}
