package de.zmt.kitt.ecs.component.environment;

import org.jscience.physics.amount.Amount;

import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;
import de.zmt.kitt.util.UnitConstants;
import de.zmt.kitt.util.quantity.AreaDensity;
import ecs.Component;

public class FoodField implements Component {
    private static final long serialVersionUID = 1L;
    /** Stores amount of <b>available</b> food for every location */
    private final DoubleGrid2D foodField;

    public FoodField(DoubleGrid2D foodField) {
	this.foodField = foodField;
    }

    /**
     * 
     * @param position
     * @return available food density on patch at given position in g dry weight
     *         per square meter
     */
    // TODO full amount differs from g/m^2 if 1px != 1m^2
    public Amount<AreaDensity> getFoodDensity(Double2D position) {
        return Amount.valueOf(foodField.get((int) position.x, (int) position.y),
        	UnitConstants.FOOD_DENSITY);
    }

    /**
     * Sets available food density at patch of given position.
     * 
     * @param position
     * @param foodDensity
     *            dry weight, preferably in g/m2
     */
    public void setFoodDensity(Double2D position,
            Amount<AreaDensity> foodDensity) {
        double gramFood = foodDensity.doubleValue(UnitConstants.FOOD_DENSITY);
        foodField.set((int) position.x, (int) position.y, gramFood);
    }

    public int getWidth() {
	return foodField.getWidth();
    }

    public int getHeight() {
	return foodField.getHeight();
    }

    /**
     * Field object getter for portrayal in GUI.
     * 
     * @return food field
     */
    public DoubleGrid2D getFieldObject() {
	return foodField;
    }
}
