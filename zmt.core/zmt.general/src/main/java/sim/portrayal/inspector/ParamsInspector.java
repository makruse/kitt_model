package sim.portrayal.inspector;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import sim.display.GUIState;
import sim.engine.params.SimParams;
import sim.engine.params.def.OptionalParamDefinition;
import sim.engine.params.def.ParamDefinition;
import sim.portrayal.Inspector;

/**
 * {@link TabbedInspector} with tabs generated from a {@link SimParams} object.
 * 
 * @author mey
 * 
 */
public class ParamsInspector extends TabbedInspector {
    private static final long serialVersionUID = 1L;

    private SimParams simParams;
    private final GUIState gui;

    /**
     * Constructs a {@code ParamsInspector} populated from the given
     * {@link SimParams} object
     * 
     * @param simParams
     *            the params object to populated this inspector from
     * @param gui
     *            GUI state of this inspector
     */
    public ParamsInspector(SimParams simParams, GUIState gui) {
	super();
	this.simParams = simParams;
	this.gui = gui;

	setVolatile(false);
	// scroll buttons in tab bar
	tabs.setTabPlacement(JTabbedPane.TOP);
	tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

	populateModelInspector();
    }

    /**
     * Populates the tabbed model inspector with data from simulation
     * parameters.
     */
    private void populateModelInspector() {
	clear();

	for (ParamDefinition def : simParams.getDefinitions()) {
	    addDefinitionTab(def);
	}
    }

    /**
     * Add given {@link ParamDefinition} as a new tab in {@link TabbedInspector}
     * .
     * 
     * @param def
     *            parameter definition
     * @return {@link Inspector} displaying given {@link ParamDefinition}
     */
    public Inspector addDefinitionTab(final ParamDefinition def) {
	final Inspector defInspector = Inspector.getInspector(def, gui, null);
	addInspector(defInspector, def.getTitle());

	int inspectorIndex = tabs.indexOfComponent(defInspector);
	JLabel tabLabel = new JLabel() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public String getText() {
		// text changes with definition's title
		if (def.getTitle() != null && !super.getText().equals(def.getTitle())) {
		    // need call to update internal state (e.g. to change width)
		    setText(def.getTitle());
		}

		return super.getText();
	    }
	};

	// optional parameter: add label with a close button
	Component tabComponent;
	if (def instanceof OptionalParamDefinition) {
	    tabComponent = new CloseButtonTabComponent(tabLabel, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    int i = tabs.indexOfComponent(defInspector);
		    Inspector optionalInspector = (Inspector) inspectors.get(i);

		    if (i != -1) {
			// remove tab with this component
			removeInspector(optionalInspector);
			simParams.removeOptionalDefinition((OptionalParamDefinition) def);
		    }
		}
	    });
	}
	// non-optional parameter: add the label without close button
	else {
	    tabComponent = tabLabel;
	}
	tabs.setTabComponentAt(inspectorIndex, tabComponent);

	// switch to newly added inspector
	tabs.setSelectedComponent(defInspector);

	return defInspector;
    }

    public void setParams(SimParams simParams) {
	this.simParams = simParams;

	populateModelInspector();
    }
}
