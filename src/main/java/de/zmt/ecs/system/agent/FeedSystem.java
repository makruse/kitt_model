package de.zmt.ecs.system.agent;

import static javax.measure.unit.NonSI.HOUR;

import java.util.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.FoodMap.FoundFood;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.storage.Compartment.Type;
import de.zmt.util.*;
import sim.engine.Kitt;
import sim.params.def.*;
import sim.util.Double2D;

/**
 * Let entities retrieve available food at their current position and trigger
 * ingestion for the desired amount.
 * 
 * @author mey
 * 
 */
public class FeedSystem extends AgentSystem {

    /** {@link BehaviorMode}s during which the fish is feeding. */
    private static final Collection<BehaviorMode> ACTIVITIES_ALLOWING_FEEDING = Arrays.asList(BehaviorMode.FORAGING);

    private static final double DESIRED_EXCESS_RMR_VALUE = 5;
    /**
     * Excess desired storage capacity on RMR:<br>
     * {@value #DESIRED_EXCESS_RMR_VALUE}h
     * <p>
     * Fish will be hungry until desired excess is achieved.
     */
    private static final Amount<Duration> DESIRED_EXCESS_RMR = Amount.valueOf(DESIRED_EXCESS_RMR_VALUE, HOUR);

    public FeedSystem(Kitt sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	Metabolizing metabolizing = entity.get(Metabolizing.class);
	Compartments compartments = entity.get(Compartments.class);

	boolean hungry = computeIsHungry(compartments.getStorageAmount(Type.EXCESS),
		metabolizing.getRestingMetabolicRate());
	metabolizing.setHungry(hungry);

	// only start feeding if hungry and in a feeding mood
	if (!hungry || !ACTIVITIES_ALLOWING_FEEDING.contains(metabolizing.getBehaviorMode())) {
	    return;
	}

	// fetch necessary components and data
	EnvironmentDefinition environmentDefinition = getEnvironment().get(EnvironmentDefinition.class);
	Double2D worldPosition = entity.get(Moving.class).getPosition();
	SpeciesDefinition speciesDefinition = entity.get(SpeciesDefinition.class);
	Amount<Length> accessibleRadius = speciesDefinition.getAccessibleForagingRadius();

	// calculate available food from density
	FoundFood foundFood = getEnvironment().get(FoodMap.class).findAvailableFood(worldPosition, accessibleRadius,
		environmentDefinition);

	Amount<Mass> rejectedFood = feed(foundFood.getAvailableFood(), entity.get(Growing.class).getBiomass(),
		metabolizing, speciesDefinition, compartments);

	// call back to return rejected food
	foundFood.returnRejected(rejectedFood);

	// transfer digested in compartments
	boolean reproductive = entity.get(LifeCycling.class).isReproductive();
	entity.get(Compartments.class).transferDigested(reproductive);
    }

    /**
     * @see #DESIRED_EXCESS_RMR
     * @param excessAmount
     *            amount of excess energy
     * @param restingMetabolicRate
     * @return True until desired excess amount is achieved
     */
    private boolean computeIsHungry(Amount<Energy> excessAmount, Amount<Power> restingMetabolicRate) {
	Amount<Energy> desiredExcessAmount = DESIRED_EXCESS_RMR.times(restingMetabolicRate).to(excessAmount.getUnit());

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
    private Amount<Mass> feed(Amount<Mass> availableFood, Amount<Mass> biomass, Metabolizing metabolizing,
	    SpeciesDefinition speciesDefinition, Compartments compartments) {
	Amount<Mass> rejectedFood;

	if (availableFood != null && availableFood.getEstimatedValue() > 0) {
	    Amount<Energy> energyToIngest = computeEnergyToIngest(availableFood, biomass, speciesDefinition);
	    // transfer energy to gut
	    Amount<Energy> rejectedEnergy = compartments.add(energyToIngest).getRejected();
	    // convert rejected energy back to mass
	    rejectedFood = rejectedEnergy.divide(speciesDefinition.getEnergyContentFood()).to(UnitConstants.FOOD);
	    metabolizing.setIngestedEnergy(energyToIngest.minus(rejectedEnergy));
	}
	// fish cannot feed, nothing ingested
	else {
	    rejectedFood = availableFood;
	    metabolizing.setIngestedEnergy(AmountUtil.zero(UnitConstants.CELLULAR_ENERGY));
	}

	return rejectedFood;
    }

    private Amount<Energy> computeEnergyToIngest(Amount<Mass> availableFood, Amount<Mass> biomass,
	    SpeciesDefinition speciesDefinition) {
	// ingest desired amount and reject the rest
	// consumption rate depends on fish biomass
	Amount<Mass> foodConsumption = biomass.times(speciesDefinition.getMaxIngestionPerStep());
	// fish cannot consume more than available...
	Amount<Mass> foodToIngest = AmountUtil.min(foodConsumption, availableFood);
	return foodToIngest.times(speciesDefinition.getEnergyContentFood()).to(UnitConstants.CELLULAR_ENERGY);
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Metabolizing.class, Growing.class, Compartments.class,
		Moving.class, LifeCycling.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(
		// for position
		MoveSystem.class,
		// for age in delay calculation of digesta entering gut
		AgeSystem.class);
    }

}
