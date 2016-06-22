package sim.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.zmt.output.Output;
import de.zmt.params.def.OptionalParamDefinition;
import sim.engine.SimState;
import sim.engine.ZmtSimState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ParamsInspector;
import sim.util.Bag;

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
    private final JMenu addMenu = new JMenu(ADD_MENU_TITLE);
    /** Menu for inspecting various things. */
    private final JMenu inspectMenu = new JMenu(INSPECT_MENU_TITLE);
    /** Set for items added to {@link Console#timeBox}. */
    private final Set<TimeBoxItem> addedTimeBoxItems = new HashSet<>();

    /**
     * Constructs a new {@code ZmtConsole}. {@link GUIState#state} must refer to
     * an instance of {@link ZmtSimState}.
     *
     * @param gui
     *            gui state to be used
     */
    public ZmtConsole(GUIState gui) {
        super(gui);

        if (!(gui.state instanceof ZmtSimState)) {
            throw new IllegalArgumentException(gui.state + " must be refer to an instance of " + ZmtSimState.class);
        }

        getJMenuBar().add(new ParamsMenu(this));
        // invisible as long there is no menu item
        addMenu.setVisible(false);
        getJMenuBar().add(addMenu);

        getJMenuBar().add(inspectMenu);
        inspectMenu.setEnabled(false);

        addInspectMenuItem(OUTPUT_INSPECTOR_NAME, (state, name) -> {
            Inspector inspector = Inspector.getInspector(((ZmtSimState) state.state).getOutput(), state, name);
            inspector.setVolatile(true);
            return inspector;
        });
    }

    /**
     * Adds menu item for adding new optional parameter definition objects to
     * the 'Add' menu. The class' simple name is used as text of the menu item.
     * 
     * @param optionalDefinitionClass
     *            the class to be used, needs a public no-argument constructor
     */
    public void addOptionalDefinitionMenuItem(final Class<? extends OptionalParamDefinition> optionalDefinitionClass) {
        addOptionalDefinitionMenuItem(optionalDefinitionClass, optionalDefinitionClass.getSimpleName());
    }

    /**
     * Adds menu item for adding new optional parameter definition objects to
     * the 'Add' menu.
     * 
     * @param optionalDefinitionClass
     *            the class to be used, needs a public no-argument constructor
     * @param menuItemText
     *            the text of the menu item to add a new definition
     */
    public void addOptionalDefinitionMenuItem(final Class<? extends OptionalParamDefinition> optionalDefinitionClass,
            String menuItemText) {
        addOptionalDefinitionMenuItem(optionalDefinitionClass, menuItemText, addMenu);
        addMenu.setVisible(true);
    }

    /**
     * Adds menu item for adding new optional parameter definition objects.
     * 
     * @param optionalDefinitionClass
     *            the class to be used, needs a public no-argument constructor
     * @param menuItemText
     *            the text of the menu item to add a new definition
     * @param menu
     *            the {@link JMenu} where the item is added
     */
    protected void addOptionalDefinitionMenuItem(final Class<? extends OptionalParamDefinition> optionalDefinitionClass,
            String menuItemText, JMenu menu) {
        JMenuItem addOptionalItem = new JMenuItem(menuItemText);
        if (SimApplet.isApplet()) {
            addOptionalItem.setEnabled(false);
        }
        addOptionalItem.addActionListener(new AddOptionalListener(optionalDefinitionClass));
        menu.add(addOptionalItem);
    }

    /**
     * Appends a menu item to the 'Inspect' menu adding the {@link Inspector}
     * from given supplier when triggered.
     * 
     * @param menuItemText
     *            the menu item text to be displayed
     * @param supplier
     *            the supplier for the inspector
     * 
     * @return the {@code Component} added
     */
    public JMenuItem addInspectMenuItem(String menuItemText, InspectorSupplier supplier) {
        return addInspectMenuItem(menuItemText, supplier, inspectMenu);
    }

    /**
     * Appends a menu item adding the {@link Inspector} from given supplier when
     * triggered.
     * 
     * @param menuItemText
     *            the menu item text to be displayed
     * @param supplier
     *            the supplier for the inspector
     * @param menu
     *            the {@link JMenu} where the item is added
     * 
     * @return the {@code Component} added
     */
    protected JMenuItem addInspectMenuItem(String menuItemText, InspectorSupplier supplier, JMenu menu) {
        JMenuItem menuItem = menu.add(menuItemText);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // set inspector to console
                Bag inspectors = new Bag();
                inspectors.add(supplier.getInspector(getSimulation(), null));
                Bag names = new Bag();
                names.add(menuItemText);
                setInspectors(inspectors, names);
            }
        });
        return menuItem;
    }

    /**
     * Adds a {@link TimeBoxItem} to the combo box at the bottom of the console
     * window.
     * 
     * @param item
     *            the {@link TimeBoxItem} to add
     * @return <tt>true</tt> if item was not already added
     */
    @SuppressWarnings("unchecked") // timeBox in Console is a raw type
    public boolean addTimeBoxItem(TimeBoxItem item) {
        if (addedTimeBoxItems.add(item)) {
            timeBox.addItem(item);
            return true;
        }
        return false;
    }

    /**
     * Removes a {@link TimeBoxItem} from the combo box at the bottom of the
     * console window.
     * 
     * @param item
     *            the {@link TimeBoxItem} to remove
     * @return <tt>true</tt> if the combo box contained the specified element
     */
    public boolean removeTimeBoxItem(TimeBoxItem item) {
        if (addedTimeBoxItems.remove(item)) {
            timeBox.removeItem(item);
            return true;
        }
        return false;
    }

    /**
     * Selects a {@link TimeBoxItem} in the combo box at the bottom of the
     * console window.
     * 
     * @param item
     *            the {@link Time} to select
     */
    public void selectTimeBoxItem(TimeBoxItem item) {
        timeBox.setSelectedItem(item);
    }

    /**
     * Gets the menu for adding optional definitions.
     *
     * @return the menu for adding optional definitions
     */
    protected JMenu getAddMenu() {
        return addMenu;
    }

    /**
     * Gets the menu for inspecting various things.
     *
     * @return the menu for inspecting various things
     */
    protected JMenu getInspectMenu() {
        return inspectMenu;
    }

    @Override
    void startSimulation() {
        super.startSimulation();
        // we have a simulation object now: enable the menu item
        inspectMenu.setEnabled(true);
    }

    @Override
    void updateTime(long steps, double time, double rate) {
        Object selectedItem = timeBox.getSelectedItem();
        if (addedTimeBoxItems.contains(selectedItem)) {
            updateTimeText(((TimeBoxItem) selectedItem).createText(getSimulation().state));
        }
        /*
         * Call super only if selected item is one of the original ones.
         * Otherwise a RuntimeException will be thrown.
         */
        else {
            super.updateTime(steps, time, rate);
        }
    }

    @FunctionalInterface
    public static interface InspectorSupplier {
        Inspector getInspector(GUIState state, String name);
    }

    /**
     * An item that can be added to the time box at the bottom of the console
     * window.
     * 
     * @see ZmtConsole#addTimeBoxItem(TimeBoxItem)
     * @see ZmtConsole#removeTimeBoxItem(TimeBoxItem)
     * @see ZmtConsole#selectTimeBoxItem(TimeBoxItem)
     * @author mey
     *
     */
    @FunctionalInterface
    public static interface TimeBoxItem {
        /** The text that is displayed for the item in the time box. */
        @Override
        String toString();

        /**
         * Creates the text displayed in the label next to the time box.
         * 
         * @param state
         *            the simulation state
         * @return the text to be displayed in the label next to the time box
         */
        String createText(SimState state);

        /**
         * Convenience factory method to create a {@link TimeBoxItem} with the
         * specified {@link Function} for creating text.
         * 
         * @param name
         *            the name to be displayed in the time box
         * @param createText
         *            the {@link Function} for creating the text displayed in
         *            the label next to the time box
         * @return the created {@link TimeBoxItem} item
         */
        public static TimeBoxItem create(String name, Function<SimState, String> createText) {
            return new TimeBoxItem() {

                @Override
                public String createText(SimState state) {
                    return createText.apply(state);
                }

                @Override
                public String toString() {
                    return name;
                }
            };
        }
    }

    /**
     * {@link ActionListener} implementation adding a new
     * {@link OptionalParamDefinition} to the simulation's parameters object.
     * 
     * @author mey
     *
     */
    private class AddOptionalListener implements ActionListener {
        private final Class<? extends OptionalParamDefinition> optionalDefinitionClass;

        private AddOptionalListener(Class<? extends OptionalParamDefinition> optionalDefinitionClass) {
            this.optionalDefinitionClass = optionalDefinitionClass;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ZmtSimState sim = (ZmtSimState) getSimulation().state;

            // add new fish definition to parameter object and model inspector
            OptionalParamDefinition optionalDefinition;
            try {
                optionalDefinition = optionalDefinitionClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                logger.log(Level.WARNING, "Cannot add object of " + optionalDefinitionClass
                        + ". No-arg constructor instantiation impossible.", ex);
                return;
            }

            sim.getParams().addOptionalDefinition(optionalDefinition);
            ((ParamsInspector) getModelInspector()).addDefinitionTab(optionalDefinition);

            // switch to models tab to inform user about change
            getTabPane().setSelectedComponent(modelInspectorScrollPane);
        }
    }
}
