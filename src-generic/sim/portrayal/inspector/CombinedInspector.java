package sim.portrayal.inspector;

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

    private final Inspector[] inspectors;

    public CombinedInspector(Inspector... inspectors) {
	this.inspectors = inspectors;

	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	for (Inspector inspector : inspectors) {
	    add(inspector);
	}
    }

    @Override
    public void updateInspector() {
	for (Inspector inspector : inspectors) {
	    inspector.updateInspector();
	}
    }

}
