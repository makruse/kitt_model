package de.zmt.sim.engine;

import java.util.logging.Logger;

import sim.engine.*;
import sim.util.Double2D;
import de.zmt.sim.engine.params.def.ParamDefinition;

/**
 * Parameterized agent.
 * 
 * @author cmeyer
 * 
 */
public abstract class ParamAgent implements Steppable, Stoppable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParamAgent.class
	    .getName());
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

    @Override
    public void stop() {
	if (stoppable != null) {
	    stoppable.stop();
	} else {
	    logger.warning("Did nothing. No stoppable set.");
	}
    }

    /**
     * @return {@link ParamDefinition} of the agent
     */
    public abstract ParamDefinition getDefinition();
}