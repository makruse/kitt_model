package de.zmt.ecs.factory;

import de.zmt.ecs.*;
import ec.util.MersenneTwisterFast;

/**
 * Interface for a factory class that creates a certain {@link Entity}. To be
 * used in {@link EntityCreationHandler}.
 * 
 * @see EntityCreationHandler#addEntity(EntityFactory, int)
 * @author mey
 *
 */
public interface EntityFactory {
    /**
     * Creates a certain entity.
     * 
     * @param manager
     *            entity manager for the created entity
     * @param random
     *            random number generator
     * @return the created entity
     */
    Entity create(EntityManager manager, MersenneTwisterFast random);
}