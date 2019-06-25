package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;

import static javax.measure.unit.NonSI.WEEK;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;

import de.zmt.util.AmountUtil;
import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.DynamicScheduling;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.FormulaUtil;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;

/**
 * Let entities grow if they could ingest enough food.
 * <p>
 * <img src="doc-files/gen/GrowthSystem.svg" alt= "GrowthSystem Activity
 * Diagram">
 * 
 * @author mey
 * 
 */
/*
@formatter:off
@startuml doc-files/gen/GrowthSystem.svg

start
:compute biomass;
:compute RMR;
if (biomass > expected biomass?) then (yes)
    partition "Update Expected Biomass" {
        :compute expected length
        for age + (1 step);
        :compute expected mass
        for expected length;
    }
else (no)
endif

if (biomass at top?) then (yes)
	:update length from biomass;
	if (//Can Change Phase//\n**AND** coin flip successful?) then (yes)
		:enter next phase;
	else (no)
	endif
else (no)
endif
stop

partition "Can Change Phase" {
    start
    :canChangeSex (true if not GONOCHORISTIC)<
    if (JUVENILE?) then (yes)
        :return true>
    elseif (canChangeSex **AND** INITIAL?) then (yes)
	    :return true>
    else
        :return false>
    endif
    stop
}

@enduml
@formatter:on
 */
public class GrowthSystem extends AgentSystem {
    private static final double ALLOW_NEXT_PHASE_PROBABILITY_FACTOR_PER_SECOND_PER_LENGTH_VALUE = 0.01;

    private final double nextPhaseMaxLengthVariation;

    /**
     * Factor per time frame and body length to calculate the probability for
     * phase change.
     * 
     * @see #isNextPhaseAllowed(Amount, Amount, Phase, Amount, Amount, double, MersenneTwisterFast)
     */
    private static final Amount<?> ALLOW_NEXT_PHASE_PROBABILITY_FACTOR = Amount.valueOf(
            ALLOW_NEXT_PHASE_PROBABILITY_FACTOR_PER_SECOND_PER_LENGTH_VALUE,
            UnitConstants.PER_SIMULATION_TIME.divide(UnitConstants.BODY_LENGTH));

    /**
     * counts to a week, after a week fish can attempt a new phase change(if it has grown)
     * after the try, timer gets reset to 0, don't no how/where other timer is used,
     * therefore just adding a little new one
     */
    private Amount<Duration> weeklyTimer = AmountUtil.zero(UnitConstants.SIMULATION_TIME);

    public GrowthSystem(MersenneTwisterFast random)
    {
        nextPhaseMaxLengthVariation = random.nextDouble() * 0.1;
    }

    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        Growing growing = entity.get(Growing.class);
        LifeCycling lifeCycling = entity.get(LifeCycling.class);
        SpeciesDefinition definition = entity.get(SpeciesDefinition.class);
        Aging aging = entity.get(Aging.class);
        Amount<Duration> deltaTime = entity.get(DynamicScheduling.class).getDeltaTime();
        weeklyTimer = weeklyTimer.plus(deltaTime);

        entity.get(Compartments.class).computeBiomassAndEnergy(growing);
        Amount<Mass> biomass = growing.getBiomass();
        Amount<Power> restingMetabolicRate = FormulaUtil.restingMetabolicRate(biomass);
        entity.get(Metabolizing.class).setRestingMetabolicRate(restingMetabolicRate);

        growing.setExpectedBiomass(computeExpectedBiomass(definition, entity.get(Aging.class), deltaTime));

