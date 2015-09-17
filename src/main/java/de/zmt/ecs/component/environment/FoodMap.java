package de.zmt.ecs.component.environment;

import java.util.Arrays;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.sim.display.KittWithUI.FieldPortrayable;
import de.zmt.sim.portrayal.portrayable.ProvidesPortrayable;
import de.zmt.util.UnitConstants;
import de.zmt.util.quantity.AreaDensity;
import sim.field.grid.*;
import sim.util.*;

/**
 * Handles food densities on grid cells in discrete map space.
 * 
 * @author cmeyer
 *
 */
public class FoodMap implements Component, ProvidesPortrayable<FieldPortrayable> {
    private static final long serialVersionUID = 1L;

    /** Reusable cache to improve performance in neighborhood lookup. */
    private final DoubleNeighborsResult lookupCache = new DoubleNeighborsResult();

    /** Stores amount of <b>available</b> food for every location. */
    final DoubleGrid2D foodField;

    public FoodMap(DoubleGrid2D foodField) {
	this.foodField = foodField;
    }

    /**
     * Finds available food around {@code worldPosition} within
     * {@code accessibleWorldRadius}.
     * <p>
     * The amount of available food mass is collected from affected density
     * values within food grid and stored in a return object. A distance penalty
     * will lead to less available food for distant patches.
     * <p>
     * <b>NOTE:</b> Lookup cache is reused in {@link FoundFood} return objects,
     * do not call this method again before handling the result.
     * 
     * @see FindFoodConverter
     * @param worldPosition
     * @param accessibleWorldRadius
     * @param converter
     * @return {@link FoundFood} object which contains the available amount and
     *         a callback function which triggers the subtraction from food
     *         field.
     */
    // FIXME need to sum up intersection area or only integer positions / radia
    // see TestFoodMap#findAvailableFoodOnDifferentPositions
    public FoundFood findAvailableFood(Double2D worldPosition, Amount<Length> accessibleWorldRadius,
	    FindFoodConverter converter) {
	Int2D mapPosition = converter.worldToMap(worldPosition);
	double accessibleMapRadius = converter.worldToMap(accessibleWorldRadius);

	DoubleNeighborsResult result = findRadialNeighbors(mapPosition,
		accessibleMapRadius);
	DoubleBag availableDensityValues = new DoubleBag(result.size());

	// sum available food densities from patches in reach
	double availableDensitiesSum = 0;
	for (int i = 0; i < result.size(); i++) {
	    double distanceSq = mapPosition.distanceSq(result.xPos.get(i), result.yPos.get(i));
	    // make less food available on distant patches
	    double distanceFraction = 1 / (distanceSq + 1);
	    assert distanceFraction > 0 && distanceFraction <= 1 : distanceFraction + "is an invalid value for a fraction";

	    double totalDensityValue = result.values.get(i);
	    double availableDensityValue = totalDensityValue * distanceFraction;

	    // add available density to bag for storing it within return object
	    availableDensityValues.add(availableDensityValue);
	    availableDensitiesSum += availableDensityValue;
	}
	// convert sum of available food densities to mass
	Amount<Mass> availableFood = converter.densityToMass(valueToDensity(availableDensitiesSum));

	return new FoundFood(availableFood, result, availableDensityValues);
    }

    /**
     * 
     * @param mapPosition
     * @param radius
     * @return result of radial neighbors lookup
     */
    private DoubleNeighborsResult findRadialNeighbors(Int2D mapPosition, double radius) {
	foodField.getRadialLocations(mapPosition.x, mapPosition.y, radius, Grid2D.BOUNDED, true, Grid2D.CENTER, true,
		lookupCache.xPos, lookupCache.yPos);
	
	lookupCache.values.clear();

	// DoubleGrid2D#getRadialNeighbors only accepts integer values for dist
	int numResults = lookupCache.xPos.numObjs;
	for (int i = 0; i < numResults; i++) {
	    lookupCache.values.add(foodField.get(lookupCache.xPos.get(i), lookupCache.yPos.get(i)));
	}
	
	return lookupCache;
    }

