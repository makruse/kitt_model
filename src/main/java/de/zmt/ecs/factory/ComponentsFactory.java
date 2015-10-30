package de.zmt.ecs.factory;

import java.io.Serializable;
import java.util.Collection;

import de.zmt.ecs.Component;
import de.zmt.ecs.factory.ComponentsFactory.Parameters;

/**
 * A factory to create components for a certain entity.
 * 
 * @author mey
 *
 * @param <T>
 *            type of the parameter object needed for creation
 */
interface ComponentsFactory<T extends Parameters> extends Serializable {

    /**
     * Creates components for a certain entity.
     * 
     * @param parameters
     * @return components
     */
    Collection<Component> createComponents(T parameters);

    /**
     * Class containing parameters needed to create the entity.
     * 
     * @author mey
     *
     */
    public static interface Parameters {

    }
}