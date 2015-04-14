package de.zmt.kitt.sim.engine.output;

import java.util.*;
import java.util.logging.Logger;

import sim.display.GUIState;
import sim.engine.*;
import sim.portrayal.*;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.*;
import de.zmt.io.CsvWriter;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.engine.agent.Agent;
import de.zmt.kitt.sim.params.def.EnvironmentDefinition;
import de.zmt.kitt.util.UnitConstants;
import de.zmt.sim.portrayal.inspector.CombinedInspector;

/**
 * Provides continuous output within the GUI via {@link Inspector} and file.
 * 
 * @author cmeyer
 * 
 */
// TODO file output
public class KittOutput implements Steppable, ProvidesInspector, Proxiable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittOutput.class
	    .getName());

    private static final String AGE_DATA_PREFIX = "age_";
    private static final String HABITAT_DATA_PREFIX = "habitat_";
    private static final String POPULATION_DATA_PREFIX = "population_";

    private final Environment environment;

    private final Collector populationDataCollector = new PopulationDataCollector();
    private final Collector ageDataCollector = new AgeDataCollector();
    private final Collector stayDurationsCollector = new StayDurationsCollector();

    private final CsvWriter writer;

    public KittOutput(Environment environment, String outputPath) {
	this.environment = environment;
	writer = createWriter(outputPath);
    }

    private CsvWriter createWriter(String path) {
	// TODO
	return null;
    }

    @Override
    public void step(SimState state) {
	KittSim sim = (KittSim) state;

	EnvironmentDefinition def = sim.getParams().getEnvironmentDefinition();
	long steps = sim.schedule.getSteps();
	Collection<Collector> collectors = new LinkedList<Collector>();

	if (steps % def.getOutputAgeInterval() == 0) {
	    collectors.add(returnCleared(ageDataCollector));
	}

	if (steps % def.getOutputPopulationInterval() == 0) {
	    collectors.add(returnCleared(populationDataCollector));
	}

	// collect stay duration in every step
	collectors.add(stayDurationsCollector);
	collectData(collectors);

	// TODO write data to file
    }

    /** @return cleared collector */
    private Collector returnCleared(Collector collector) {
	collector.clear();
	return collector;
    }

    private void collectData(Collection<Collector> collectors) {
	if (collectors.isEmpty()) {
	    return;
	}

	Bag agents = environment.getAgents();
	for (Object obj : agents) {
	    if (!(obj instanceof Agent)) {
		continue;
	    }

	    for (Collector collector : collectors) {
		Agent agent = (Agent) obj;

		// send habitat message
		if (StayDurationsCollector.class.isAssignableFrom(collector
			.getClass())) {
		    Habitat habitat = environment.obtainHabitat(agent
			    .getPosition());
		    collector.collect(agent,
			    new StayDurationsCollector.HabitatMessage(habitat));
		}
		// default case: send no message
		else {
		    collector.collect(agent, null);
		}
	    }
	}
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new CombinedInspector(new SimpleInspector(this, state, name),
		Inspector.getInspector(environment, state,
			Environment.class.getSimpleName()));
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public Collector getPopulationData() {
	    return populationDataCollector;
	}

	public Collector getAgeData() {
	    return ageDataCollector;
	}

	public Collector getStayDurations() {
	    return stayDurationsCollector;
	}

	public String nameStayDurations() {
	    return "StayDurations_" + UnitConstants.SIMULATION_TIME;
	}
    }
}