    /**
     * 
     * @param mapX
     *            map X coordinate
     * @param mapY
     *            map Y coordinate
     * @return available food density on patch at given position in g dry weight
     *         per square meter
     */
    public Amount<AreaDensity> getFoodDensity(int mapX, int mapY) {
	return valueToDensity(foodField.get(mapX, mapY));
    }

    /**
     * @param densityValue
     * @return {@link Amount} from given density value
     */
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
    public void setFoodDensity(int mapX, int mapY, Amount<AreaDensity> foodDensity) {
	double gramFood = foodDensity.doubleValue(UnitConstants.FOOD_DENSITY);
	foodField.set(mapX, mapY, gramFood);
    }

    public int getWidth() {
	return foodField.getWidth();
    }

    public int getHeight() {
	return foodField.getHeight();
    }

    @Override
    public String toString() {
	return Arrays.deepToString(foodField.field);
    }

    @Override
    public FieldPortrayable providePortrayable() {
	return new FieldPortrayable() {

	    @Override
	    public Object getField() {
		return foodField;
	    }
	};
    }

    /**
     * Provides available food and callback to return rejected.
     * <p>
     * <b>NOTE:</b> Unless {@link #returnRejected(Amount)} was called no changes
     * are made to the food field. The FoundFood object should not be stored as
     * well, because each one uses the same cache for storing results.
     * 
     * @author cmeyer
     * 
     */
    public class FoundFood {
	private final Amount<Mass> availableFood;
	private final DoubleNeighborsResult foundResult;
	/**
	 * Provided available density values per location. These can be lower
	 * than those in {@link #foundResult} and used to model diminishing
	 * accessibility of patches more far away than other.
	 */
	private final DoubleBag accessibleDensityValues;

	private FoundFood(Amount<Mass> availableFood, DoubleNeighborsResult foundResult,
		DoubleBag availableDensityValues) {
	    this.availableFood = availableFood;
	    this.foundResult = foundResult;
	    this.accessibleDensityValues = availableDensityValues;
	}

	/**
	 * Callback function to trigger the subtraction from the food field and
	 * return the rejected amount.
	 * <p>
	 * {@code rejected Food} cannot be higher than the available amount and
	 * not smaller than zero. If it is zero (exact), the next returned
	 * available food will be zero as well (no food left).
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
		throw new IllegalArgumentException("Cannot return more food than available.");
	    }
	    if (rejectedFood.getEstimatedValue() < 0) {
		throw new IllegalArgumentException("Rejected food cannot be negative.");
	    }

	    double returnFraction = 1 - rejectedFood.divide(availableFood).getEstimatedValue();
	    assert returnFraction > 0 && returnFraction <= 1 : returnFraction + " is an invalid value for a fraction.";

	    for (int i = 0; i < accessibleDensityValues.numObjs; i++) {
		double availableDensityValue = accessibleDensityValues.get(i);
		int x = foundResult.xPos.get(i);
		int y = foundResult.yPos.get(i);
		double totalDensityValue = foundResult.values.get(i);
		assert totalDensityValue >= availableDensityValue : "total: " + totalDensityValue + ", available: "
			+ availableDensityValue + " at (" + x + ", " + y + ")";

		/*
		 * Total value is decreased by difference to available, times
		 * the factor of return.
		 * 
		 * This divides the consumed amount of food between patches in
		 * reach, according to the distance penalty applied before.
		 */
		foodField.set(x, y, totalDensityValue - availableDensityValue * returnFraction);
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

    public static interface FindFoodConverter extends WorldToMapConverter {
	/**
	 * Convert food density within one map pixel to the absolute mass
	 * contained.
	 * 
	 * @param density
	 * @return absolute mass within patch with given {@code density}
	 */
	Amount<Mass> densityToMass(Amount<AreaDensity> density);

	/**
	 * Convert from world to map distance (pixel).
	 * 
	 * @param worldDistance
	 * @return map distance
	 */
	double worldToMap(Amount<Length> worldDistance);
    }

    private static class DoubleNeighborsResult {
	public final IntBag xPos = new IntBag();
	public final IntBag yPos = new IntBag();
	public final DoubleBag values = new DoubleBag();

	public int size() {
	    // all three bags will have the same number of elements
	    return xPos.numObjs;
	}
    }
}
