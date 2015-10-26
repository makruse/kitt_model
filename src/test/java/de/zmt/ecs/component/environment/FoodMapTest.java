package de.zmt.ecs.component.environment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.logging.Logger;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;
import org.junit.*;

import de.zmt.ecs.component.environment.FoodMap.*;
import de.zmt.pathfinding.MapUpdateHandler;
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

    private static final Converter CONVERTER = new Converter();

    /** Exactly at the center of the grid. */
    private static final Double2D CENTER_POS = new Double2D((FOOD_FIELD_WIDTH + 1) * 0.5,
	    (FOOD_FIELD_HEIGHT + 1) * 0.5);
    private static final Int2D MAP_CENTER_POS = CONVERTER.worldToMap(CENTER_POS);

    /**
     * At the edge of grid square, meaning any lookup radius > 0 will cover more
     * than one square.
     */
    private static final Double2D SQUARE_EDGE_POS = new Double2D(FOOD_FIELD_WIDTH / 2, FOOD_FIELD_WIDTH / 2);

    private static final Amount<Length> RADIUS_SMALL = Amount.valueOf(0.5, UnitConstants.WORLD_DISTANCE);
    private static final Amount<Length> RADIUS_WIDE = Amount.valueOf(1, UnitConstants.WORLD_DISTANCE);

    private static final double REJECTED_FOOD_PROPORTION = 0.4;
    /**
     * Food density value at center remaining after rejecting proportion of
     * {@value #REJECTED_FOOD_PROPORTION}.
     */
    private static final double REMAINING_FOOD_AT_CENTER = FOOD_FIELD_INIT_VALUE
	    - (1 - REJECTED_FOOD_PROPORTION) * FOOD_FIELD_INIT_VALUE;
    /**
     * Food density value at neighbors remaining after rejecting proportion of
     * {@value #REJECTED_FOOD_PROPORTION}.
     */
    private static final double REMAINING_FOOD_AT_NEIGHBORS = FOOD_FIELD_INIT_VALUE
	    - (1 - REJECTED_FOOD_PROPORTION) * FOOD_FIELD_INIT_VALUE * 0.5;
    /** Maximum error accepted due to imprecision in calculations. */
    private static final double MAX_ERROR = 1E-10;

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
    private MapUpdateHandler mockUpdateHandler;

    @Before
    public void setUp() {
	mockUpdateHandler = mock(MapUpdateHandler.class);
	foodMap = new FoodMap(new DoubleGrid2D(FOOD_FIELD_WIDTH, FOOD_FIELD_HEIGHT, FOOD_FIELD_INIT_VALUE),
		mockUpdateHandler);
    }

    /**
     * Test if food can be found and consumed on a single square, i.e. the
     * position and radius will not overlap more than one square.
     */
    @Test
    public void findAvailableFoodOnSingle() {
	FoundFood foundFood = foodMap.findAvailableFood(CENTER_POS, RADIUS_SMALL, CONVERTER);
	Amount<Mass> availableFood = foundFood.getAvailableFood();
	assertThat(availableFood.getEstimatedValue(), is(closeTo(FOOD_FIELD_INIT_VALUE, MAX_ERROR)));

	Amount<Mass> rejectedFood = availableFood.times(REJECTED_FOOD_PROPORTION);
	foundFood.returnRejected(rejectedFood);
	assertThat(foodMap.getFoodDensity(MAP_CENTER_POS.x, MAP_CENTER_POS.y).getEstimatedValue(),
		is(closeTo(REMAINING_FOOD_AT_CENTER, MAX_ERROR)));

	verify(mockUpdateHandler).markDirty(MAP_CENTER_POS.x, MAP_CENTER_POS.y);
    }

    /**
     * Tests if nothing is available after taking all.
     */
    @Test
    public void findAvailableFoodOnSingleRejectZero() {
	findAndConsumeAll(CENTER_POS, RADIUS_SMALL);

	// we should not be able to get anything now
	Amount<Mass> availableFoodEmpty = findAndConsumeAll(CENTER_POS, RADIUS_SMALL);
	assertThat(availableFoodEmpty.getEstimatedValue(), is(0d));
    }

    /**
     * Test if food can be found and consumed on multiple squares, i.e. the
     * position and radius will overlap more than one square.
     */
    @Test
    public void findAvailableFoodOnMulti() {
	FoundFood foundFood = foodMap.findAvailableFood(CENTER_POS, RADIUS_WIDE, CONVERTER);
	Amount<Mass> availableFood = foundFood.getAvailableFood();
	assertThat(availableFood.getEstimatedValue(), is(closeTo(AVAILABLE_FOOD_MULTI_VALUE, MAX_ERROR)));

	Amount<Mass> rejectedFood = availableFood.times(REJECTED_FOOD_PROPORTION);
	foundFood.returnRejected(rejectedFood);

	// center
	assertThat(foodMap.getFoodDensity(MAP_CENTER_POS.x, MAP_CENTER_POS.y).getEstimatedValue(),
		is(closeTo(REMAINING_FOOD_AT_CENTER, MAX_ERROR)));
	verify(mockUpdateHandler).markDirty(MAP_CENTER_POS.x, MAP_CENTER_POS.y);
	// top
	assertThat(foodMap.getFoodDensity(MAP_CENTER_POS.x, MAP_CENTER_POS.y - 1).getEstimatedValue(),
		is(closeTo(REMAINING_FOOD_AT_NEIGHBORS, MAX_ERROR)));
	verify(mockUpdateHandler).markDirty(MAP_CENTER_POS.x, MAP_CENTER_POS.y - 1);
	// bottom
	assertThat(foodMap.getFoodDensity(MAP_CENTER_POS.x, MAP_CENTER_POS.y + 1).getEstimatedValue(),
		is(closeTo(REMAINING_FOOD_AT_NEIGHBORS, MAX_ERROR)));
	verify(mockUpdateHandler).markDirty(MAP_CENTER_POS.x, MAP_CENTER_POS.y + 1);
	// left
	assertThat(foodMap.getFoodDensity(MAP_CENTER_POS.x - 1, MAP_CENTER_POS.y).getEstimatedValue(),
		is(closeTo(REMAINING_FOOD_AT_NEIGHBORS, MAX_ERROR)));
	verify(mockUpdateHandler).markDirty(MAP_CENTER_POS.x - 1, MAP_CENTER_POS.y);
	// right
	assertThat(foodMap.getFoodDensity(MAP_CENTER_POS.x + 1, MAP_CENTER_POS.y).getEstimatedValue(),
		is(closeTo(REMAINING_FOOD_AT_NEIGHBORS, MAX_ERROR)));
	verify(mockUpdateHandler).markDirty(MAP_CENTER_POS.x + 1, MAP_CENTER_POS.y);
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
	assertThat(availableFoodCenter.getEstimatedValue(),
		is(closeTo(availableFoodUneven.getEstimatedValue(), MAX_ERROR)));
    }

    /**
     * Tests if continuously rejecting a partial amount will make available food
     * zero without errors.
     */
    @Test
    public void findAvailableFoodOnContinuousReject() {
	Amount<Mass> availableFood;
	do {
	    FoundFood foundFood = foodMap.findAvailableFood(CENTER_POS, RADIUS_SMALL, CONVERTER);
	    availableFood = foundFood.getAvailableFood();
	    foundFood.returnRejected(availableFood.times(REJECTED_FOOD_PROPORTION));
	} while (availableFood.getEstimatedValue() > 0);
    }

    private Amount<Mass> findAndConsumeAll(Double2D position, Amount<Length> radius) {
	FoundFood foundFood = foodMap.findAvailableFood(position, radius, CONVERTER);
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
