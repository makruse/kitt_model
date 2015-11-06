package sim.portrayal.portrayable;

/**
 * Simple portrayable to provide data for portraying fields.
 * 
 * @author mey
 * @param <T>
 *            type of field
 *
 */
public interface FieldPortrayable<T> extends Portrayable {
    /**
     * 
     * @return the field object
     */
    T getField();
}