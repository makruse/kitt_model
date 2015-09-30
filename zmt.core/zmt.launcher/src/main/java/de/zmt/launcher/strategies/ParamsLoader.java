package de.zmt.launcher.strategies;

import de.zmt.sim.engine.params.*;

public interface ParamsLoader extends LauncherStrategy {
    /**
     * Load simulation parameters.
     * 
     * @param simParamsPath
     *            path to simulation parameters XML file
     * @param simParamsClass
     *            type of simulation parameters
     * @return {@link SimParams} object of type {@code simParamsClass} loaded
     *         from XML file
     * @throws ParamsLoadFailedException
     */
    <T extends SimParams> T loadSimParams(String simParamsPath,
	    Class<T> simParamsClass) throws ParamsLoadFailedException;

    /**
     * Load automation parameters.
     * 
     * @param autoParamsPath
     *            path to automation parameters XML file
     * @return {@link AutoParams} object loaded from XML file
     * @throws ParamsLoadFailedException
     */
    AutoParams loadAutoParams(String autoParamsPath)
	    throws ParamsLoadFailedException;

    public static class ParamsLoadFailedException extends Exception {
	private static final long serialVersionUID = 1L;

	public ParamsLoadFailedException(String message, Throwable cause) {
	    super(message, cause);
	}

	public ParamsLoadFailedException(String message) {
	    super(message);
	}

	public ParamsLoadFailedException(Throwable cause) {
	    super(cause);
	}

    }
}