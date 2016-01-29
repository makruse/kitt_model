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
import sim.engine.params.def.OptionalParamDefinition;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ParamsInspector;
import sim.util.Bag;

/**
 * GUI console that can save / load parameters and add optional parameter
 * definitions to the simulation.
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
    private static final String SHOW_MENU_TITLE = "Show";
    private static final String OUTPUT_INSPECTOR_NAME = "Output Inspector";

    /** Menu for adding optional definitions. */
    private JMenu addMenu = new JMenu(ADD_MENU_TITLE);
    /** Menu for showing various items. */
    private JMenu showMenu = new JMenu(SHOW_MENU_TITLE);

    private final JMenuItem outputInspectorMenuItem = new JMenuItem(OUTPUT_INSPECTOR_NAME);
    private final OutputInspectorListener outputInspectorListener = new OutputInspectorListener();

    /**
     * C a new {@code ZmtConsole}.
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

	getJMenuBar().add(showMenu);
	showMenu.add(outputInspectorMenuItem);
	outputInspectorMenuItem.setEnabled(false);
	outputInspectorMenuItem.addActionListener(outputInspectorListener);
    }

    /**
     * Add menu item for adding new optional parameter definition objects.
     * 
     * @param optionalDefinitionClass
     *            the class to be used, needs a public no-argument constructor
     * @param menuItemName
     *            name of the menu item to add a new definition
     */
    public void addOptionalDefinitionMenuItem(final Class<? extends OptionalParamDefinition> optionalDefinitionClass,
	    String menuItemName) {
	JMenuItem addOptionalItem = new JMenuItem(menuItemName);
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
     * Appends a component to the end of the 'Show' menu.
     * 
     * @param c
     * @return the {@code Component} added
     */
    public Component addToShowMenu(Component c) {
	return showMenu.add(c);
    }

    @Override
    void startSimulation() {
	super.startSimulation();
	// we have a simulation object now: enable the menu item
	outputInspectorMenuItem.setEnabled(true);
    }

    /**
     * {@code ActionListener} to add an output inspector to the inspectors tab.
     * 
     * @author mey
     *
     */
    private class OutputInspectorListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    // get the inspector
	    Inspector outputInspector = Inspector.getInspector(((ZmtSimState) getSimulation().state).getOutput(),
		    getSimulation(), null);
	    outputInspector.setVolatile(true);

	    // add it to the console
	    Bag inspectors = new Bag();
	    inspectors.add(outputInspector);
	    Bag names = new Bag();
	    names.add(OUTPUT_INSPECTOR_NAME);
	    setInspectors(inspectors, names);
	}
    }
}
