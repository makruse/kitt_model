package de.zmt.kitt.sim.engine;

import sim.display.GUIState;
import sim.engine.*;
import sim.portrayal.*;
import sim.portrayal.inspector.ProvidesInspector;
import de.zmt.sim.portrayal.inspector.CombinedInspector;

/**
 * Provides continuous output within the GUI via {@link Inspector} and file.
 * 
 * @author cmeyer
 * 
 */
// TODO file output
public class KittOutput implements Steppable, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    private final Environment environment;

    public KittOutput(Environment environment) {
	this.environment = environment;
    }

    @Override
    public void step(SimState state) {
	// TODO calculate continuous output

    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new CombinedInspector(new SimpleInspector(this, state, name),
		Inspector.getInspector(environment, state,
			Environment.class.getSimpleName()));
    }

}
