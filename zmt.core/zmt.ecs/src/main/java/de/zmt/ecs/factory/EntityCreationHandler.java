package de.zmt.ecs.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;
import sim.engine.Stoppable;

/**
 * Class for handling entity creation. Added entities are scheduled and
 * listeners notified. Instances of {@link EntityFactory} can be used for adding
 * entities as well.
 * 
 * @author mey
 *
 */
public class EntityCreationHandler implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MersenneTwisterFast random;
    private final EntityManager manager;
    private final Schedule schedule;
    private final Collection<EntityCreationListener> listeners = new ArrayList<>();

    /**
     * Constructs a new {@code EntityCreationHandler} adding entities to
     * {@code manager} and {@code schedule}.
     * 
     * @param manager
     * @param random
     * @param schedule
     */
    public EntityCreationHandler(EntityManager manager, MersenneTwisterFast random, Schedule schedule) {
        super();
        this.manager = manager;
        this.random = random;
        this.schedule = schedule;
    }

    /**
     * Schedule an entity created from factory and notify listeners.
     * 
     * @param factory
     *            the factory to create the entity
     * @param factoryParam
     *            the parameter object for the factory
     * @param ordering
     *            ordering in {@link Schedule}
     * @return created entity
     */
    public <T> Entity addEntity(EntityFactory<T> factory, T factoryParam, int ordering) {
        Entity entity = factory.create(getManager(), random, factoryParam);
        addEntity(entity, ordering);
        return entity;
    }

    /**
     * Schedule an existing entity and notify listeners.
     * 
     * @param entity
     * @param ordering
     *            ordering in {@link Schedule}
     * @return {@code entity}
     */
    public Entity addEntity(final Entity entity, int ordering) {
        // create stoppable triggering removal of fish from schedule and field
        final Stoppable scheduleStoppable = schedule.scheduleRepeating(schedule.getTime() + 1.0, ordering, entity);

        // create stoppable triggering removal of fish from schedule and field
        entity.addStoppable(new Stoppable() {
            private static final long serialVersionUID = 1L;

            @Override
            public void stop() {
                scheduleStoppable.stop();

                // notify listeners of removal
                for (EntityCreationListener listener : listeners) {
                    listener.onRemoveEntity(entity);
                }
            }
        });

        // notify listeners of creation
        for (EntityCreationListener listener : listeners) {
            listener.onCreateEntity(entity);
        }

        return entity;
    }

    protected MersenneTwisterFast getRandom() {
        return random;
    }

    protected Schedule getSchedule() {
        return schedule;
    }

    public EntityManager getManager() {
        return manager;
    }

    public boolean addListener(EntityCreationListener listener) {
        return listeners.add(listener);
    }

    public boolean removeListener(EntityCreationListener listener) {
        return listeners.remove(listener);
    }
}