package ecs;

/**
 * Entity systems are added to the {@link EntityManager} and called when an
 * {@link Entity} is stepped by the {@link sim.engine.Schedule}.
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

    void onAdd(EntityManager manager);

    void onRemove(EntityManager manager);

    void update(Entity entity);
}