        Amount<Length> oldLength = growing.getLength();
        // only grow in length if at top, prevent shrinking
        if (growing.hasTopBiomass()) {
            growing.setLength(FormulaUtil.expectedLength(definition.getLengthMassCoeff(), biomass,
                    definition.getInvLengthMassExponent()));


            // if fish has grown in length, fish may enter next phase
            //only checked once a week to ensure slower phase change
            // -> the more often the function is called the more likely it is for a fish to change phase
            if(weeklyTimer.isGreaterThan(Amount.valueOf(2,WEEK).to(UnitConstants.SIMULATION_TIME))
                    && oldLength.isLessThan(growing.getLength())){
                if (lifeCycling.canChangePhase(definition.canChangeSex()) && isNextPhaseAllowed(aging.getAge(), growing.getLength(), lifeCycling.getPhase(),
                        definition.getNextPhaseStartLength(lifeCycling.getPhase()),
                        definition.getNextPhase50PercentMaturityLength(lifeCycling.getPhase()),
                        nextPhaseMaxLengthVariation, state.random)) {
                    lifeCycling.enterNextPhase();
                }
                weeklyTimer = AmountUtil.zero(UnitConstants.SIMULATION_TIME);
            }
        }
    }

    /**
     * Computes expected biomass for the next step.
     * 
     * @param definition
     *            the {@link SpeciesDefinition}
     * @param aging
     *            the {@link Aging} component
     * @param deltaTime
     *            the time passed between iterations
     * @return expected biomass
     */
    private static Amount<Mass> computeExpectedBiomass(SpeciesDefinition definition, Aging aging,
            Amount<Duration> deltaTime) {
        Amount<Duration> virtualAge = aging.getAge().plus(deltaTime);
        Amount<Length> expectedLength = FormulaUtil.expectedLength(definition.getAsymptoticLength(),
                definition.getGrowthCoeff(), virtualAge, definition.getZeroSizeAge());
        Amount<Mass> expectedMass = FormulaUtil.expectedMass(definition.getLengthMassCoeff(), expectedLength,
                definition.getLengthMassExponent());

        return expectedMass;
    }

    /**
     * Determine if change to next phase is allowed. Decision is made based on
     * difference between current and next phase length. In this way, the
     * probability for a change rises the more the length increases.
     * 
     * <pre>
     *  allow_next_phase = (length - next_phase_length) [cm]
     *  &nbsp; * {@value #ALLOW_NEXT_PHASE_PROBABILITY_FACTOR_PER_SECOND_PER_LENGTH_VALUE} [1/(s*cm)] * delta [s]
     * </pre>
     * 
     * @param length
     *            the current length
     * @param nextPhaseStartLength
     *            the length needed for the next phase
     * @param nextPhase50PercentLength
     *            the length at which probability is 50%
     * @param random
     *            the random number generator of this simulation
     * @return {@code true} if phase change is allowed
     */
    private static boolean isNextPhaseAllowed(Amount<Duration> age, Amount<Length> length, LifeCycling.Phase phase, Amount<Length> nextPhaseStartLength,
                                              Amount<Length> nextPhase50PercentLength, double nextPhaseMaxLengthVariation,
                                              MersenneTwisterFast random) {

        //at this length a juvenile fish needs to change its phase to initialPhase
        //for female ok to take longer to change phase to give a bit more time for reproduction
        //if(phase == Phase.JUVENILE && length.isGreaterThan(nextPhase50PercentLength.times(1.2 + nextPhaseMaxLengthVariation)))
        if(phase == Phase.JUVENILE && age.getEstimatedValue() >= 3)
            return true;

       if(length.isLessThan(nextPhaseStartLength))
            return false;

        double probability = 1/(1+Math.pow(2, -(length.minus(nextPhase50PercentLength).getEstimatedValue())));

        if (probability < 0) {
            return false;
        } else if (probability > 1) {
            return true;
        } else {
            return random.nextBoolean(probability);
        }
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.asList(Growing.class, Compartments.class, Metabolizing.class, LifeCycling.class,
                DynamicScheduling.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.asList(
                /*
                 * to update the RMR after the previous value has been used for
                 * calculating the consumed energy
                 */
                ConsumeSystem.class);
    }
}
