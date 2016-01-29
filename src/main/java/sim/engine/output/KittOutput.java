package sim.engine.output;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.component.environment.AgentWorld;
import de.zmt.ecs.component.environment.SimulationTime;
import sim.display.GUIState;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.engine.output.message.CollectMessage;
import sim.params.KittParams;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.inspector.CombinedInspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * Provides continuous output within the GUI via {@link Inspector} and file.
 * 
 * @author mey
 * 
 */
public class KittOutput extends Output implements ProvidesInspector {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittOutput.class.getName());

    private static final String AGE_DATA_TITLE = "age";
    // private static final String HABITAT_DATA_TITLE = "habitat";
    private static final String POPULATION_DATA_TITLE = "population";

    public KittOutput(Path outputPath, KittParams params) {
	super(outputPath);

	Set<SpeciesDefinition> speciesDefs = new HashSet<>(params.getSpeciesDefs());
	AgeDataCollector ageDataCollector = new AgeDataCollector(speciesDefs);
	PopulationDataCollector populationDataCollector = new PopulationDataCollector(speciesDefs);
	StayDurationsCollector stayDurationsCollector = new StayDurationsCollector(speciesDefs);

	addCollectorAndWriter(ageDataCollector, AGE_DATA_TITLE);
	addCollectorAndWriter(populationDataCollector, POPULATION_DATA_TITLE);
	addCollector(stayDurationsCollector);

	EnvironmentDefinition envDefinition = params.getEnvironmentDefinition();
	putInterval(ageDataCollector, envDefinition.getOutputAgeInterval());
	putInterval(populationDataCollector, envDefinition.getOutputPopulationInterval());
    }

    /** Creates a message for every simulation agent. */
    @Override
    protected Iterable<? extends CollectMessage> createDefaultCollectMessages(SimState state) {
	Entity environment = ((Kitt) state).getEnvironment();
	Collection<?> agents = environment.get(AgentWorld.class).getAgents();
	Collection<EntityCollectMessage> messages = new ArrayList<>(agents.size());

	for (Object agent : agents) {
	    Entity agentEntity = (Entity) agent;
	    messages.add(new EntityCollectMessage(agentEntity));
	}
	return messages;
    }

    /**
     * Adds agent world and simulation time components to super class inspector.
     */
    @Override
    public Inspector provideInspector(GUIState state, String name) {
	Inspector inspector = new CombinedInspector(new SimpleInspector(this, state, name));
	for (Component component : ((Kitt) state.state).getEnvironment()
		.get(Arrays.<Class<? extends Component>> asList(AgentWorld.class, SimulationTime.class))) {
	    inspector.add(Inspector.getInspector(component, state, name));
	}

	return inspector;
    }
}
