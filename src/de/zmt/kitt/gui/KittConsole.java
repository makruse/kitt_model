package de.zmt.kitt.gui;

import java.awt.event.*;

import javax.swing.*;

import sim.display.SimApplet;
import de.zmt.kitt.sim.KittSim;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import de.zmt.sim_base.gui.ParamsConsole;
import de.zmt.sim_base.gui.portrayal.inspector.ParamsInspector;

public class KittConsole extends ParamsConsole {
    private static final long serialVersionUID = 1L;

    public KittConsole(KittGui gui) {
	super(gui);

	getTabPane().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

	// add 'Add' menu
	JMenu addMenu = new JMenu("Add");
	getJMenuBar().add(addMenu);

	// add menu item for adding a new species tab
	JMenuItem addSpecies = new JMenuItem("Species");
	if (SimApplet.isApplet())
	    addSpecies.setEnabled(false);
	addSpecies.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		doAddSpecies();
	    }
	});
	addMenu.add(addSpecies);
    }


    private void doAddSpecies() {
	KittGui gui = (KittGui) getSimulation();
	KittSim sim = (KittSim) gui.state;

	// add new species definition to parameter object and model inspector
	SpeciesDefinition def = new SpeciesDefinition();
	sim.getParams().addSpeciesDef(def);
	((ParamsInspector) getModelInspector()).addDefinitionTab(def);

	// switch to models tab to inform user about change
	int modelIndex = getTabPane().indexOfTab("Model");
	getTabPane().setSelectedIndex(modelIndex);
    }
}
