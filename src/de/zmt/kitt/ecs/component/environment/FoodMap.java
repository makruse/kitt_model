package de.zmt.kitt.ecs.component.environment;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import sim.field.grid.DoubleGrid2D;
import sim.util.*;
import de.zmt.ecs.Component;
import de.zmt.kitt.util.UnitConstants;
import de.zmt.kitt.util.quantity.AreaDensity;
import de.zmt.util.*;
import de.zmt.util.Grid2DUtil.LookupMode;
import de.zmt.util.Grid2DUtil.RadialNeighborsResult;

public class FoodMap implements Component {
    private static final long serialVersionUID = 1L;
    /** Stores amount of <b>available</b> food for every location */
    private final DoubleGrid2D foodField;

    public FoodMap(DoubleGrid2D foodField) {
	this.foodField = foodField;
    }

    /**
     * Finds available food around {@code position} within
     * {@code accessibleRadius}.
     * 
     * @param mapPosition
     * @param accessibleRadius
     * @param converter
     * @return {@link FoundFood} object which contains the available amount and
     *         a callback function which triggers the subtraction from food
     *         field.
     */
    public FoundFood findAvailableFood(Double2D mapPosition,
	    Amount<Length> accessibleRadius, DensityToMassConverter converter) {

	RadialNeighborsResult result = Grid2DUtil.findRadialNeighbors(
		foodField, mapPosition,
		accessibleRadius.doubleValue(UnitConstants.MAP_DISTANCE),
		LookupMode.BOUNDED);
	DoubleBag distancesSq = Grid2DUtil.computeDistancesSq(
		result.locationsResult, mapPosition);
	DoubleBag availableDensityValues = new DoubleBag(distancesSq.numObjs);

	// sum available food densities from patches in reach
	double availableDensitiesSum = 0;
	for (int i = 0; i < distancesSq.numObjs; i++) {
	    // only small amounts can be found in faraway patches
	    double distanceFactor = 1 / (distancesSq.get(i) + 1);
	    assert distanceFactor > 0 && distanceFactor <= 1 : distanceFactor;

	    double totalDensityValue = result.values.get(i);
	    double availableDensityValue = totalDensityValue * distanceFactor;

	    // add available density to bag for storing it within return object
	    availableDensityValues.add(availableDensityValue);
	    availableDensitiesSum += availableDensityValue;
	}
	// convert sum of available food densities to mass
	Amount<Mass> availableFood = converter
		.densityToMass(valueToDensity(availableDensitiesSum));

	return new FoundFood(availableFood, result, availableDensityValues);
    }

    /**
     * 
     * @param mapX
     *            map X coordinate
     * @param mapY
     *            map Y coordinate
     * @return food density on patch at given position in g dry weight per
     *         square meter
     */
    public Amount<AreaDensity> getFoodDensity(int mapX, int mapY) {
	return valueToDensity(foodField.get(mapX, mapY));
    }

    /** @return {@link Amount} from given density value */
    private Amount<AreaDensity> valueToDensity(double densityValue) {
	return Amount.valueOf(densityValue, UnitConstants.FOOD_DENSITY);
    }

    /**
     * Sets available food density at patch of given position.
     * 
     * @param mapX
     *            map X coordinate
     * @param mapY
     *            map Y coordinate
     * @param foodDensity
     *            dry weight, preferably in g/m2
     */
    public void setFoodDensity(int mapX, int mapY,
	    Amount<AreaDensity> foodDensity) {
	double gramFood = foodDensity.doubleValue(UnitConstants.FOOD_DENSITY);
	foodField.set(mapX, mapY, gramFood);
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

    /**
     * Provides available food and callback to return rejected.
     * 
     * @author cmeyer
     * 
     */
    public class FoundFood {
	private final Amount<Mass> availableFood;
	private final RadialNeighborsResult foundResult;
	/** Provided available density values per location */
	private final DoubleBag availableDensityValues;

	private FoundFood(Amount<Mass> availableFood,
		RadialNeighborsResult foundResult,
		DoubleBag availableDensityValues) {
	    this.availableFood = availableFood;
	    this.foundResult = foundResult;
	    this.availableDensityValues = availableDensityValues;
	}

	/**
	 * Callback function to trigger the subtraction from the food field and
	 * return the rejected amount.
	 * <p>
	 * <b>NOTE:</b> The food field is not changed unless this function is
	 * called.
	 * 
	 * @param rejectedFood
	 */
	public void returnRejected(Amount<Mass> rejectedFood) {
	    // all was rejected: no need to update the field
	    if (rejectedFood.equals(availableFood)) {
		return;
	    }
	    if (rejectedFood.isGreaterThan(availableFood)) {
		throw new IllegalArgumentException(
			"Cannot return more food than available.");
	    }
	    if (rejectedFood.getEstimatedValue() < 0) {
		throw new IllegalArgumentException(
			"Rejected food cannot be negative.");
	    }

	    double returnFraction = 1 - rejectedFood.divide(availableFood)
		    .getEstimatedValue();
	    assert returnFraction > 0 && returnFraction <= 1 : returnFraction;

	    for (int i = 0; i < availableDensityValues.numObjs; i++) {
		double availableDensityValue = availableDensityValues.get(i);
		int x = foundResult.locationsResult.xPos.get(i);
		int y = foundResult.locationsResult.yPos.get(i);
		double totalDensityValue = foundResult.values.get(i);
		assert totalDensityValue >= availableDensityValue : "total: "
			+ totalDensityValue + ", available: "
			+ availableDensityValue + " at (" + x + ", " + y + ")";

		/*
		 * Total value is decreased by difference to available, times
		 * the factor of return.
		 * 
		 * This divides the consumed amount of food between patches in
		 * reach, according to the distance penalty applied before.
		 */
		foodField.set(x, y, totalDensityValue - availableDensityValue
			* returnFraction);
	    }
	}

	public Amount<Mass> getAvailableFood() {
	    return availableFood;
	}

	@Override
	public String toString() {
	    return "FoundFood [availableFood=" + availableFood + "]";
	}
    }

    public static interface DensityToMassConverter {
	/**
	 * Convert food density within one map pixel to the absolute mass
	 * contained.
	 * 
	 * @param density
	 * @return absolute mass within patch with given {@code density}
	 */
	Amount<Mass> densityToMass(Amount<AreaDensity> density);
    }
}
