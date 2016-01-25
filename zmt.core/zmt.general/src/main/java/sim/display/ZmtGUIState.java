package sim.display;

import java.util.*;

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
    private final List<GuiListener> guiListeners = new ArrayList<>(1);

    /** Model inspector displaying definitions from Parameter object. */
    private final ParamsInspector inspector;

    public ZmtGUIState(ZmtSimState state) {
	super(state);

	inspector = new ParamsInspector(state.getParams(), this);
    }

    /**
     * Adds a {@link GuiListener} to be notified on gui events.
     * 
     * @param listener
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public final boolean addGuiListener(GuiListener listener) {
	return guiListeners.add(listener);
    }

    /**
     * Removes a {@link GuiListener}, which is no longer notified on GUI events.
     * 
     * @param listener
     * @return <tt>true</tt> if specified listener was removed
     */
    public final boolean removeGuiListener(Object listener) {
	return guiListeners.remove(listener);
    }

    @Override
    public void start() {
	for (GuiListener listener : guiListeners) {
	    listener.onGuiStart();
	}
	super.start();
    }

    @Override
    public void finish() {
	for (GuiListener listener : guiListeners) {
	    listener.onGuiFinish();
	}
	super.finish();
    }

    @Override
    public void quit() {
	super.quit();
	guiListeners.clear();
    }

    /** Returns a {@link ParamsInspector} displaying {@link SimParams}. */
    @Override
    public ParamsInspector getInspector() {
	return inspector;
    }

    public static interface GuiListener extends EventListener {
	/** Called on {@link GUIState#start()}. */
	void onGuiStart();

	/** Called on {@link GUIState#finish()}. */
	void onGuiFinish();
    }
}
