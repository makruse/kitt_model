package de.zmt.output;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.environment.AgentWorld;
import de.zmt.output.message.CollectMessage;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.KittParams;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.portrayal.Inspector;

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
	putInterval(ageDataCollector, convertToStepInterval(envDefinition.getOutputAgeInterval()));
	putInterval(populationDataCollector, convertToStepInterval(envDefinition.getOutputPopulationInterval()));
    }

    /**
     * 
     * @param simulationTime
     * @return equivalent step interval
     */
    private static int convertToStepInterval(Amount<Duration> simulationTime) {
	return (int) simulationTime.to(UnitConstants.SIMULATION_TIME).getExactValue();
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
}
