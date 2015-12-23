package sim.engine.output;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.*;
import de.zmt.util.*;
import sim.display.GUIState;
import sim.engine.*;
import sim.engine.output.StayDurationsCollector.HabitatMessage;
import sim.engine.output.message.*;
import sim.params.KittParams;
import sim.params.def.*;
import sim.portrayal.Inspector;
import sim.util.Double2D;

/**
 * Provides continuous output within the GUI via {@link Inspector} and file.
 * 
 * @author mey
 * 
 */
public class KittOutput extends Output {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittOutput.class.getName());

    private static final String GENERAL_PREFIX = "kitt_results_";
    private static final String POPULATION_DATA_PREFIX = "_population";
    private static final String AGE_DATA_PREFIX = "_age";
    // private static final String HABITAT_DATA_PREFIX = "_habitat";

    public KittOutput(File outputDirectory, KittParams params) {
	super();
	outputDirectory.mkdir();
	int fileIndex = CsvWriterUtil.findNextIndex(outputDirectory, GENERAL_PREFIX);

	File outputAgeFile = CsvWriterUtil.generateWriterFile(outputDirectory, GENERAL_PREFIX, fileIndex,
		AGE_DATA_PREFIX);
	File outputPopulationFile = CsvWriterUtil.generateWriterFile(outputDirectory, GENERAL_PREFIX, fileIndex,
		POPULATION_DATA_PREFIX);

	Set<SpeciesDefinition> speciesDefs = new HashSet<>(params.getSpeciesDefs());
	Collector ageDataCollector = new WritingCollector(new AgeDataCollector(speciesDefs), outputAgeFile);
	Collector populationDataCollector = new WritingCollector(new PopulationDataCollector(speciesDefs),
		outputPopulationFile);
	Collector stayDurationsCollector = new StayDurationsCollector(speciesDefs);

	EnvironmentDefinition envDefinition = params.getEnvironmentDefinition();
	addCollector(ageDataCollector, envDefinition.getOutputAgeInterval());
	addCollector(populationDataCollector, envDefinition.getOutputPopulationInterval());
	addCollector(stayDurationsCollector);
    }

    /**
     * Creates a message for every simulation agent.
     * {@link StayDurationsCollector} will receive {@link HabitatMessage}s.
     */
    @Override
    protected Iterable<? extends CollectMessage> createCollectMessages(final Collector recipient, SimState state) {
	Entity environment = ((Kitt) state).getEnvironment();
	Collection<?> agents = environment.get(AgentWorld.class).getAgents();
	Collection<CollectMessage> messages = new ArrayList<>(agents.size());

	for (Object agent : agents) {
	    Entity agentEntity = (Entity) agent;
	    if (recipient instanceof StayDurationsCollector) {
		messages.add(createHabitatMessage(agentEntity, environment));

	    } else {
		messages.add(new DefaultCollectMessage<>(agentEntity));
	    }
	}
	return messages;
    }

    /**
     * Creates a {@link HabitatMessage} with the give data.
     * 
     * @param agent
     * @param environment
     * @return {@link HabitatMessage} with given data
     */
    private static CollectMessage createHabitatMessage(final Entity agent, final Entity environment) {
	return new StayDurationsCollector.HabitatMessage() {

	    @Override
	    public Object getSimObject() {
		return agent;
	    }

	    @Override
	    public Habitat getHabitat() {
		Double2D position = agent.get(Moving.class).getPosition();
		HabitatMap habitatMap = environment.get(HabitatMap.class);
		WorldToMapConverter converter = environment.get(EnvironmentDefinition.class);
		return habitatMap.obtainHabitat(position, converter);
	    }
	};
    }

    /**
     * Adds agent world and simulation time components to super class inspector.
     */
    @Override
    public Inspector provideInspector(GUIState state, String name) {
	Inspector inspector = super.provideInspector(state, name);
	for (Component component : ((Kitt) state.state).getEnvironment()
		.get(Arrays.<Class<? extends Component>> asList(AgentWorld.class, SimulationTime.class))) {
	    inspector.add(Inspector.getInspector(component, state, name));
	}

	return inspector;
    }
}
