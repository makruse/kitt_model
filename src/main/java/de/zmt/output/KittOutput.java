package de.zmt.output;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.output.collector.Collector;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.output.writing.OutputWriter;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.KittParams;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.TimeOfDay;
import de.zmt.util.UnitConstants;
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

    private static final Path POPULATION_SUBPATH = Paths.get("population");
    private static final Path AGE_SUBPATH = Paths.get("age");
    private static final String STAY_SUBPATH_PREFIX = "stay_";

    public KittOutput(Path outputPath, KittParams params, HabitatMap habitatMap) {
        super(outputPath);

        Collection<SpeciesDefinition> speciesDefs = params.getSpeciesDefs();
        EnvironmentDefinition envDefinition = params.getEnvironmentDefinition();
        StrategyCollector<?> ageDataCollector = AgeData.createCollector(speciesDefs);
        StrategyCollector<?> populationDataCollector = PopulationData.createCollector(speciesDefs);

        addCollector(ageDataCollector, CollectorOption.writer(AGE_SUBPATH),
                CollectorOption.name(AgeData.class.getSimpleName()),
                CollectorOption.interval(convertToStepInterval(envDefinition.getOutputAgeInterval())));
        addCollector(populationDataCollector, CollectorOption.writer(POPULATION_SUBPATH),
                CollectorOption.name(PopulationData.class.getSimpleName()),
                CollectorOption.interval(convertToStepInterval(envDefinition.getOutputPopulationInterval())));

        for (TimeOfDay timeOfDay : TimeOfDay.values()) {
            StrategyCollector<LocationStayDurations> stayDurationsCollector = LocationStayDurations.createCollector(
                    habitatMap.getWidth(), habitatMap.getHeight(), envDefinition.getStepDuration(), timeOfDay);
            // first collector running on interval accumulating stays
            addCollector(stayDurationsCollector,
                    CollectorOption.name(LocationStayDurations.class.getSimpleName() + "_" + timeOfDay));
            // second collector for the clearing / writing in intervals
            OutputWriter writer = stayDurationsCollector.getCollectable()
                    .createWriter(getOutputPath().resolve(STAY_SUBPATH_PREFIX + timeOfDay));
            addCollector(Collector.EMPTY,
                    CollectorOption.interval(convertToStepInterval(envDefinition.getOutputStayDurationsInterval())),
                    CollectorOption.writer(writer), CollectorOption.hidden(true));
        }
    }

    /**
     * 
     * @param simulationTime
     * @return equivalent step interval
     */
    private static int convertToStepInterval(Amount<Duration> simulationTime) {
        return (int) simulationTime.to(UnitConstants.SIMULATION_TIME).getExactValue();
    }
}
