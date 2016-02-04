package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.FoodMap.FoundFood;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.agent.move.MoveSystem;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;
import sim.util.Double2D;

/**
 * Let entities retrieve available food at their current position and trigger
 * ingestion for the desired amount. This system also makes {@link Compartments}
 * transfer digested energy from gut while subtracting which was consumed.
 * 
 * @author mey
 * 
 */
public class FeedSystem extends AgentSystem {

    /** {@link BehaviorMode}s during which the fish is feeding. */
    private static final Collection<BehaviorMode> ACTIVITIES_ALLOWING_FEEDING = Arrays.asList(BehaviorMode.FORAGING);

    public FeedSystem(Kitt sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	Metabolizing metabolizing = entity.get(Metabolizing.class);
	Compartments compartments = entity.get(Compartments.class);

	boolean hungry = compartments.computeIfHungry();
	metabolizing.setHungry(hungry);

	// only start feeding if hungry and in a feeding mood
	if (hungry && ACTIVITIES_ALLOWING_FEEDING.contains(metabolizing.getBehaviorMode())) {
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
	}
	// agent did not feed: nothing ingested
	else {
	    metabolizing.setIngestedEnergy(AmountUtil.zero(UnitConstants.CELLULAR_ENERGY));
	}
    }

    /**
     * Offer available food for digestion. The remaining amount that was
     * rejected is returned. Food is rejected if the fish is not hungry or its
     * storage limitations exceeded.
     * 
     * @param availableFood
     *            the available food to consume within the accessible
     *            environment
     * @param biomass
     *            biomass of the agent
     * @param metabolizing
     * @param speciesDefinition
     * @param compartments
     * @return rejectedFood food that cannot be consumed due to max ingestion
     *         rate and gut capacity
     */
    private static Amount<Mass> feed(Amount<Mass> availableFood, Amount<Mass> biomass, Metabolizing metabolizing,
	    SpeciesDefinition speciesDefinition, Compartments compartments) {
	Amount<Mass> rejectedFood;

	if (availableFood.getEstimatedValue() > 0) {
	    // consumption rate depends on fish biomass
	    Amount<Mass> maxIngestionAmount = biomass
		    .times(speciesDefinition.getMaxIngestionRate().times(EnvironmentDefinition.STEP_DURATION))
		    .to(UnitConstants.BIOMASS);
	    // fish cannot consume more than its max ingestion rate
	    Amount<Mass> foodToIngest = AmountUtil.min(maxIngestionAmount, availableFood);
	    Amount<Energy> energyToIngest = foodToIngest.times(speciesDefinition.getEnergyContentFood())
		    .to(UnitConstants.CELLULAR_ENERGY);
	    // transfer energy to gut
	    Amount<Energy> rejectedEnergy = compartments.add(energyToIngest).getRejected();
	    /*
	     * Convert rejected energy back to mass and add difference between
	     * available food and the maximum ingestable amount.
	     */
	    rejectedFood = rejectedEnergy.divide(speciesDefinition.getEnergyContentFood()).to(UnitConstants.FOOD)
		    .plus(availableFood.minus(foodToIngest));
	    metabolizing.setIngestedEnergy(energyToIngest.minus(rejectedEnergy));
	}
	// fish cannot feed, nothing ingested
	else {
	    rejectedFood = availableFood;
	    metabolizing.setIngestedEnergy(AmountUtil.zero(UnitConstants.CELLULAR_ENERGY));
	}

	return rejectedFood;
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
		AgeSystem.class,
		// for subtracting consumed energy
		ConsumeSystem.class);
    }

}
