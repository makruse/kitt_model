package de.zmt.kitt.sim.engine.output;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import sim.display.GUIState;
import sim.engine.*;
import sim.portrayal.*;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.*;
import de.zmt.io.*;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.engine.agent.Agent;
import de.zmt.kitt.sim.params.def.*;
import de.zmt.sim.portrayal.inspector.CombinedInspector;

/**
 * Provides continuous output within the GUI via {@link Inspector} and file.
 * 
 * @author cmeyer
 * 
 */
public class KittOutput implements Steppable, ProvidesInspector, Proxiable,
	Closeable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittOutput.class
	    .getName());

    private static final String GENERAL_PREFIX = "kitt_results_";
    private static final String POPULATION_DATA_PREFIX = "_population";
    private static final String AGE_DATA_PREFIX = "_age";
    private static final String HABITAT_DATA_PREFIX = "_habitat";
    private static final int DIGITS_COUNT = 5;

    private final Environment environment;

    private final Collector populationDataCollector;
    private final Collector ageDataCollector;
    private final Collector stayDurationsCollector;

    private final SteppedCsvWriter populationDataWriter;
    private final SteppedCsvWriter ageDataWriter;

    public KittOutput(Environment environment, File outputDirectory,
	    Collection<SpeciesDefinition> speciesDefinitions) {
	this.environment = environment;
	this.populationDataCollector = new PopulationDataCollector(
		speciesDefinitions);
	this.ageDataCollector = new AgeDataCollector(speciesDefinitions);
	this.stayDurationsCollector = new StayDurationsCollector(
		speciesDefinitions);

	// create output directory
	outputDirectory.mkdir();
	int fileIndex = findNextIndex(outputDirectory, GENERAL_PREFIX);

	// no assignment of final variables within try / catch possible
	SteppedCsvWriter tempPopulationDataWriter = null;
	SteppedCsvWriter tempAgeDataWriter = null;
	try {
	    tempPopulationDataWriter = new SteppedCsvWriter(
		    populationDataCollector, generateWriterFile(
			    outputDirectory, GENERAL_PREFIX, fileIndex,
			    POPULATION_DATA_PREFIX));
	    tempAgeDataWriter = new SteppedCsvWriter(ageDataCollector,
		    generateWriterFile(outputDirectory, GENERAL_PREFIX,
			    fileIndex, AGE_DATA_PREFIX));
	} catch (IOException e) {
	    logger.log(Level.WARNING,
		    "No file output. A file could not be created.", e);
	}

	populationDataWriter = tempPopulationDataWriter;
	ageDataWriter = tempAgeDataWriter;
    }

    /**
     * Finds next index for files starting with {@code prefixBeforeIndex} to be
     * used in output.
     * 
     * @param directory
     * @param prefixBeforeIndex
     * @return index after the last already present in {@code directory}.
     */
    private static int findNextIndex(File directory,
	    final String prefixBeforeIndex) {
	// get list of files from former simulation runs
	File[] files = directory.listFiles(new FilenameFilter() {

	    @Override
	    public boolean accept(File dir, String name) {
		if (name.startsWith(prefixBeforeIndex)) {
		    return true;
		}
		return false;
	    }
	});

	// no other files present, first index is 0
	if (files.length <= 0) {
	    return 0;
	}

	// get last existing index from file list
	Arrays.sort(files);
	String lastFileName = files[files.length - 1].getName();
	// extract index from last file in list
	int lastIndex = Integer.parseInt(lastFileName.substring(
		prefixBeforeIndex.length(), prefixBeforeIndex.length()
			+ DIGITS_COUNT));

	return lastIndex + 1;
    }

    /**
     * @param directory
     * @param prefixBeforeIndex
     * @param index
     * @param prefixAfterIndex
     * @return {@link File} for {@link CsvWriter}
     */
    private static File generateWriterFile(File directory,
	    String prefixBeforeIndex, int index, String prefixAfterIndex) {
	return new File(directory, prefixBeforeIndex
		// next integer with leading zeroes
		+ String.format("%0" + DIGITS_COUNT + "d", index)
		+ prefixAfterIndex + CsvWriter.FILENAME_SUFFIX);
    }

    @Override
    public void step(SimState state) {
	KittSim sim = (KittSim) state;

	// collect stay duration in every step
	collectData(stayDurationsCollector);

	EnvironmentDefinition def = sim.getParams().getEnvironmentDefinition();
	long steps = sim.schedule.getSteps();

	if (steps % def.getOutputAgeInterval() == 0) {
	    clearCollectWriteData(ageDataCollector, ageDataWriter, steps);
	}

	if (steps % def.getOutputPopulationInterval() == 0) {
	    clearCollectWriteData(populationDataCollector,
		    populationDataWriter, steps);
	}

    }

    private void clearCollectWriteData(Collector collector,
	    SteppedCsvWriter writer, long steps) {
	collector.clear();
	collectData(collector);

	if (writer == null) {
	    return;
	}

	try {
	    writer.writeData(steps);
	} catch (IOException e) {
	    logger.warning("Failed to write data from " + collector + " with "
		    + writer);
	}
    }

    private void collectData(Collector collector) {
	Bag agents = environment.getAgents();
	for (Object obj : agents) {
	    if (!(obj instanceof Agent)) {
		continue;
	    }

	    Agent agent = (Agent) obj;

	    // send habitat message
	    if (collector instanceof StayDurationsCollector) {
		Habitat habitat = environment
			.obtainHabitat(agent.getPosition());
		collector.collect(agent,
			new StayDurationsCollector.HabitatMessage(habitat));
	    }
	    // default case: send no message
	    else {
		collector.collect(agent, null);
	    }
	}
    }

    @Override
    public void close() throws IOException {
	populationDataWriter.close();
	ageDataWriter.close();
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
    }
}
