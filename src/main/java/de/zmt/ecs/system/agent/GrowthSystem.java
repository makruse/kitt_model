package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.params.def.EnvironmentDefinition;
import de.zmt.params.def.SpeciesDefinition;
import de.zmt.util.FormulaUtil;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;

/**
 * Let entities grow if they could ingest enough food.
 * <p>
 * <img src="doc-files/gen/GrowthSystem.svg" alt=
 * "GrowthSystem Activity Diagram">
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
    /**
     * Factor per time frame and body length to calculate the probability for
     * phase change.
     * 
     * @see #allowNextPhase(Amount, Amount)
     */
    private static final Amount<?> ALLOW_NEXT_PHASE_PROBABILITY_FACTOR = Amount.valueOf(
	    ALLOW_NEXT_PHASE_PROBABILITY_FACTOR_PER_SECOND_PER_LENGTH_VALUE,
	    UnitConstants.PER_SECOND.divide(UnitConstants.BODY_LENGTH));

    public GrowthSystem(Kitt sim) {
	super(sim);
    }

    /**
     * Updates biomass from compartments and resting metabolic rate. Fish will
     * grow in length if enough biomass could be accumulated.
     */
    @Override
    protected void systemUpdate(Entity entity) {
	Growing growing = entity.get(Growing.class);
	LifeCycling lifeCycling = entity.get(LifeCycling.class);
	SpeciesDefinition definition = entity.get(SpeciesDefinition.class);

	Amount<Mass> biomass = entity.get(Compartments.class).computeBiomass();
	growing.setBiomass(biomass);
	Amount<Power> restingMetabolicRate = FormulaUtil.restingMetabolicRate(biomass);
	entity.get(Metabolizing.class).setRestingMetabolicRate(restingMetabolicRate);

	// fish had enough energy to grow, update length
	if (biomass.isGreaterThan(growing.getExpectedBiomass())) {
	    growing.setExpectedBiomass(computeExpectedBiomass(definition, entity.get(Aging.class)));
	}

	// only grow in length if at top, prevent shrinking
	if (growing.hasTopBiomass()) {
	    growing.setLength(FormulaUtil.expectedLength(definition.getLengthMassCoeff(), biomass,
		    definition.getInvLengthMassExponent()));

	    // length has changed, reproductive status may change as well
	    if (lifeCycling.canChangePhase(definition.canChangeSex())
		    && allowNextPhase(growing.getLength(), definition.getNextPhaseLength(lifeCycling.getPhase()))) {
		lifeCycling.enterNextPhase();
	    }
	}
    }

    /**
     * Computes expected biomass for the next step.
     * 
     * @param definition
     * @param aging
     * @return expected biomass
     */
    private static Amount<Mass> computeExpectedBiomass(SpeciesDefinition definition, Aging aging) {
	Amount<Duration> virtualAge = aging.getAge().plus(EnvironmentDefinition.STEP_DURATION);
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
     * @param currentLength
     * @param nextPhaseLength
     * @return {@code true} if phase change is allowed
     */
    private boolean allowNextPhase(Amount<Length> currentLength, Amount<Length> nextPhaseLength) {
	double probability = currentLength.minus(nextPhaseLength).times(ALLOW_NEXT_PHASE_PROBABILITY_FACTOR)
		.times(EnvironmentDefinition.STEP_DURATION).to(Unit.ONE).getEstimatedValue();

	if (probability < 0) {
	    return false;
	} else if (probability > 1) {
	    return true;
	} else {
	    return getRandom().nextBoolean(probability);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Growing.class, Compartments.class, Metabolizing.class,
		LifeCycling.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(
		/*
		 * to update the RMR after the previous value has been used for
		 * calculating the consumed energy
		 */
		ConsumeSystem.class);
    }
}
