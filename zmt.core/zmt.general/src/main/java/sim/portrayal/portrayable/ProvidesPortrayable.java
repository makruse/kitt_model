package sim.portrayal.portrayable;

/**
 * Provides encapsulation of portrayed classes. Use this interface to provide an
 * object containing accessors for portrayal. This is to make intent clear and
 * mark these accessors to be only for that specific use.
 * <p>
 * For example, a class contains a grid which is used internally only but the
 * object itself is needed to be portrayed in GUI. A portrayable is provided
 * containing an accessor for the grid, which intent is then clearly marked.
 * 
 * @author cmeyer
 * 
 * @param <T>
 *            subclass of Portrayable
 */
public interface ProvidesPortrayable<T extends Portrayable> {
    /**
     * @return {@link Portrayable} of the object
     */
    T providePortrayable();
}
