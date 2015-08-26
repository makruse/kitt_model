package de.zmt.ecs.system.agent;

import java.util.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.Reproducing.Phase;
import de.zmt.ecs.system.AbstractAgentSystem;
import de.zmt.sim.KittSim;
import de.zmt.sim.params.def.*;
import de.zmt.util.*;

/**
 * Let entities grow if they could ingest enough food.
 * 
 * @author cmeyer
 * 
 */
public class GrowthSystem extends AbstractAgentSystem {
    /** Factor for probability of changing reproductive status. */
    private static final double ALLOW_NEXT_PHASE_PROBABILITY_FACTOR = 0.01;

    public GrowthSystem(KittSim sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	computeExpected(entity);
	grow(entity);
    }

    // TODO we may need to do that before feeding
    // (expectedBiomass is referenced in ProteinCompartment)
    private void computeExpected(Entity entity) {
	SpeciesDefinition speciesDefinition = entity
		.get(SpeciesDefinition.class);
	Growing growing = entity.get(Growing.class);
	Amount<Duration> delta = EnvironmentDefinition.STEP_DURATION;

	growing.setVirtualAgeForExpectedLength(growing.getVirtualAge()
		.plus(delta));

	growing.setExpectedLength(FormulaUtil.expectedLength(
		speciesDefinition.getMaxLength(),
		speciesDefinition.getGrowthCoeff(),
		growing.getVirtualAgeForExpectedLength(),
		speciesDefinition.getBirthLength()));

	growing.setExpectedBiomass(FormulaUtil.expectedMass(
		speciesDefinition.getLengthMassCoeff(),
		growing.getExpectedLength(),
		speciesDefinition.getLengthMassExponent()));
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
	Reproducing reproducing = entity.get(Reproducing.class);
	SpeciesDefinition speciesDefinition = entity
		.get(SpeciesDefinition.class);

	Amount<Mass> biomass = entity.get(Compartments.class).computeBiomass();
	growing.setBiomass(biomass);
	entity.get(Metabolizing.class).setStandardMetabolicRate(
		FormulaUtil.standardMetabolicRate(biomass));

	// fish had enough energy to grow, update length and virtual age
	if (biomass.isGreaterThan(growing.getExpectedBiomass())) {
	    growing.acceptExpected();

	    // length has changed, reproductive status may change as well
	    if (allowNextPhase(growing.getLength(),
		    reproducing.getPhase(), speciesDefinition)) {
		reproducing.enterNextPhase();
	    }
	}
    }

    /**
     * Determine if change to next phase is allowed. First check if status
     * allows it and then decide based on difference between current and next
     * phase length. In this way, the probability for a change rises the more
     * the length increases.<br>
     * {@code allow_next_phase = (length - next_phase_length) * 1/cm * }
     * {@value #ALLOW_NEXT_PHASE_PROBABILITY_FACTOR}
     * 
     * @param currentLength
     * @param currentPhase
     * @param definition
     * @return {@code true} if status change is allowed
     */
    private boolean allowNextPhase(Amount<Length> currentLength,
	    Phase currentPhase, SpeciesDefinition definition) {
	// can only change status when juvenile to enter initial,
	// or when in initial to change sex
	if (currentPhase != Phase.JUVENILE
		&& !(definition.doesChangeSex() && currentPhase == Phase.INITIAL)) {
	    return false;
	}

	Amount<Length> nextPhaseLength = definition
		.getNextPhaseLength(currentPhase);
	double probability = currentLength.minus(nextPhaseLength).doubleValue(
		UnitConstants.BODY_LENGTH)
		* ALLOW_NEXT_PHASE_PROBABILITY_FACTOR;

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
	return Arrays.<Class<? extends Component>> asList(Growing.class,
		Compartments.class, Metabolizing.class, Reproducing.class);
    }
}
