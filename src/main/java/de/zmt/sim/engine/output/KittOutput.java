package de.zmt.sim.engine.output;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.*;
import de.zmt.sim.engine.output.Collector.CollectMessage;
import de.zmt.sim.params.KittParams;
import de.zmt.sim.params.def.*;
import de.zmt.util.*;
import sim.engine.SimState;
import sim.portrayal.Inspector;

/**
 * Provides continuous output within the GUI via {@link Inspector} and file.
 * 
 * @author cmeyer
 * 
 */
public class KittOutput extends Output {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittOutput.class
	    .getName());

    private static final String GENERAL_PREFIX = "kitt_results_";
    private static final String POPULATION_DATA_PREFIX = "_population";
    private static final String AGE_DATA_PREFIX = "_age";
    // private static final String HABITAT_DATA_PREFIX = "_habitat";
    private final AgentWorld agentWorld;
    private final HabitatMap habitatMap;
    private final EnvironmentDefinition environmentDefinition;

    public static Output create(Entity environment, File outputDirectory,
	    KittParams params) {
	outputDirectory.mkdir();
	int fileIndex = CsvWriterUtil.findNextIndex(outputDirectory,
		GENERAL_PREFIX);

	File outputAgeFile = CsvWriterUtil.generateWriterFile(outputDirectory,
		GENERAL_PREFIX, fileIndex, AGE_DATA_PREFIX);
	File outputPopulationFile = CsvWriterUtil.generateWriterFile(
		outputDirectory, GENERAL_PREFIX, fileIndex,
		POPULATION_DATA_PREFIX);

	Collection<SpeciesDefinition> speciesDefs = params.getSpeciesDefs();
	AgeDataCollector ageDataCollector = new AgeDataCollector(speciesDefs,
		outputAgeFile);
	PopulationDataCollector populationDataCollector = new PopulationDataCollector(
		speciesDefs, outputPopulationFile);
	StayDurationsCollector stayDurationsCollector = new StayDurationsCollector(
		speciesDefs);
	List<Collector> collectors = Arrays.<Collector> asList(
		ageDataCollector, populationDataCollector,
		stayDurationsCollector);

	EnvironmentDefinition envDefinition = params.getEnvironmentDefinition();
	Map<Collector, Integer> intervals = new HashMap<>();
	intervals.put(ageDataCollector, envDefinition.getOutputAgeInterval());
	intervals.put(populationDataCollector,
		envDefinition.getOutputPopulationInterval());

	return new KittOutput(collectors, environment, intervals);
    }

    private KittOutput(List<Collector> collectors, Entity environment,
	    Map<Collector, Integer> intervals) {
	// AgentWorld and SimulationTime display output in GUI
	super(collectors, Arrays.asList(environment.get(AgentWorld.class),
		environment.get(SimulationTime.class)), intervals);
	this.agentWorld = environment.get(AgentWorld.class);
	this.habitatMap = environment.get(HabitatMap.class);
	this.environmentDefinition = environment
		.get(EnvironmentDefinition.class);
    }

    @Override
    protected Collection<?> obtainAgents() {
	return agentWorld.getAgents();
    }

    @Override
    protected CollectMessage obtainCollectMessage(Collector recipient,
	    final Entity agent, SimState state) {
	if (recipient instanceof StayDurationsCollector) {
	    return new StayDurationsCollector.HabitatMessage() {

		@Override
		public Entity getAgent() {
		    return agent;
		}

		@Override
		public Habitat getHabitat() {
		    return habitatMap.obtainHabitat(agent.get(Moving.class)
			    .getPosition(), environmentDefinition);
		}
	    };
	}
	return super.obtainCollectMessage(recipient, agent, state);
    }
}