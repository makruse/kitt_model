package de.zmt.kitt.sim.engine.output;

import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.sim.engine.agent.Agent;
import de.zmt.kitt.sim.engine.agent.fish.*;
import de.zmt.kitt.sim.engine.agent.fish.Metabolism.LifeStage;
import de.zmt.kitt.sim.engine.agent.fish.Metabolism.Sex;
import de.zmt.kitt.util.*;
import de.zmt.sim.engine.params.def.ParameterDefinition;

public class PopulationDataCollector
	extends
	EncapsulatedClearableMap<ParameterDefinition, PopulationDataCollector.PopulationData>
	implements Collector {
    private static final long serialVersionUID = 1L;

    @Override
    public void collect(Agent agent, Object message) {
	PopulationDataCollector.PopulationData classData = map.get(agent
		.getDefinition());

	if (classData == null) {
	    classData = new PopulationData();
	    map.put(agent.getDefinition(), classData);
	}

	classData.totalCount++;

	if (agent instanceof Fish) {
	    Metabolism metabolism = ((Fish) agent).getMetabolism();
	    Amount<Mass> biomass = metabolism.getBiomass();

	    classData.totalMass = classData.totalMass.plus(biomass);

	    // fish is reproductive
	    if (metabolism.getSex() == Sex.FEMALE
		    && metabolism.getLifeStage() == LifeStage.ADULT) {
		classData.reproductiveCount++;
		classData.reproductiveMass = classData.reproductiveMass
			.plus(biomass);
	    }
	    // fish is juvenile
	    else if (metabolism.getLifeStage() == LifeStage.JUVENILE) {
		classData.juvenileCount++;
		classData.juvenileMass = classData.juvenileMass.plus(biomass);
	    }
	}
    }

    public PopulationData getPopulationData(ParameterDefinition agentClassDef) {
	return map.get(agentClassDef);
    }

    /**
     * Population data for a class of agents.
     * 
     * @author cmeyer
     * 
     */
    // getters used in reflection by mason GUI
    @SuppressWarnings("unused")
    public static class PopulationData implements Clearable {
	private PopulationData() {
	    clear();
	}

	private int totalCount;
	private int juvenileCount;
	private int reproductiveCount;

	private Amount<Mass> totalMass;
	private Amount<Mass> juvenileMass;
	private Amount<Mass> reproductiveMass;

	@Override
	public void clear() {
	    totalCount = 0;
	    juvenileCount = 0;
	    reproductiveCount = 0;

	    totalMass = AmountUtil.zero(UnitConstants.BIOMASS);
	    juvenileMass = AmountUtil.zero(UnitConstants.BIOMASS);
	    reproductiveMass = AmountUtil.zero(UnitConstants.BIOMASS);
	}

	public int getTotalCount() {
	    return totalCount;
	}

	public int getJuvenileCount() {
	    return juvenileCount;
	}

	public int getReproductiveCount() {
	    return reproductiveCount;
	}

	public Amount<Mass> getTotalMass() {
	    return totalMass;
	}

	public Amount<Mass> getJuvenileMass() {
	    return juvenileMass;
	}

	public Amount<Mass> getReproductiveMass() {
	    return reproductiveMass;
	}

	@Override
	public String toString() {
	    return "" + totalCount + " [...]";
	}
    }
}