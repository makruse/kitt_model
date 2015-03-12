package de.zmt.sim.portrayal.portrayable;

/**
 * Provides encapsulation of portrayed classes.
 * 
 * @author cmeyer
 * 
 * @param <T>
 *            subclass of Portrayable
 */
public interface ProvidesPortrayable<T extends Portrayable> {
    T providePortrayable();
}
