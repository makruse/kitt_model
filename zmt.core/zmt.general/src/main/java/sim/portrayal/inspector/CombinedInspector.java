package sim.portrayal.inspector;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import sim.engine.Stoppable;
import sim.portrayal.Inspector;

/**
 * Combines multiple inspectors and aligns them vertically. On
 * {@link #updateInspector()}, all added inspectors are updated.
 * 
 * @author mey
 * 
 */
public class CombinedInspector extends Inspector {
    private static final long serialVersionUID = 1L;

    public CombinedInspector(Collection<? extends Inspector> inspectors) {
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	for (Inspector inspector : inspectors) {
	    add(inspector);
	}
    }

    public CombinedInspector(Inspector... inspectors) {
	this(Arrays.asList(inspectors));
    }

    @Override
    public JFrame createFrame(Stoppable stopper) {
	JFrame frame = super.createFrame(stopper);
	// get rid of scroller from super
	frame.setContentPane(this);
	frame.pack();
	return frame;
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
