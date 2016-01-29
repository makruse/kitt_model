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
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.util.FormulaUtil;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;

/**
 * Let entities grow if they could ingest enough food.
 * 
 * @author mey
 * 
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

    @Override
    protected void systemUpdate(Entity entity) {
	computeExpecteds(entity);
	grow(entity);
    }

    /**
     * Computes expected biomass and length.
     * 
     * @see FormulaUtil
     * @param entity
     */
    private static void computeExpecteds(Entity entity) {
	SpeciesDefinition speciesDefinition = entity.get(SpeciesDefinition.class);
	Growing growing = entity.get(Growing.class);
	Amount<Duration> delta = EnvironmentDefinition.STEP_DURATION;

	Amount<Duration> virtualAgeForExpectedLength = growing.getVirtualAge().plus(delta);
	growing.setVirtualAgeForExpectedLength(virtualAgeForExpectedLength);

	growing.setExpectedLength(FormulaUtil.expectedLength(speciesDefinition.getAsymptoticLength(),
		speciesDefinition.getGrowthCoeff(), virtualAgeForExpectedLength, speciesDefinition.getZeroSizeAge()));

	growing.setExpectedBiomass(FormulaUtil.expectedMass(speciesDefinition.getLengthMassCoeff(),
		growing.getExpectedLength(), speciesDefinition.getLengthMassDegree()));
    }

    /**
     * Updates biomass from compartments. Fish will grow in size and
     * {@link Growing#virtualAge} be increased if enough biomass could be
     * accumulated.
     * 
     * @param entity
     */
    private void grow(Entity entity) {
	Growing growing = entity.get(Growing.class);
	LifeCycling lifeCycling = entity.get(LifeCycling.class);
	SpeciesDefinition speciesDefinition = entity.get(SpeciesDefinition.class);

	Amount<Mass> biomass = entity.get(Compartments.class).computeBiomass();
	growing.setBiomass(biomass);
	Amount<Power> restingMetabolicRate = FormulaUtil.restingMetabolicRate(biomass);
	entity.get(Metabolizing.class).setRestingMetabolicRate(restingMetabolicRate);

	// fish had enough energy to grow, update length and virtual age
	if (biomass.isGreaterThan(growing.getExpectedBiomass())) {
	    growing.acceptExpected();

	    // length has changed, reproductive status may change as well
	    Amount<Length> nextPhaseLength = speciesDefinition.getNextPhaseLength(lifeCycling.getPhase());
	    if (lifeCycling.canChangePhase(speciesDefinition.canChangeSex())
		    && allowNextPhase(growing.getLength(), nextPhaseLength)) {
		lifeCycling.enterNextPhase();
	    }
	}
    }

    /**
     * Determine if change to next phase is allowed. Decision is made based on
     * difference between current and next phase length. In this way, the
     * probability for a change rises the more the length increases.
     * 
     * <pre>
     *  allow_next_phase = (length - next_phase_length) [cm]
     *  &nbsp * {@value #ALLOW_NEXT_PHASE_PROBABILITY_FACTOR_PER_SECOND_PER_LENGTH_VALUE} [1/(s*cm)] * delta [s]
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
