package de.zmt.ecs.system.agent;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.FormulaUtil;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import org.jscience.physics.amount.Amount;
import sim.engine.Kitt;
import sim.engine.SimState;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static javax.measure.unit.NonSI.WEEK;
import static javax.measure.unit.NonSI.YEAR;

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

    private Amount<Mass> biomassYesterday = Amount.valueOf(0, UnitConstants.BIOMASS);

    private HashMap<Entity, Amount<Duration>> timers = new HashMap<>();

    /**
     * Factor per time frame and body length to calculate the probability for
     * phase change.
     * 
     * @see #isNextPhaseAllowed(Amount, Amount, Phase, Amount, Amount, double, MersenneTwisterFast)
     */
    public GrowthSystem(MersenneTwisterFast random)
    {
        nextPhaseMaxLengthVariation = random.nextDouble() * 0.1;
    }

    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        Growing growing = entity.get(Growing.class);
        LifeCycling lifeCycling = entity.get(LifeCycling.class);
        Aging aging = entity.get(Aging.class);
        SpeciesDefinition definition = entity.get(SpeciesDefinition.class);
        Amount<Duration> deltaTime = entity.get(DynamicScheduling.class).getDeltaTime();
        Amount<Duration> timer = timers.get(entity);
        if(timer == null){
            timer = deltaTime;
            timers.put(entity, timer);
        } else {
            timer = timer.plus(deltaTime);
            timers.put(entity,timer);
        }

        entity.get(Compartments.class).computeBiomassAndEnergy(growing);
        Amount<Mass> biomass = growing.getBiomass();
        Amount<Power> restingMetabolicRate = FormulaUtil.restingMetabolicRate(biomass);
        entity.get(Metabolizing.class).setRestingMetabolicRate(restingMetabolicRate);

        growing.setExpectedBiomass(computeExpectedBiomass(definition, entity.get(Aging.class), deltaTime));

        // only grow in length once per day && if more biomass than day before, to prevent shrinking
        if(((Kitt) state).getEnvironment().get(SimulationTime.class).isFirstStepInDay(deltaTime)) {
            if (biomass.isGreaterThan(biomassYesterday)) {
                growing.setLength(FormulaUtil.expectedLength(definition.getLengthMassCoeff(), biomass,
                        definition.getInvLengthMassExponent()));
            }

               if(timer.isGreaterThan(Amount.valueOf(2, WEEK))) {
                   if (lifeCycling.canChangePhase(definition.canChangeSex())
                           && isNextPhaseAllowed(aging.getAge(), growing.getLength(), lifeCycling.getPhase(),
                           definition.getNextPhaseStartLength(lifeCycling.getPhase()),
                           definition.getNextPhase100PercentMaturityLength(lifeCycling.getPhase()),
                           nextPhaseMaxLengthVariation, state.random)) {
                       lifeCycling.enterNextPhase();
                   }
                   timer = Amount.valueOf(0, UnitConstants.SIMULATION_TIME);
                   timers.put(entity, timer);
               }
            biomassYesterday = biomass;
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
     * Doesn't properly work, presumably because of the weird dynamic scheduling and weird interactions with other
     * variables in the simulation...
     *
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
     * @param nextPhase100PercentLength
     *            the length at which probability is about 100%
     * @param random
     *            the random number generator of this simulation
     * @return {@code true} if phase change is allowed
     */

    private static boolean isNextPhaseAllowed(Amount<Duration> age, Amount<Length> length, LifeCycling.Phase phase, Amount<Length> nextPhaseStartLength,
                                              Amount<Length> nextPhase100PercentLength, double nextPhaseMaxLengthVariation,
                                              MersenneTwisterFast random) {

        //in nature: juveniles are no older than 3 years
        if(phase == Phase.JUVENILE && age.isGreaterThan(Amount.valueOf(3, YEAR)))
            return true;

        if(length.isLessThan(nextPhaseStartLength))
            return false;
        //TODO: add nextPhaseMaxLengthVariation
        double lengthDiff = length.minus(nextPhase100PercentLength).getEstimatedValue();
        //double lengthDiff = length.minus(nextPhase100PercentLength
        //                      .plus(nextPhase100PercentLength.times(nextPhaseMaxLengthVariation)).getEstimatedValue();
        //if your really want
        double probability;

        probability = 1/(1+Math.exp(-lengthDiff));

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
