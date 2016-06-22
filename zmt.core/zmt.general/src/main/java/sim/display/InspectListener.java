package sim.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import sim.portrayal.Inspector;
import sim.util.Bag;

/**
 * Abstract {@code ActionListener} implementation to add an inspector to the
 * console's inspectors tab. Implementing classes need to specify the inspector
 * to be displayed.
 * 
 * @author mey
 *
 */
public abstract class InspectListener implements ActionListener {
    /** Reference to console for displaying the inspector. */
    private ZmtConsole zmtConsole;
    /** The name of the inspector for GUI display. */
    private final String inspectorName;

    /**
     * Constructs a new {@link InspectListener}.
     * 
     * @param inspectorName
     *            the name of the inspector for GUI display
     */
    public InspectListener(String inspectorName) {
	this.inspectorName = inspectorName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	// get the inspector
	Inspector inspector = getInspectorToShow(zmtConsole.getSimulation(), null);

	// add it to the console
	Bag inspectors = new Bag();
	inspectors.add(inspector);
	Bag names = new Bag();
	names.add(inspectorName);
	zmtConsole.setInspectors(inspectors, names);
    }

    /**
     * Called by console in when adding to 'Inspect' menu.
     * 
     * @see ZmtConsole#addInspectMenuItem(InspectListener)
     * @return the name of the inspector for GUI display
     */
    String getInspectorName() {
	return inspectorName;
    }

    /**
     * Called by console in when adding to 'Inspect' menu.
     * 
     * @see ZmtConsole#addInspectMenuItem(InspectListener)
     * @param zmtConsole
     */
    void setZmtConsole(ZmtConsole zmtConsole) {
	this.zmtConsole = zmtConsole;
    }

    /**
     * Returns the inspector to be displayed. Implementing classes need to
     * specify this.
     * 
     * @param state
     * @param name
     * @return the {@link Inspector} to be displayed when menu item was clicked
     */
    protected abstract Inspector getInspectorToShow(GUIState state, String name);
}