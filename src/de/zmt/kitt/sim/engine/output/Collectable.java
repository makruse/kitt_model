package de.zmt.kitt.sim.engine.output;

import java.io.Serializable;

import de.zmt.io.CsvWritable;

/**
 * Collectable data.
 * 
 * @author cmeyer
 * 
 */
public interface Collectable extends CsvWritable, Serializable {
    /** Clear collected data. */
    void clear();
}