package sim.engine.output;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.*;
import de.zmt.util.*;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.output.Collector.CollectMessage;
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

    private final Entity environment;

    public KittOutput(Entity environment, File outputDirectory, KittParams params) {
	super();
	this.environment = environment;

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

    @Override
    protected Collection<?> obtainSimObjects(Collector collector, SimState state) {
	return environment.get(AgentWorld.class).getAgents();
    }

    @Override
    protected CollectMessage obtainCollectMessage(Collector recipient, final Object simObject, SimState state) {
	if (recipient instanceof StayDurationsCollector) {
	    return new StayDurationsCollector.HabitatMessage() {

		@Override
		public Object getSimObject() {
		    return simObject;
		}

		@Override
		public Habitat getHabitat() {
		    Double2D position = ((Entity) simObject).get(Moving.class).getPosition();
		    HabitatMap habitatMap = environment.get(HabitatMap.class);
		    WorldToMapConverter converter = environment.get(EnvironmentDefinition.class);
		    return habitatMap.obtainHabitat(position, converter);
		}
	    };
	}
	return super.obtainCollectMessage(recipient, simObject, state);
    }

    /**
     * Adds agent world and simulation time components to super class inspector.
     */
    @Override
    public Inspector provideInspector(GUIState state, String name) {
	Inspector inspector = super.provideInspector(state, name);
	for (Component component : environment
		.get(Arrays.<Class<? extends Component>> asList(AgentWorld.class, SimulationTime.class))) {
	    inspector.add(Inspector.getInspector(component, state, name));
	}

	return inspector;
    }

}
