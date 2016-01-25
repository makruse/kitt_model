package de.zmt.launcher.strategies;

import sim.display.*;
import sim.engine.ZmtSimState;

public interface ClassLocator extends LauncherStrategy {

    /**
     * Locates {@link ZmtSimState} class literal from simulation name.
     * 
     * @param simName
     *            name of the simulation
     * @return simulation class of type {@link ZmtSimState}
     * @throws ClassNotFoundException
     */
    Class<? extends ZmtSimState> findSimStateClass(String simName) throws ClassNotFoundException;

    /**
     * Locates {@link GUIState} class literal from simulation name.
     * 
     * @param simName
     *            name of the simulation
     * @return {@link GUIState} class for this simulation
     * @throws ClassNotFoundException
     */
    Class<? extends ZmtGUIState> findGuiStateClass(String simName) throws ClassNotFoundException;

}