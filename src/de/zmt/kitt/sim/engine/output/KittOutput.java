package de.zmt.kitt.sim.engine.output;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import sim.engine.SimState;
import de.zmt.kitt.sim.Habitat;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.params.KittParams;
import de.zmt.kitt.sim.params.def.*;
import de.zmt.sim.engine.ParamAgent;
import de.zmt.sim.engine.output.*;
import de.zmt.sim.engine.output.Collector.CollectMessage;
import de.zmt.sim.util.CsvWriterUtil;

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
    private final Environment environment;

    public static Output create(Environment environment, File outputDirectory,
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

    private KittOutput(List<Collector> collectors, Environment environment,
	    Map<Collector, Integer> intervals) {
	super(collectors, Collections.singletonList(environment), intervals);
	this.environment = environment;
    }

    @Override
    protected Collection<?> obtainAgents() {
	return environment.getAgents();
    }

    @Override
    protected CollectMessage obtainCollectMessage(Collector recipient,
	    final ParamAgent agent, SimState state) {
	if (recipient instanceof StayDurationsCollector) {
	    return new StayDurationsCollector.HabitatMessage() {

		@Override
		public ParamAgent getAgent() {
		    return agent;
		}

		@Override
		public Habitat getHabitat() {
		    return environment.obtainHabitat(agent.getPosition());
		}
	    };
	}
	return super.obtainCollectMessage(recipient, agent, state);
    }

}
