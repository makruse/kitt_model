package de.zmt.kitt.ecs.system.agent;

import java.util.*;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.ecs.component.agent.Reproducing.LifeStage;
import de.zmt.kitt.ecs.system.AbstractAgentSystem;
import de.zmt.kitt.sim.KittSim;
import de.zmt.kitt.sim.params.def.*;
import de.zmt.kitt.util.FormulaUtil;
import ecs.*;

/**
 * Let entities grow if they could ingest enough food.
 * 
 * @author cmeyer
 * 
 */
public class GrowthSystem extends AbstractAgentSystem {

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
	Growing growthComp = entity.get(Growing.class);
	Amount<Duration> delta = EnvironmentDefinition.STEP_DURATION;

	growthComp.setVirtualAgeForExpectedLength(growthComp.getVirtualAge()
		.plus(delta));

	growthComp.setExpectedLength(FormulaUtil.expectedLength(
		speciesDefinition.getMaxLength(),
		speciesDefinition.getGrowthCoeff(),
		growthComp.getVirtualAgeForExpectedLength(),
		speciesDefinition.getBirthLength()));

	growthComp.setExpectedBiomass(FormulaUtil.expectedMass(
		speciesDefinition.getLengthMassCoeff(),
		growthComp.getExpectedLength(),
		speciesDefinition.getLengthMassExponent()));
    }

    /**
     * Updates biomass from compartments. Fish will grow in size and
     * {@link #virtualAge} be increased if enough biomass could be accumulated.
     * 
     * @param entity
     */
    private void grow(Entity entity) {
	MassComponent massComp = entity.get(MassComponent.class);
	Reproducing reproducing = entity
		.get(Reproducing.class);
	Growing growing = entity.get(Growing.class);

	massComp.setBiomass(entity.get(Compartments.class)
		.computeBiomass());
	entity.get(Metabolizing.class).setStandardMetabolicRate(FormulaUtil
		.standardMetabolicRate(massComp.getBiomass()));

	// fish had enough energy to grow, update length and virtual age
	if (massComp.getBiomass().isGreaterThan(growing.getExpectedBiomass())) {
	    growing.acceptExpected();

	    // fish turns adult if it reaches a certain length
	    if (reproducing.getLifeStage() == LifeStage.JUVENILE
		    && growing.getExpectedLength().isGreaterThan(entity.get(
			    SpeciesDefinition.class).getAdultLength())) {
		reproducing.mature();
	    }
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(MassComponent.class,
		Growing.class,
		Compartments.class, Metabolizing.class,
		Reproducing.class);
    }
}
