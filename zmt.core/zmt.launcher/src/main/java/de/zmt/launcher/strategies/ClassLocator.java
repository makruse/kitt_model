package de.zmt.launcher.strategies;

import sim.display.GUIState;
import sim.engine.ZmtSimState;

public interface ClassLocator extends LauncherStrategy {

    /**
     * @param simName
     *            name of the simulation
     * @return simulation class of type {@link ZmtSimState}
     * @throws ClassNotFoundException
     */
    Class<? extends ZmtSimState> findSimStateClass(String simName) throws ClassNotFoundException;

    /**
     * @param simName
     *            name of the simulation
     * @return {@link GUIState} class for this simulation
     * @throws ClassNotFoundException
     */
    Class<? extends GUIState> findGuiStateClass(String simName) throws ClassNotFoundException;

}