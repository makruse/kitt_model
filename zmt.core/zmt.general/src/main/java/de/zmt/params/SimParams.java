package de.zmt.params;

/**
 * Simulation parameters that contain a value for the seed of the random number
 * generator used.
 * 
 * @author mey
 *
 */
public interface SimParams extends ParamDefinition {
    public static final String DEFAULT_FILENAME = "params.xml";

    /** @return the seed value from this {@link SimParams} object. */
    long getSeed();
}