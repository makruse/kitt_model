package de.zmt.launcher.strategies;

import java.nio.file.Path;

import sim.engine.params.AutoParams;
import sim.engine.params.SimParams;

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
    <T extends SimParams> T loadSimParams(Path simParamsPath, Class<T> simParamsClass) throws ParamsLoadFailedException;

    /**
     * Load automation parameters.
     * 
     * @param autoParamsPath
     *            path to automation parameters XML file
     * @return {@link AutoParams} object loaded from XML file
     * @throws ParamsLoadFailedException
     */
    AutoParams loadAutoParams(Path autoParamsPath) throws ParamsLoadFailedException;

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
