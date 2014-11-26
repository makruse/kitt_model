package de.zmt.kitt.sim;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.Double3D;

/**
 * Superclass for all individuals in the simulation field.<br />
 * implements the step method of the Mason-Steppable.<br />
 * The step method is called in each time-step of a running simulation.<br />
 * for a different behavior of the agent it has to be overridden.<br /> 
 */
public abstract class Agent implements Steppable{
		
	/** counter which is increased for each new instance for an Agent independent of subclass*/
	static long currentIdAssignment=0;
	/** identifier for each Agent-Instance */
	public long id=0;
	/** counter provides the id for each new agent instance */
	static long idCur=0;	
	/** after first call of the step method born is set to true */
	protected boolean born;
	/** after initialization, alive is set to true.when destruct is caught, alive is set to false and the live loop has no actions anymore */
	protected boolean alive;	
	/** current position and position of last step of the agent in the world */
	public Double3D pos,oldpos; 	
	/** holds last movement distance independently of the movement method */
	public double dx=0,dy=0,dz=0;
	private static final long serialVersionUID = 1L;
	/** */
	public int order;
	
	
	/**
	 * @param Double3D pos
	 * @param environment in which agent runs
	 * @param cfg configuration for the agent
	 */
	public Agent( final Double3D pos)
	{
		id=idCur++;
		this.oldpos = pos;
		this.born=false;
		this.alive=false;
	} 
	
	
	 /**
	 * called every time when its taken from queue in the scheduler
	 */
	@Override
	public void step( final SimState state )
	{	

	}
	
	/** */
	public long getId() {
		return id;
	}

	/** */
	public boolean isAlive() {
		return alive;
	}

	/** */
	public Double3D getPos() {
		return pos;
	}

	/** */
	public Double3D getOldpos() {
		return oldpos;
	}
}
