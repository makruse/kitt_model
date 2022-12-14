package de.zmt.output;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import de.zmt.ecs.component.agent.LifeCycling;
import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.KittParams;
import de.zmt.params.SpeciesDefinition;
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
    private static final Path LENGTH_SUBPATH = Paths.get("length");
    private static final Path STAY_SUBPATH = Paths.get("stay");

    public KittOutput(Path outputPath, KittParams params, HabitatMap habitatMap, FoodMap foodMap) {
        super(outputPath);

        Collection<SpeciesDefinition> speciesDefs = params.getSpeciesDefs();
        EnvironmentDefinition envDefinition = params.getEnvironmentDefinition();
        StrategyCollector<?> ageDataCollector = AgeData.createCollector(speciesDefs);
        StrategyCollector<?> lengthDataCollector = LengthData.createCollector(speciesDefs);
        StrategyCollector<?> populationDataCollector = PopulationData.createCollector(speciesDefs);
        StrategyCollector<LocationStayDurations> stayDurationsCollector = LocationStayDurations
                .createCollector(envDefinition.getStepDuration(), habitatMap, foodMap);
        StrategyCollector<LifeCyclingData> lifeCyclingDataCollector = LifeCyclingData.createCollector(habitatMap,foodMap);

        if(envDefinition.ageOutput())
        addCollector(ageDataCollector, CollectorOption.writer(AGE_SUBPATH),
                     CollectorOption.name(AgeData.class.getSimpleName()),
                     CollectorOption.interval(convertToStepInterval(envDefinition.getOutputAgeInterval())));

        if(envDefinition.lengthOutput())
        addCollector(lengthDataCollector, CollectorOption.writer(LENGTH_SUBPATH),
                CollectorOption.name(LengthData.class.getSimpleName()),
                CollectorOption.interval(convertToStepInterval(envDefinition.getOutputLengthInterval())));

        if(envDefinition.populationOutput())
        addCollector(populationDataCollector, CollectorOption.writer(POPULATION_SUBPATH),
                     CollectorOption.name(PopulationData.class.getSimpleName()),
                     CollectorOption.interval(convertToStepInterval(envDefinition.getOutputPopulationInterval())));

        if(envDefinition.stayOutput())
        addCollector(stayDurationsCollector,
                     // need to collect on every step but write only at the given one
                     CollectorOption.writeInterval(convertToStepInterval(envDefinition.getOutputStayDurationsInterval())),
                     CollectorOption.writer(STAY_SUBPATH), CollectorOption.hidden(true));

        if(envDefinition.lifeCyclingOutput())
        addCollector(lifeCyclingDataCollector,
                     CollectorOption.interval(convertToStepInterval(envDefinition.getOutputLifeCycleInterval())),
                     CollectorOption.name("LifeCyclingData"),
                     CollectorOption.writer(Paths.get("lifeCycling")));
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
