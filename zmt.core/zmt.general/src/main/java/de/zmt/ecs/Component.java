package de.zmt.ecs;

import java.io.Serializable;

/**
 * Implementing classes can be added as components to an {@link Entity} to model
 * aspects of a simulation object.
 * <p>
 * Components are used to model state. The logic for manipulating them should be
 * contained in {@link EntitySystem}s.
 * 
 * @author cmeyer
 * 
 */
public interface Component extends Serializable {

}
