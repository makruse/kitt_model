package sim.portrayal.inspector;

import java.awt.Component;
import java.util.*;

import javax.swing.BoxLayout;

import sim.portrayal.Inspector;

/**
 * Combines multiple inspectors and aligns them vertically.
 * 
 * @author mey
 * 
 */
public class CombinedInspector extends Inspector {
    private static final long serialVersionUID = 1L;

    public CombinedInspector(Collection<Inspector> inspectors) {
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	for (Inspector inspector : inspectors) {
	    add(inspector);
	}
    }

    public CombinedInspector(Inspector... inspectors) {
	this(Arrays.asList(inspectors));
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
