package sim.engine.output;

import java.util.*;

import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.util.UnitConstants;
import sim.engine.params.def.ParamDefinition;
import sim.params.def.SpeciesDefinition;
import sim.util.Proxiable;

/**
 * Collects population data for every species.
 * 
 * @see PopulationData
 * @see SpeciesDefinition
 * @author mey
 *
 */
public class PopulationDataCollector
	extends DefinitionSeparatedCollector<ParamDefinition, PopulationDataCollector.PopulationData> {
    private static final long serialVersionUID = 1L;

    public PopulationDataCollector(Collection<? extends ParamDefinition> agentClassDefs) {
	super(agentClassDefs);
    }

    @Override
    public void beforeCollect(BeforeMessage message) {
	getCollectable().clear();
    }

    @Override
    public void collect(CollectMessage message) {
	Entity agent = (Entity) message.getSimObject();

	if (!agent.has(SpeciesDefinition.class)) {
	    return;
	}
	SpeciesDefinition definition = agent.get(SpeciesDefinition.class);

	PopulationData classData = getData(definition);

	if (classData == null) {
	    classData = new PopulationData();
	}

	classData.totalCount++;

	if (!agent.has(Growing.class) || !agent.has(LifeCycling.class)) {
	    return;
	}
	Growing growing = agent.get(Growing.class);
	LifeCycling lifeCycling = agent.get(LifeCycling.class);

	Amount<Mass> biomass = growing.getBiomass();
	classData.totalMass += biomass.doubleValue(UnitConstants.BIOMASS);

	// fish is reproductive
	if (lifeCycling.isReproductive()) {
	    classData.reproductiveCount++;
	    classData.reproductiveMass += biomass.doubleValue(UnitConstants.BIOMASS);
	}
	// fish is juvenile
	else if (lifeCycling.getPhase() == Phase.JUVENILE) {
	    classData.juvenileCount++;
	    classData.juvenileMass += biomass.doubleValue(UnitConstants.BIOMASS);
	}
    }

    @Override
    protected PopulationData createCollectable(ParamDefinition definition) {
	return new PopulationData();
    }

    /**
     * Population data for a class of agents.
     * <p>
     * Data consists of counts and accumulated mass for total, juvenile,
     * reproductive agents.
     * 
     * @author mey
     * 
     */
    // getters used in reflection by mason GUI
    @SuppressWarnings("unused")
    public static class PopulationData implements Collectable, Proxiable {
	private static final long serialVersionUID = 1L;

	private static final List<String> HEADERS = Arrays.asList("total_count", "juvenile_count", "reproductive_count",
		"total_mass_" + UnitConstants.BIOMASS, "juvenile_mass_" + UnitConstants.BIOMASS,
		"reproductive_mass_" + UnitConstants.BIOMASS);

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
	    return Arrays.asList(totalCount, juvenileCount, reproductiveCount, totalMass, juvenileMass,
		    reproductiveMass);
	}

	@Override
	public int getColumnCount() {
	    return HEADERS.size();
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