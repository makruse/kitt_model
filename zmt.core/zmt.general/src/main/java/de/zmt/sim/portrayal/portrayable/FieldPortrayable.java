package de.zmt.sim.portrayal.portrayable;

/**
 * Simple portrayable to provide data for portraying fields.
 * 
 * @author cmeyer
 *
 */
public interface FieldPortrayable extends Portrayable {
    /**
     * 
     * @return the field object
     */
    Object getField();
}