package sim.display;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import sim.engine.Parameterizable;
import sim.engine.ZmtSimState;
import sim.engine.output.Output;
import sim.engine.params.def.OptionalParamDefinition;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ParamsInspector;

/**
 * GUI console that can save / load parameters and add optional parameter
 * definitions to the simulation.
 * <p>
 * Two menus are added by this console. The 'Add' menu adds optional definitions
 * to the simulation while the 'Inspect' menu is used for displaying inspectors
 * like that from {@link Output} which is added by default.
 * 
 * @see GUIState#createController()
 * @author mey
 *
 */
public class ZmtConsole extends Console {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ZmtConsole.class.getName());

    // ADD MENU
    private static final String ADD_MENU_TITLE = "Add";

    // SHOW MENU
    private static final String INSPECT_MENU_TITLE = "Inspect";
    private static final String OUTPUT_INSPECTOR_NAME = "Output";

    /** Menu for adding optional definitions. */
    private JMenu addMenu = new JMenu(ADD_MENU_TITLE);
    /** Menu for inspecting various things. */
    private JMenu inspectMenu = new JMenu(INSPECT_MENU_TITLE);

    /**
     * Constructs a new {@code ZmtConsole}.
     *
     * @param gui
     *            gui state to be used
     */
    public ZmtConsole(GUIState gui) {
	super(gui);

	getJMenuBar().add(new ParamsMenu(this));
	// invisible as long there is no menu item
	addMenu.setVisible(false);
	getJMenuBar().add(addMenu);

	getJMenuBar().add(inspectMenu);
	inspectMenu.setEnabled(false);

	addToInspectMenu(new InspectListener(OUTPUT_INSPECTOR_NAME) {

	    @Override
	    protected Inspector getInspectorToShow(GUIState state, String name) {
		Inspector outputInspector = Inspector.getInspector(((ZmtSimState) state.state).getOutput(), state,
			name);
		outputInspector.setVolatile(true);
		return outputInspector;
	    }
	});
    }

    /**
     * Add menu item for adding new optional parameter definition objects.
     * 
     * @param optionalDefinitionClass
     *            the class to be used, needs a public no-argument constructor
     * @param menuItemText
     *            the text of the menu item to add a new definition
     */
    public void addOptionalDefinitionMenuItem(final Class<? extends OptionalParamDefinition> optionalDefinitionClass,
	    String menuItemText) {
	JMenuItem addOptionalItem = new JMenuItem(menuItemText);
	if (SimApplet.isApplet()) {
	    addOptionalItem.setEnabled(false);
	}
	addOptionalItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		doAddOptional(optionalDefinitionClass);
	    }
	});
	addMenu.add(addOptionalItem);
	addMenu.setVisible(true);
    }

    private void doAddOptional(Class<? extends OptionalParamDefinition> optionalDefinitionClass) {
	Parameterizable sim = (Parameterizable) getSimulation().state;

	// add new fish definition to parameter object and model inspector
	OptionalParamDefinition optionalDefinition;
	try {
	    optionalDefinition = optionalDefinitionClass.newInstance();
	} catch (InstantiationException | IllegalAccessException e) {
	    logger.log(Level.WARNING, "Cannot add object of " + optionalDefinitionClass
		    + ". No-arg constructor instantiation impossible.", e);
	    return;
	}

	sim.getParams().addOptionalDefinition(optionalDefinition);
	((ParamsInspector) getModelInspector()).addDefinitionTab(optionalDefinition);

	// switch to models tab to inform user about change
	getTabPane().setSelectedComponent(modelInspectorScrollPane);
    }

    /**
     * Appends a menu item triggering given listener. Menu text is listener's
     * inspector name.
     * 
     * @param listener
     *            the listener to trigger by the appended menu item
     * @return the {@code Component} added
     */
    public Component addToInspectMenu(InspectListener listener) {
	listener.setZmtConsole(this);
	JMenuItem menuItem = new JMenuItem(listener.getInspectorName());
	menuItem.addActionListener(listener);
	return inspectMenu.add(menuItem);
    }

    @Override
    void startSimulation() {
	super.startSimulation();
	// we have a simulation object now: enable the menu item
	inspectMenu.setEnabled(true);
    }
}
