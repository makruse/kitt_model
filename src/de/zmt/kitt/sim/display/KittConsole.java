package de.zmt.kitt.sim.display;

import java.awt.event.*;

import javax.swing.*;

import sim.display.SimApplet;
import de.zmt.kitt.sim.KittSim;
import de.zmt.kitt.sim.params.KittParams;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import de.zmt.sim.display.ParamsConsole;
import de.zmt.sim.engine.params.AbstractParams;
import de.zmt.sim.portrayal.inspector.ParamsInspector;

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

	setCurrentDir(KittSim.DEFAULT_INPUT_DIR);
    }

    private void doAddSpecies() {
	KittGui gui = (KittGui) getSimulation();
	KittSim sim = (KittSim) gui.state;

	// add new fish definition to parameter object and model inspector
	SpeciesDefinition def = new SpeciesDefinition();
	sim.getParams().addSpeciesDef(def);
	((ParamsInspector) getModelInspector()).addDefinitionTab(def);

	// switch to models tab to inform user about change
	int modelIndex = getTabPane().indexOfTab("Model");
	getTabPane().setSelectedIndex(modelIndex);
    }

    @Override
    protected Class<? extends AbstractParams> getParamsClass() {
	return KittParams.class;
    }
}
