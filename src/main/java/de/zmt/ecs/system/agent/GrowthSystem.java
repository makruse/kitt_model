package de.zmt.ecs.system.agent;

import java.util.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.util.*;
import sim.engine.Kitt;
import sim.params.def.*;

/**
 * Let entities grow if they could ingest enough food.
 * 
 * @author mey
 * 
 */
public class GrowthSystem extends AgentSystem {
    /** Factor for probability of changing reproductive status. */
    private static final double ALLOW_NEXT_PHASE_PROBABILITY_FACTOR = 0.01;

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
     * probability for a change rises the more the length increases.<br>
     * {@code allow_next_phase = (length - next_phase_length) * 1/cm * }
     * {@value #ALLOW_NEXT_PHASE_PROBABILITY_FACTOR}
     * 
     * @param currentLength
     * @param nextPhaseLength
     * @return {@code true} if phase change is allowed
     */
    private boolean allowNextPhase(Amount<Length> currentLength, Amount<Length> nextPhaseLength) {
	double probability = currentLength.minus(nextPhaseLength).doubleValue(UnitConstants.BODY_LENGTH)
		* ALLOW_NEXT_PHASE_PROBABILITY_FACTOR;

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
	return Arrays.<Class<? extends EntitySystem>> asList(ConsumeSystem.class);
    }
}
