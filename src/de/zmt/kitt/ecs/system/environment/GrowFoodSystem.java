package de.zmt.kitt.ecs.system.environment;

import java.util.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.ecs.component.environment.*;
import de.zmt.kitt.ecs.system.AbstractKittSystem;
import de.zmt.kitt.sim.Habitat;
import de.zmt.kitt.sim.params.def.EnvironmentDefinition;
import de.zmt.kitt.util.FormulaUtil;
import de.zmt.kitt.util.quantity.AreaDensity;
import ecs.*;

public class GrowFoodSystem extends AbstractKittSystem {

    /** Grow food once per day. */
    @Override
    protected void systemUpdate(Entity entity) {
	if (entity.get(SimulationTime.class).isFirstStepInDay()) {
	    growFood(EnvironmentDefinition.STEP_DURATION,
		    entity.get(EnvironmentDefinition.class)
			    .getAlgalGrowthRate(), entity.get(FoodMap.class),
		    entity.get(HabitatMap.class));
	}

    }

    /**
     * Let algae grow for whole food grid.<br>
     * <b>NOTE:</b> Computationally expensive.
     * 
     * @param delta
     * @param algalGrowthRate
     * @param foodField
     * @param habitatField
     */
    private void growFood(Amount<Duration> delta,
	    Amount<Frequency> algalGrowthRate, FoodMap foodMap,
	    HabitatMap habitatMap) {
	for (int y = 0; y < foodMap.getHeight(); y++) {
	    for (int x = 0; x < foodMap.getWidth(); x++) {
		Habitat habitat = habitatMap.obtainHabitat(x, y);

		// total food density is the available plus minimum
		Amount<AreaDensity> totalFoodDensity = foodMap
			.getFoodDensity(x, y).plus(
				habitat.getFoodDensityMin());

		Amount<AreaDensity> grownFoodDensity = FormulaUtil.growAlgae(
			totalFoodDensity, habitat.getFoodDensityMax(),
			algalGrowthRate, delta).minus(
			habitat.getFoodDensityMin());

		foodMap.setFoodDensity(x, y, grownFoodDensity);
	    }
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.asList(EnvironmentDefinition.class, FoodMap.class,
		SimulationTime.class);
    }

}
