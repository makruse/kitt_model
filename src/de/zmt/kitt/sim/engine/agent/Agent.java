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
    public enum LifeState {
	INSTANTIATED, ALIVE, DEAD
    }

    // TODO id probably not needed, remove it.
    /**
     * counter which is increased for each new instance for an Agent independent
     * of subclass
     */
    static long currentIdAssignment = 0;
    /** identifier for each Agent-Instance */
    public long id = 0;
    /** counter provides the id for each new agent instance */
    static long idCur = 0;
    protected LifeState lifeState = LifeState.INSTANTIATED;
    /** current position and position of last step of the agent in the world */
    public Double2D pos, oldpos;
    /** holds last movement distance independently of the movement method */
    public double dx = 0, dy = 0, dz = 0;
    private static final long serialVersionUID = 1L;
    /** */
    public int order;

    /**
     * @param Double3D
     *            pos
     * @param environment
     *            in which agent runs
     * @param cfg
     *            configuration for the agent
     */
    public Agent(final Double2D pos) {
	id = idCur++;
	this.pos = pos;
	this.oldpos = pos;
    }

    /**
     * called every time when its taken from queue in the scheduler
     */
    @Override
    public void step(final SimState state) {

    }

    public long getId() {
	return id;
    }

    public Double2D getPos() {
	return pos;
    }

    public Double2D getOldpos() {
	return oldpos;
    }
}
