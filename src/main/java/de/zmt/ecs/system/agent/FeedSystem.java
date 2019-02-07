package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;

import javax.measure.quantity.*;
import javax.measure.unit.Unit;

import de.zmt.storage.Compartment;
import ec.util.MersenneTwisterFast;
import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.DynamicScheduling;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.FoodMap.FoundFood;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 * Let entities retrieve available food at their current position and trigger
 * ingestion for the desired amount. This system also makes {@link Compartments}
 * transfer digested energy from gut while subtracting which was consumed.
 * <p>
 * <img src="doc-files/gen/FeedSystem.svg" alt= "FeedSystem Activity Diagram">
 * 
 * @author mey
 * 
 */
/*
/*
@formatter:off
@startuml doc-files/gen/FeedSystem.svg

start
if (feeding) then (yes)
	:find available food;
	partition Feed {
	    :compute food to ingest
	    by limiting available food
	    to maximum ingestion rate;
	    :convert food to ingest to energy;
	    partition "Add Energy to Gut" {
	        :ingested energy<
            if (gut is full?) then (yes)
                :ingest nothing>
	        elseif (energy exceeds limit?) then (yes)
	            :ingest up to limit;
            else
	            :ingest everything;
            endif
            :add Digesta with
            ingested amount to gut;
            :return ingested amount>
	    }
	    :subtract ingested energy
	    from food grid;
	}
	else (no)
endif
stop

@enduml
@formatter:on
 */
public class FeedSystem extends AgentSystem {

    private Amount<Frequency> ingestionRate = Amount.valueOf(0.0,UnitConstants.PER_DAY)
            .to(UnitConstants.PER_SIMULATION_TIME);

    private Amount<Mass> desiredFoodAmount = Amount.valueOf(0.0,UnitConstants.FOOD);

    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        Metabolizing metabolizing = entity.get(Metabolizing.class);
        Compartments compartments = entity.get(Compartments.class);
        Entity environment = ((Kitt) state).getEnvironment();
        SpeciesDefinition speciesDefinition = entity.get(SpeciesDefinition.class);
        Kitt kitt = (Kitt) state;
        Amount<Duration> deltaTime = entity.get(DynamicScheduling.class).getDeltaTime();

        computeDesiredFoodAmount(entity.get(Growing.class), compartments, speciesDefinition, kitt.random, deltaTime);

        if (metabolizing.isFeeding()) {
            // fetch necessary components and data
            EnvironmentDefinition environmentDefinition = environment.get(EnvironmentDefinition.class);
            Double2D worldPosition = entity.get(Moving.class).getWorldPosition();
            Amount<Length> accessibleRadius = speciesDefinition.getAccessibleForagingRadius();

            // calculate available food from density
            FoundFood foundFood = environment.get(FoodMap.class).findAvailableFood(worldPosition, accessibleRadius,
                    environmentDefinition);

            Amount<Mass> rejectedFood = feed(foundFood.getAvailableFood(), entity.get(Growing.class).getBiomass(),
                    metabolizing, speciesDefinition, compartments,entity.get(Growing.class), deltaTime);

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
     * @param deltaTime
     *            the delta time after the last iteration
     * @return rejectedFood food that cannot be consumed due to max ingestion
     *         rate and gut capacity
     */
    private Amount<Mass> feed(Amount<Mass> availableFood, Amount<Mass> biomass, Metabolizing metabolizing,
            SpeciesDefinition speciesDefinition, Compartments compartments,Growing growing, Amount<Duration> deltaTime) {
        Amount<Mass> rejectedFood;

        if (availableFood.getEstimatedValue() > 0) {
            // consumption rate depends on fish biomass
            Amount<Mass> ingestionAmount = biomass.times(speciesDefinition.getMeanIngestionRate().times(deltaTime))
                    .to(UnitConstants.BIOMASS);

            System.out.println("Biomass: " + biomass + " Expected: " + growing.getExpectedBiomass()
                    + " DesiredFoodAmount: " + desiredFoodAmount + " DesiredIngestionAmount: " + ingestionAmount);

            ingestionAmount = AmountUtil.max(ingestionAmount, desiredFoodAmount);
            // fish cannot consume more than its max ingestion rate
            Amount<Mass> foodToIngest = AmountUtil.min(ingestionAmount, availableFood);
            Amount<Energy> energyToIngest = foodToIngest.times(speciesDefinition.getEnergyContentFood())
                    .to(UnitConstants.CELLULAR_ENERGY);
            // transfer energy to gut
            Amount<Energy> rejectedEnergy = compartments.addToGut(energyToIngest).getRejected();
            /*
             * Convert rejected energy back to mass and add difference between
             * available food and the maximum ingestable amount.
             */
            rejectedFood = rejectedEnergy.divide(speciesDefinition.getEnergyContentFood()).to(UnitConstants.FOOD)
                    .plus(availableFood.minus(foodToIngest));

            System.out.println("FoodToIngest: " + foodToIngest + " rejected: " + rejectedFood
                        + " TotalIngested: " + availableFood.minus(rejectedFood));
            metabolizing.setIngestedEnergy(energyToIngest.minus(rejectedEnergy));
        }
        // fish cannot feed, nothing ingested
        else {
            rejectedFood = availableFood;
            metabolizing.setIngestedEnergy(AmountUtil.zero(UnitConstants.CELLULAR_ENERGY));
        }

        return rejectedFood;
    }

    /**
     * calculates how much the fish actually wants to eat to meet his expected biomass
     * so it tries to react to a deficit of biomass but still considers a maximum amount
     * a fish can eat in a given timestep
     *
     * if a fish has less than it's expected biomass(including variation) it's missing
     * mass an therefore gets hungry and tries to eat more, if this is not the case
     * isMissingBiomass in compartments is set to false, which causes the fish to not be hungry anymore
     */
    private void computeDesiredFoodAmount(Growing growing, Compartments compartments, SpeciesDefinition def,
                                          MersenneTwisterFast rng, Amount<Duration> deltaTime){
        double expectedBiomassVariation = 0.005;
        Amount<Mass> expectedBiomass = growing.getExpectedBiomass();
        Amount<Mass> biomass = growing.getBiomass();
        Amount<Mass> variatedExpected = expectedBiomass.plus(expectedBiomass.times(
                (rng.nextDouble(true,true)) * expectedBiomassVariation));

       // System.out.println("ExpectedBiomass: " + expectedBiomass + " VariatedBiomass: " + variatedExpected + " Biomass: " +growing.getBiomass());
        Amount<Mass> missingBiomass = variatedExpected.minus(biomass);

        compartments.setIsMissingBiomass(missingBiomass.isGreaterThan(Amount.valueOf(0.0,UnitConstants.BIOMASS)));

        if(compartments.isMissingBiomass()){
            Amount<Energy> missingEnergy = Amount.valueOf(missingBiomass.times(Compartment.Type.KJ_PER_GRAM_PROTEIN_VALUE).getEstimatedValue(),
                    UnitConstants.CELLULAR_ENERGY);

             desiredFoodAmount = AmountUtil.min(missingEnergy.divide(def.getEnergyContentFood()).to(UnitConstants.FOOD),
                     biomass.times(def.getMaxIngestionRate().times(deltaTime)).to(UnitConstants.BIOMASS));
        }
    }


    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.asList(Metabolizing.class, Growing.class, Compartments.class, Moving.class, LifeCycling.class,
                DynamicScheduling.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.asList(
                // for age in delay calculation of digesta entering gut
                AgeSystem.class,
                // to have the biomass updated
                GrowthSystem.class);
    }

}
