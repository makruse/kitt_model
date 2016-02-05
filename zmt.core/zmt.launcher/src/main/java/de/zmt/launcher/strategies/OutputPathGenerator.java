package de.zmt.launcher.strategies;

import java.nio.file.Path;

import de.zmt.launcher.LauncherArgs.Mode;
import sim.engine.SimState;

public interface OutputPathGenerator extends LauncherStrategy {
    /**
     * Returns an {@link Iterable} that generates a path to a unique directory
     * each time.
     * 
     * @param simClass
     *            the simulation class to generate an output path for
     * @param mode
     *            the launch mode
     * @param directory
     *            the parent directory for the created directories
     * @return iterable to directories
     */
    Iterable<Path> createPaths(Class<? extends SimState> simClass, Mode mode, Path directory);
}
