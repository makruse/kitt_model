package de.zmt.sim.display;

import sim.display.GUIState;
import sim.engine.SimState;
import de.zmt.launcher.strategies.DefaultClassLocatorTest;

/**
 * Test class used within {@link DefaultClassLocatorTest}.
 * 
 * @author cmeyer
 *
 */
public class ExampleWithUI extends GUIState {
    public ExampleWithUI(SimState state) {
	super(state);
    }
}