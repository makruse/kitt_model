package de.zmt.kitt.test;

import de.zmt.kitt.sim.Agent;
import de.zmt.kitt.sim.Environment;
import de.zmt.kitt.sim.Fish;
import de.zmt.kitt.sim.ModelParams;
import de.zmt.kitt.sim.Sim;
import ec.util.MersenneTwisterFast;

public class MockEnvironment extends Environment {

	long step=0;
	
	public MockEnvironment(Sim sim,MersenneTwisterFast rand, ModelParams params) {
		super(sim,rand, params);	
		
		initPlayground();
	}

	
	public long getCurrentTimestep() {		
		return step++;
	}

	
	public void scheduleEnvironment() {
		System.out.println( "environment mock-scheduled");
	}

	
	public void schedule(Fish fish) {
		System.out.println( "fish mock-scheduled");
	}
	
	public void initPlayground(){
		
	}
}
