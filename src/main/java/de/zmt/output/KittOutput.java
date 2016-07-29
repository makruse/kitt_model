package de.zmt.output;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

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

    public KittOutput(Path outputPath, KittParams params) {
        super(outputPath);

        Collection<SpeciesDefinition> speciesDefs = params.getSpeciesDefs();
        StrategyCollector<?> ageDataCollector = AgeData.createCollector(speciesDefs);
        StrategyCollector<?> populationDataCollector = PopulationData.createCollector(speciesDefs);
        StrategyCollector<?> stayDurationsCollector = HabitatStayDurations.createCollector(speciesDefs);
        EnvironmentDefinition envDefinition = params.getEnvironmentDefinition();

        addCollector(ageDataCollector, CollectorOption.writer(AGE_SUBPATH),
                CollectorOption.name(AgeData.class.getSimpleName()),
                CollectorOption.interval(convertToStepInterval(envDefinition.getOutputAgeInterval())));
        addCollector(populationDataCollector, CollectorOption.writer(POPULATION_SUBPATH),
                CollectorOption.name(PopulationData.class.getSimpleName()),
                CollectorOption.interval(convertToStepInterval(envDefinition.getOutputPopulationInterval())));
        addCollector(stayDurationsCollector, CollectorOption.name(HabitatStayDurations.class.getSimpleName()));
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
