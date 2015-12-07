package sim.display;

import sim.engine.ZmtSimState;
import sim.engine.params.SimParams;
import sim.portrayal.inspector.ParamsInspector;

/**
 * A {@link GUIState} that displays the parameter of its {@link ZmtSimState} in
 * a tabbed interface.
 * 
 * @author mey
 *
 */
public abstract class ZmtGUIState extends GUIState {

    /** Model inspector displaying definitions from Parameter object */
    private final ParamsInspector inspector;

    public ZmtGUIState(ZmtSimState state) {
	super(state);

	inspector = new ParamsInspector(state.getParams(), this);
    }

    /** Returns a {@link ParamsInspector} displaying {@link SimParams}. */
    @Override
    public ParamsInspector getInspector() {
	return inspector;
    }
}
