package de.zmt.sim.portrayal.inspector;

import java.awt.Component;

import javax.swing.BoxLayout;

import sim.portrayal.Inspector;

/**
 * Combines multiple inspectors and aligns them vertically.
 * 
 * @author cmeyer
 * 
 */
public class CombinedInspector extends Inspector {
    private static final long serialVersionUID = 1L;

    public CombinedInspector(Inspector... inspectors) {
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	for (Inspector inspector : inspectors) {
	    add(inspector);
	}
    }

    @Override
    public void updateInspector() {
	for (Component component : getComponents()) {
	    if (component instanceof Inspector) {
		((Inspector) component).updateInspector();
	    }
	}
    }

}
