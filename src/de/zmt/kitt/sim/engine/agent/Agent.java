package de.zmt.kitt.sim.engine.agent;

import sim.engine.*;
import sim.util.Double2D;

/**
 * Superclass for all individuals in the simulation field.<br />
 * implements the step method of the Mason-Steppable.<br />
 * The step method is called in each time-step of a running simulation.<br />
 * for a different behavior of the agent it has to be overridden.<br />
 */
public abstract class Agent implements Steppable {
    private static final long serialVersionUID = 1L;

    public enum LifeState {
	INSTANTIATED, ALIVE, DEAD
    }

    protected LifeState lifeState = LifeState.INSTANTIATED;
    /** current position and position of last step of the agent in the world */
    public Double2D pos, oldpos;

    public Agent(final Double2D pos) {
	this.pos = pos;
	this.oldpos = pos;
    }

    @Override
    public void step(SimState state) {
	// remember last position
	oldpos = pos;
    }
}
