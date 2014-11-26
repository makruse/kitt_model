package de.zmt.kitt.test;

import de.zmt.kitt.sim.Environment;
import de.zmt.kitt.sim.Fish;
import de.zmt.kitt.sim.ModelParams;
import de.zmt.kitt.sim.SpeciesDefinition;

public class MockFish extends Fish {

	public MockFish(final double x, final double y, final double z, double initialBiomass, double initialSize, Environment env, ModelParams params, SpeciesDefinition speciesDefinition){
		super(x, y, z, initialBiomass, initialSize, env, params,speciesDefinition);
		
	}
}
