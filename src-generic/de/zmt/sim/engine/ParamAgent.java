package de.zmt.sim.engine;

import sim.engine.*;
import sim.util.Double2D;
import de.zmt.sim.engine.params.def.ParamDefinition;

/**
 * Parameterized agent.
 * 
 * @author cmeyer
 * 
 */
public abstract class ParamAgent implements Steppable {
    private static final long serialVersionUID = 1L;

    protected Double2D position;
    protected Stoppable stoppable;

    public ParamAgent(Double2D pos) {
	this.position = pos;
    }

    public Double2D getPosition() {
	return position;
    }

    public void setStoppable(Stoppable stoppable) {
        this.stoppable = stoppable;
    }
    
    /**
     * @return {@link ParamDefinition} of the agent
     */
    public abstract ParamDefinition getDefinition();
}