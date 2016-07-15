package de.zmt.ecs.system.environment;

import static javax.measure.unit.NonSI.DAY;

import java.util.Arrays;
import java.util.Collection;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.AbstractSystem;
import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.util.FormulaUtil;
import de.zmt.util.Habitat;
import de.zmt.util.quantity.AreaDensity;
import sim.engine.SimState;

/**
 * Manipulates food densities. It is assumed that fish cannot find all food
 * within an area, a minimum amount will always be left over. The regrowth
 * function will use the total amount and not just the available.
 * <p>
 * For example, a habitat with a minimum value half the maximum value will
 * regrow fastest, when there is no available food left, because the logistic
 * function is most steep at its center. Another habitat with a minimum value
 * near zero will regrow very slowly when there is no available food left.
 * <p>
 * <img src="doc-files/gen/FoodSystem.svg" alt= "FoodSystem Activity Diagram">
 * 
 * @see FormulaUtil#growAlgae(Amount, Amount, Amount, Amount)
 * @see Habitat
 * @see FoodMap
 * 
 * @author mey
 *
 */
/*
@formatter:off
@startuml doc-files/gen/FoodSystem.svg

start
if (first step in day?) then (yes)
	:grow food with algal growth rate;
endif
stop


@enduml
@formatter:on
 */
public class FoodSystem extends AbstractSystem {

    /** Grow food once per day. */
    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        FoodMap foodMap = entity.get(FoodMap.class);
        if (entity.get(SimulationTime.class).isFirstStepInDay()) {
            growFood(Amount.valueOf(1, DAY), entity.get(EnvironmentDefinition.class).getAlgalGrowthRate(), foodMap,
                    entity.get(HabitatMap.class));
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
    private static void growFood(Amount<Duration> delta, Amount<Frequency> algalGrowthRate, FoodMap foodMap,
            HabitatMap habitatMap) {
        for (int y = 0; y < foodMap.getHeight(); y++) {
            for (int x = 0; x < foodMap.getWidth(); x++) {
                Habitat habitat = habitatMap.obtainHabitat(x, y);

                if (!habitat.isAccessible()) {
                    continue;
                }

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

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.<Class<? extends EntitySystem>> asList(SimulationTimeSystem.class);
    }

}
