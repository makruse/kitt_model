package sim.display;

import de.zmt.launcher.strategies.DefaultClassLocatorTest;
import sim.engine.ZmtSimState;

/**
 * Test class used within {@link DefaultClassLocatorTest}.
 * 
 * @author mey
 *
 */
public class ExampleWithUI extends ZmtGUIState {
    public ExampleWithUI(ZmtSimState state) {
	super(state);
    }
}
