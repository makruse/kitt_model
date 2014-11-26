package de.zmt.kitt.test;

import static org.junit.Assert.*;

import org.junit.Test;

import de.zmt.kitt.sim.Environment;
import de.zmt.kitt.sim.EnvironmentDefinition;
import de.zmt.kitt.sim.Fish;
import de.zmt.kitt.sim.LifeStage;
import de.zmt.kitt.sim.ModelParams;
import de.zmt.kitt.sim.Sim;
import de.zmt.kitt.sim.SpeciesDefinition;
import ec.util.MersenneTwisterFast;

public class TestHungerstate {

	@Test
	public void test() {
		
		ModelParams params;
		try {
			params = new ModelParams("resource/testParams.xml");
			SpeciesDefinition speciesDefinition = params.speciesList.get(0);	
			EnvironmentDefinition env = params.environmentDefinition;
			MersenneTwisterFast rand = new MersenneTwisterFast();			
			double initialBiomass= 7.0;
			EnergyBudget e= new EnergyBudget(speciesDefinition, env, initialBiomass, rand );
			
			double ageInYears=3.0;
			double virtualAge=3.4;
			double netActivityCosts=1.0;
			
			for(int step=0; step <10; step++){	
				double availableFood=rand.nextDouble()*14.0;
				e.feed(availableFood);
				e.updateEnergy(step, ageInYears,virtualAge,LifeStage.JUVENILE, netActivityCosts);
			}
			assertTrue( e.biomass > 3.0 );
						
			fail("Not yet implemented");

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}
}
