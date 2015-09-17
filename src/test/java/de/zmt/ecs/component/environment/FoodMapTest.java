package de.zmt.ecs.component.environment;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.logging.Logger;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;
import org.junit.*;

import de.zmt.ecs.component.environment.FoodMap.*;
import de.zmt.util.*;
import de.zmt.util.quantity.AreaDensity;
import sim.field.grid.DoubleGrid2D;
import sim.util.*;

public class FoodMapTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FoodMapTest.class.getName());

    private static final int FOOD_FIELD_WIDTH = 4;
    private static final int FOOD_FIELD_HEIGHT = FOOD_FIELD_WIDTH;
    private static final double FOOD_FIELD_INIT_VALUE = 1;
    /** Exactly at the center of the grid. */
    private static final Double2D CENTER_POS = new Double2D((FOOD_FIELD_WIDTH + 1) * 0.5, (FOOD_FIELD_HEIGHT + 1) * 0.5);
    /**
     * At the edge of grid square, meaning any lookup radius > 0 will cover more
     * than one square.
     */
    private static final Double2D SQUARE_EDGE_POS = new Double2D(FOOD_FIELD_WIDTH / 2, FOOD_FIELD_WIDTH / 2);

    private static final Amount<Length> RADIUS_SMALL = Amount.valueOf(0.5, UnitConstants.WORLD_DISTANCE);
    private static final Amount<Length> RADIUS_WIDE = Amount.valueOf(1, UnitConstants.WORLD_DISTANCE);

    private static final double REJECTED_FOOD_PROPORTION = 0.4;
    private static final double MAX_ERROR = Math.pow(2, -40);

    /**
     * Available food value for five squares with factors by distance:<br>
     * 
     * <pre>
     *     0.5
     * 0.5 1   0.5
     *     0.5
     * </pre>
     */
    private static final double AVAILABLE_FOOD_MULTI_VALUE = FOOD_FIELD_INIT_VALUE + (4 * FOOD_FIELD_INIT_VALUE * 0.5);

    private FoodMap foodMap;

    @Before
    public void setUp() {
	foodMap = new FoodMap(new DoubleGrid2D(FOOD_FIELD_WIDTH, FOOD_FIELD_HEIGHT, FOOD_FIELD_INIT_VALUE));
    }

    /**
     * Test if food can be found and consumed on a single square, i.e. the
     * position and radius will not overlap more than one square.
     */
    @Test
    public void findAvailableFoodOnSingle() {
	FoundFood foundFood = foodMap.findAvailableFood(CENTER_POS, RADIUS_SMALL, new Converter());
	Amount<Mass> availableFood = foundFood.getAvailableFood();
	assertEquals(FOOD_FIELD_INIT_VALUE, availableFood.getEstimatedValue(), MAX_ERROR);

	Amount<Mass> rejectedFood = availableFood.times(REJECTED_FOOD_PROPORTION);
	foundFood.returnRejected(rejectedFood);
	Amount<AreaDensity> foodDensity = foodMap.getFoodDensity((int) CENTER_POS.x, (int) CENTER_POS.y);
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION) * FOOD_FIELD_INIT_VALUE,
		foodDensity.getEstimatedValue(), MAX_ERROR);
    }

    /**
     * Tests if nothing is available after taking all.
     */
    @Test
    public void findAvailableFoodOnSingleRejectZero() {
	findAndConsumeAll(CENTER_POS, RADIUS_SMALL);

	// check if there is no available food left now
	Amount<Mass> availableFoodEmpty = findAndConsumeAll(CENTER_POS, RADIUS_SMALL);
	assertThat(availableFoodEmpty.getEstimatedValue(), is(0d));
    }

    /**
     * Test if food can be found and consumed on multiple squares, i.e. the
     * position and radius will overlap more than one square.
     */
    @Test
    public void findAvailableFoodOnMulti() {
	FoundFood foundFood = foodMap.findAvailableFood(CENTER_POS, RADIUS_WIDE, new Converter());
	Amount<Mass> availableFood = foundFood.getAvailableFood();
	assertEquals(AVAILABLE_FOOD_MULTI_VALUE, availableFood.getEstimatedValue(), MAX_ERROR);

	Amount<Mass> rejectedFood = availableFood.times(REJECTED_FOOD_PROPORTION);
	foundFood.returnRejected(rejectedFood);

	int centerX = (int) CENTER_POS.x;
	int centerY = (int) CENTER_POS.y;

	// center
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION) * FOOD_FIELD_INIT_VALUE, foodMap
		.getFoodDensity(centerX, centerY).getEstimatedValue(), MAX_ERROR);
	// top
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION) * FOOD_FIELD_INIT_VALUE * 0.5, foodMap
		.getFoodDensity(centerX, centerY + 1).getEstimatedValue(), MAX_ERROR);
	// bottom
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION) * FOOD_FIELD_INIT_VALUE * 0.5, foodMap
		.getFoodDensity(centerX, centerY - 1).getEstimatedValue(), MAX_ERROR);
	// left
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION) * FOOD_FIELD_INIT_VALUE * 0.5, foodMap
		.getFoodDensity(centerX - 1, centerY).getEstimatedValue(), MAX_ERROR);
	// right
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION) * FOOD_FIELD_INIT_VALUE * 0.5, foodMap
		.getFoodDensity(centerX + 1, centerY).getEstimatedValue(), MAX_ERROR);
    }

    /**
     * Tests if amount of available food stays the same on different positions
     * of an equally distributed food field.
     */
    @Test
    public void findAvailableFoodOnDifferentPositions() {
	Amount<Mass> availableFoodCenter = findAndConsumeAll(CENTER_POS, RADIUS_WIDE);
	foodMap.foodField.setTo(FOOD_FIELD_INIT_VALUE);
	Amount<Mass> availableFoodUneven = findAndConsumeAll(SQUARE_EDGE_POS, RADIUS_WIDE);
	assertEquals(availableFoodCenter.getEstimatedValue(), availableFoodUneven.getEstimatedValue(), MAX_ERROR);
    }

    private Amount<Mass> findAndConsumeAll(Double2D position, Amount<Length> radius) {
        FoundFood foundFood = foodMap.findAvailableFood(position, radius, new Converter());
        foundFood.returnRejected(AmountUtil.zero(UnitConstants.FOOD));
        return foundFood.getAvailableFood();
    }

    private static class Converter implements FindFoodConverter {
	@Override
	public Amount<Mass> densityToMass(Amount<AreaDensity> density) {
	    // Only change unit, value stays the same.
	    return density.times(AmountUtil.one(UnitConstants.WORLD_AREA)).to(UnitConstants.FOOD);
	}

	@Override
	public Int2D worldToMap(Double2D worldCoordinates) {
	    return new Int2D((int) worldCoordinates.x, (int) worldCoordinates.y);
	}

	@Override
	public double worldToMap(Amount<Length> worldDistance) {
	    return worldDistance.doubleValue(UnitConstants.WORLD_DISTANCE);
	}
    }
}
