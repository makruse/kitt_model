package de.zmt.ecs.factory;

import de.zmt.ecs.*;
import ec.util.MersenneTwisterFast;

/**
 * Interface for a factory class that creates a certain {@link Entity}. To be
 * used in {@link EntityCreationHandler}.
 * 
 * @see EntityCreationHandler#addEntity(EntityFactory, Object, int)
 * @author mey
 * @param <T>
 *            the type of parameter object
 *
 */
public interface EntityFactory<T> {
    /**
     * Creates a certain entity.
     * 
     * @param manager
     *            entity manager for the created entity
     * @param random
     *            random number generator
     * @param parameter
     *            a parameter object
     * @return the created entity
     */
    Entity create(EntityManager manager, MersenneTwisterFast random, T parameter);
}