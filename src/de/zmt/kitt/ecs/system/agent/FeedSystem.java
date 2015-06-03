package de.zmt.kitt.ecs.system.agent;

import static javax.measure.unit.NonSI.HOUR;

import java.util.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import sim.util.Double2D;
import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.ecs.component.agent.Metabolizing.ActivityType;
import de.zmt.kitt.ecs.component.environment.*;
import de.zmt.kitt.ecs.component.environment.FoodField.FoundFood;
import de.zmt.kitt.ecs.system.AbstractAgentSystem;
import de.zmt.kitt.sim.KittSim;
import de.zmt.kitt.sim.params.def.*;
import de.zmt.kitt.storage.Compartment.Type;
import de.zmt.kitt.util.UnitConstants;
import de.zmt.util.AmountUtil;
import ecs.*;

/**
 * Let entities retrieve available food at their current position and trigger
 * ingestion for the desired amount.
 * 
 * @author cmeyer
 * 
 */
public class FeedSystem extends AbstractAgentSystem {

    /** {@link ActivityType}s during which the fish is feeding. */
    private static final Collection<ActivityType> ACTIVITIES_ALLOWING_FEEDING = Arrays
	    .asList(ActivityType.FORAGING);

    private static final double DESIRED_EXCESS_SMR_VALUE = 5;
    /**
     * Excess desired storage capacity on SMR:<br>
     * {@value #DESIRED_EXCESS_SMR_VALUE}h
     * <p>
     * Fish will be hungry until desired excess is achieved.
     */
    private static final Amount<Duration> DESIRED_EXCESS_SMR = Amount.valueOf(
	    DESIRED_EXCESS_SMR_VALUE, HOUR);

    public FeedSystem(KittSim sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	Metabolizing metabolizing = entity.get(Metabolizing.class);
	Compartments compartments = entity.get(Compartments.class);

	boolean hungry = computeIsHungry(
		compartments.getStorageAmount(Type.EXCESS),
		metabolizing.getStandardMetabolicRate());
	metabolizing.setHungry(hungry);

	// only start feeding if hungry and in a feeding mood
	if (!hungry
		|| !ACTIVITIES_ALLOWING_FEEDING.contains(metabolizing
			.getActivityType())) {
	    return;
	}

	EnvironmentDefinition environmentDefinition = environment
		.get(EnvironmentDefinition.class);
	Double2D mapPosition = environmentDefinition.worldToMap(entity.get(
		Moving.class).getPosition());
	SpeciesDefinition speciesDefinition = entity
		.get(SpeciesDefinition.class);
	Amount<Length> accessibleRadius = speciesDefinition
		.getAccessibleForagingRadius();

	// calculate available food from density
	FoundFood foundFood = environment.get(FoodField.class)
		.findAvailableFood(mapPosition, accessibleRadius,
			environmentDefinition);

	Amount<Mass> rejectedFood = feed(foundFood.getAvailableFood(), entity
		.get(Growing.class).getBiomass(), metabolizing,
		speciesDefinition, compartments);

	// call back to return rejected food
	foundFood.returnRejected(rejectedFood);
    }

    /**
     * @see #DESIRED_EXCESS_SMR
     * @return True until desired excess amount is achieved
     */
    private boolean computeIsHungry(Amount<Energy> excessAmount,
	    Amount<Power> standardMetabolicRate) {
	Amount<Energy> desiredExcessAmount = DESIRED_EXCESS_SMR.times(
		standardMetabolicRate).to(excessAmount.getUnit());

	return desiredExcessAmount.isGreaterThan(excessAmount);
    }

    /**
     * Offer available food for digestion. The remaining amount that was
     * rejected is returned. Food is rejected if the fish is not hungry or its
     * storage limitations exceeded.
     * 
     * @param availableFood
     * @param biomass
     * @param metabolizing
     * @param speciesDefinition
     * @param compartments
     * @return rejectedFood
     */
    private Amount<Mass> feed(Amount<Mass> availableFood, Amount<Mass> biomass,
	    Metabolizing metabolizing, SpeciesDefinition speciesDefinition,
	    Compartments compartments) {
	Amount<Mass> rejectedFood;

	if (availableFood != null && availableFood.getEstimatedValue() > 0) {
	    Amount<Energy> energyToIngest = computeEnergyToIngest(
		    availableFood, biomass, speciesDefinition);
	    // transfer energy to gut
	    Amount<Energy> rejectedEnergy = compartments.add(energyToIngest)
		    .getRejected();
	    // convert rejected energy back to mass
	    rejectedFood = rejectedEnergy.divide(
		    speciesDefinition.getEnergyContentFood()).to(
		    UnitConstants.FOOD);
	    metabolizing
		    .setIngestedEnergy(energyToIngest.minus(rejectedEnergy));
	}
	// fish cannot feed, nothing ingested
	else {
	    rejectedFood = availableFood;
	    metabolizing.setIngestedEnergy(AmountUtil
		    .zero(UnitConstants.CELLULAR_ENERGY));
	}

	return rejectedFood;
    }

    private Amount<Energy> computeEnergyToIngest(Amount<Mass> availableFood,
	    Amount<Mass> biomass, SpeciesDefinition speciesDefinition) {
	// ingest desired amount and reject the rest
	// consumption rate depends on fish biomass
	Amount<Mass> foodConsumption = biomass.times(speciesDefinition
		.getMaxConsumptionPerStep());
	// fish cannot consume more than available...
	Amount<Mass> foodToIngest = AmountUtil.min(foodConsumption,
		availableFood);
	return foodToIngest.times(speciesDefinition.getEnergyContentFood()).to(
		UnitConstants.CELLULAR_ENERGY);
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Metabolizing.class,
		Growing.class, Compartments.class, Moving.class);
    }

}
