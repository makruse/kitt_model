package de.zmt.kitt.sim.engine.output;

import java.util.*;

import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import sim.util.Proxiable;
import de.zmt.kitt.sim.engine.agent.Agent;
import de.zmt.kitt.sim.engine.agent.fish.*;
import de.zmt.kitt.sim.engine.agent.fish.Metabolism.LifeStage;
import de.zmt.kitt.sim.engine.agent.fish.Metabolism.Sex;
import de.zmt.kitt.util.UnitConstants;
import de.zmt.sim.engine.params.def.ParameterDefinition;

public class PopulationDataCollector
	extends
	AbstractCollector<ParameterDefinition, PopulationDataCollector.PopulationData> {
    private static final long serialVersionUID = 1L;

    public PopulationDataCollector(
	    Collection<? extends ParameterDefinition> agentClassDefs) {
	super(agentClassDefs);
    }

    @Override
    public void collect(Agent agent, Object message) {
	PopulationData classData = map.get(agent.getDefinition());

	if (classData == null) {
	    classData = new PopulationData();
	}

	classData.totalCount++;

	if (agent instanceof Fish) {
	    Metabolism metabolism = ((Fish) agent).getMetabolism();
	    Amount<Mass> biomass = metabolism.getBiomass();

	    classData.totalMass += biomass.doubleValue(UnitConstants.BIOMASS);

	    // fish is reproductive
	    if (metabolism.getSex() == Sex.FEMALE
		    && metabolism.getLifeStage() == LifeStage.ADULT) {
		classData.reproductiveCount++;
		classData.reproductiveMass += biomass
			.doubleValue(UnitConstants.BIOMASS);
	    }
	    // fish is juvenile
	    else if (metabolism.getLifeStage() == LifeStage.JUVENILE) {
		classData.juvenileCount++;
		classData.juvenileMass += biomass
			.doubleValue(UnitConstants.BIOMASS);
	    }
	}
    }

    @Override
    protected int getColumnCount() {
	return map.size() * PopulationData.HEADERS.size();
    }

    @Override
    protected PopulationData createCollectable(ParameterDefinition definition) {
	return new PopulationData();
    }

    /**
     * Population data for a class of agents.
     * 
     * @author cmeyer
     * 
     */
    // getters used in reflection by mason GUI
    @SuppressWarnings("unused")
    public static class PopulationData implements Collectable, Proxiable {
	private static final long serialVersionUID = 1L;

	private static final List<String> HEADERS = Arrays.asList(
		"total_count", "juvenile_count", "reproductive_count",
		"total_mass_" + UnitConstants.BIOMASS, "juvenile_mass_"
			+ UnitConstants.BIOMASS, "reproductive_mass_"
			+ UnitConstants.BIOMASS);

	private PopulationData() {
	    clear();
	}

	private int totalCount;
	private int juvenileCount;
	private int reproductiveCount;

	private double totalMass;
	private double juvenileMass;
	private double reproductiveMass;

	@Override
	public void clear() {
	    totalCount = 0;
	    juvenileCount = 0;
	    reproductiveCount = 0;

	    totalMass = 0;
	    juvenileMass = 0;
	    reproductiveMass = 0;
	}

	@Override
	public String toString() {
	    return "" + totalCount + " [...]";
	}

	@Override
	public Collection<String> obtainHeaders() {
	    return HEADERS;
	}

	@Override
	public Collection<?> obtainData() {
	    return Arrays.asList(totalCount, juvenileCount, reproductiveCount,
		    totalMass, juvenileMass, reproductiveMass);
	}

	@Override
	public Object propertiesProxy() {
	    return new MyPropertiesProxy();
	}

	public class MyPropertiesProxy {
	    public int getTotalCount() {
		return totalCount;
	    }

	    public int getJuvenileCount() {
		return juvenileCount;
	    }

	    public int getReproductiveCount() {
		return reproductiveCount;
	    }

	    public double getTotalMass() {
		return totalMass;
	    }

	    public double getJuvenileMass() {
		return juvenileMass;
	    }

	    public double getReproductiveMass() {
		return reproductiveMass;
	    }

	}
    }
}