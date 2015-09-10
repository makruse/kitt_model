package de.zmt.ecs.system.environment;

import java.util.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.environment.*;
import de.zmt.ecs.system.KittSystem;
import de.zmt.sim.params.def.EnvironmentDefinition;
import de.zmt.util.*;
import de.zmt.util.quantity.AreaDensity;

/**
 * Manipulates food densities. It is assumed that fish cannot find all food
 * within an area, a minimum amount will always be left over. The regrowth
 * function will use the total amount and not just the available.
 * <p>
 * For example, a habitat with a minimum value half the maximum value will
 * regrow fastest, when there is no available food left, because the logistic
 * function is most steep at its center. Another habitat with a minimum value
 * near zero will regrow very slowly when there is no available food left.
 * 
 * @see FormulaUtil#growAlgae(Amount, Amount, Amount, Amount)
 * @see Habitat
 * @see FoodMap
 * @author cmeyer
 *
 */
public class GrowFoodSystem extends KittSystem {

    /** Grow food once per day. */
    @Override
    protected void systemUpdate(Entity entity) {
	if (entity.get(SimulationTime.class).isFirstStepInDay()) {
	    growFood(EnvironmentDefinition.STEP_DURATION, entity.get(EnvironmentDefinition.class).getAlgalGrowthRate(),
		    entity.get(FoodMap.class), entity.get(HabitatMap.class));
	}

    }

    /**
     * Let algae grow for whole food grid.<br>
     * <b>NOTE:</b> Computationally expensive.
     * 
     * @param delta
     * @param algalGrowthRate
     * @param foodMap
     * @param habitatMap
     */
    private void growFood(Amount<Duration> delta, Amount<Frequency> algalGrowthRate, FoodMap foodMap,
	    HabitatMap habitatMap) {
	for (int y = 0; y < foodMap.getHeight(); y++) {
	    for (int x = 0; x < foodMap.getWidth(); x++) {
		Habitat habitat = habitatMap.obtainHabitat(x, y);

		// total food density is the available plus minimum
		Amount<AreaDensity> totalFoodDensity = foodMap.getFoodDensity(x, y).plus(habitat.getFoodDensityMin());

		Amount<AreaDensity> grownFoodDensity = FormulaUtil
			.growAlgae(totalFoodDensity, habitat.getFoodDensityMax(), algalGrowthRate, delta)
			.minus(habitat.getFoodDensityMin());

		foodMap.setFoodDensity(x, y, grownFoodDensity);
	    }
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.asList(EnvironmentDefinition.class, FoodMap.class, SimulationTime.class);
    }

}
