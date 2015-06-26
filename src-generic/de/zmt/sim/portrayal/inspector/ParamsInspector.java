package de.zmt.sim.portrayal.inspector;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;

import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.TabbedInspector;
import de.zmt.sim.engine.params.Params;
import de.zmt.sim.engine.params.def.*;
import de.zmt.sim.portrayal.component.CloseButtonTabComponent;

/**
 * {@link TabbedInspector} with tabs generated from a {@link Params} object.
 * 
 * @author cmeyer
 * 
 */
public class ParamsInspector extends TabbedInspector {
    private static final long serialVersionUID = 1L;

    private Params params;
    private final GUIState gui;

    public ParamsInspector(Params params, GUIState gui) {
	super();
	this.params = params;
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

	for (ParamDefinition def : params.getDefinitions()) {
	    addDefinitionTab(def);
	}
    }

    /**
     * Add given {@link ParamDefinition} as a new tab in
     * {@link TabbedInspector}.
     * 
     * @param tabbedInspector
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
		if (def.getTitle() != null
			&& !super.getText().equals(def.getTitle())) {
		    // need call to update internal state (e.g. to change width)
		    setText(def.getTitle());
		}

		return super.getText();
	    }
	};

	// optional parameter: add label with a close button
	Component tabComponent;
	if (def instanceof OptionalParamDefinition) {
	    tabComponent = new CloseButtonTabComponent(tabLabel,
		    new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
			    int i = tabs.indexOfComponent(defInspector);
			    Inspector optionalInspector = (Inspector) inspectors
				    .get(i);

			    if (i != -1) {
				// remove tab with this component
				removeInspector(optionalInspector);
				params.removeOptionalDefinition((OptionalParamDefinition) def);
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

    public void setParams(Params params) {
	this.params = params;

	populateModelInspector();
    }
}
