package test;

import static org.junit.Assert.assertEquals;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;
import org.junit.*;

import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;
import de.zmt.kitt.ecs.component.environment.*;
import de.zmt.kitt.ecs.component.environment.FoodMap.DensityToMassConverter;
import de.zmt.kitt.ecs.component.environment.FoodMap.FoundFood;
import de.zmt.kitt.util.UnitConstants;
import de.zmt.kitt.util.quantity.AreaDensity;
import de.zmt.util.AmountUtil;

public class TestFoodField {

    private static final int FOOD_FIELD_WIDTH = 50;
    private static final int FOOD_FIELD_HEIGHT = FOOD_FIELD_WIDTH;
    private static final double FOOD_FIELD_INIT_VALUE = 1;
    private static final Double2D CENTER_POS = new Double2D(
	    (FOOD_FIELD_WIDTH + 1) * 0.5, (FOOD_FIELD_HEIGHT + 1) * 0.5);
    private static final Double2D SQUARE_EDGE_POS = new Double2D(25, 25);

    private static final Amount<Length> RADIUS_SMALL = Amount.valueOf(0.5,
	    UnitConstants.MAP_DISTANCE);
    private static final Amount<Length> RADIUS_WIDE = Amount.valueOf(0.7,
	    UnitConstants.MAP_DISTANCE);

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
    private static final double AVAILABLE_FOOD_MULTI_VALUE = FOOD_FIELD_INIT_VALUE
	    + (4 * FOOD_FIELD_INIT_VALUE * 0.5);

    private FoodMap foodMap;

    @Before
    public void setUp() {
	foodMap = new FoodMap(new DoubleGrid2D(FOOD_FIELD_WIDTH,
		FOOD_FIELD_HEIGHT, FOOD_FIELD_INIT_VALUE));
    }

    /**
     * Test if food can be found and consumed on a single square, i.e. the
     * position and radius will not overlap more than one square.
     */
    @Test
    public void testSingle() {
	FoundFood foundFood = foodMap.findAvailableFood(CENTER_POS,
		RADIUS_SMALL, new Converter());
	Amount<Mass> availableFood = foundFood.getAvailableFood();
	assertEquals(FOOD_FIELD_INIT_VALUE, availableFood.getEstimatedValue(),
		MAX_ERROR);

	Amount<Mass> rejectedFood = availableFood
		.times(REJECTED_FOOD_PROPORTION);
	foundFood.returnRejected(rejectedFood);
	Amount<AreaDensity> foodDensity = foodMap.getFoodDensity(
		(int) CENTER_POS.x, (int) CENTER_POS.y);
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION)
		* FOOD_FIELD_INIT_VALUE, foodDensity.getEstimatedValue(),
		MAX_ERROR);
    }

    /**
     * Test if food can be found and consumed on multiple squares, i.e. the
     * position and radius will overlap more than one square.
     */
    @Test
    public void testMulti() {
	FoundFood foundFood = foodMap.findAvailableFood(CENTER_POS,
		RADIUS_WIDE, new Converter());
	Amount<Mass> availableFood = foundFood.getAvailableFood();
	assertEquals(AVAILABLE_FOOD_MULTI_VALUE,
		availableFood.getEstimatedValue(), MAX_ERROR);

	Amount<Mass> rejectedFood = availableFood
		.times(REJECTED_FOOD_PROPORTION);
	foundFood.returnRejected(rejectedFood);

	int centerX = (int) CENTER_POS.x;
	int centerY = (int) CENTER_POS.y;

	// center
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION)
		* FOOD_FIELD_INIT_VALUE,
		foodMap.getFoodDensity(centerX, centerY).getEstimatedValue(),
		MAX_ERROR);
	// top
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION)
		* FOOD_FIELD_INIT_VALUE * 0.5,
		foodMap.getFoodDensity(centerX, centerY + 1)
			.getEstimatedValue(), MAX_ERROR);
	// bottom
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION)
		* FOOD_FIELD_INIT_VALUE * 0.5,
		foodMap.getFoodDensity(centerX, centerY - 1)
			.getEstimatedValue(), MAX_ERROR);
	// left
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION)
		* FOOD_FIELD_INIT_VALUE * 0.5,
		foodMap.getFoodDensity(centerX - 1, centerY)
			.getEstimatedValue(), MAX_ERROR);
	// right
	assertEquals(FOOD_FIELD_INIT_VALUE - (1 - REJECTED_FOOD_PROPORTION)
		* FOOD_FIELD_INIT_VALUE * 0.5,
		foodMap.getFoodDensity(centerX + 1, centerY)
			.getEstimatedValue(), MAX_ERROR);
    }

    /**
     * Tests if the sum of all square values change according to the consumed
     * amount.
     */
    @Test
    public void testTotalAmount() {
	double initialSum = FOOD_FIELD_INIT_VALUE * FOOD_FIELD_WIDTH
		* FOOD_FIELD_HEIGHT;

	FoundFood foundFood = foodMap.findAvailableFood(SQUARE_EDGE_POS,
		RADIUS_WIDE, new Converter());
	Amount<Mass> availableFood = foundFood.getAvailableFood();
	foundFood.returnRejected(AmountUtil.zero(availableFood));

	double sum = 0;
	double[] foodFieldArray = foodMap.getFieldObject().toArray();
	for (int i = 0; i < foodFieldArray.length; i++) {
	    sum += foodFieldArray[i];
	}

	// calculate changes in sum being made from food retrieval
	double sumDifference = initialSum - sum;
	assertEquals(availableFood.getEstimatedValue(), sumDifference,
		MAX_ERROR);
    }

    // FIXME need to sum up intersection area
    @Test
    public void testEqualityOnDifferentPositions() {
	foodMap.getFieldObject().setTo(FOOD_FIELD_INIT_VALUE);
	Amount<Mass> availableFoodUneven = findAndConsumeAll(SQUARE_EDGE_POS,
		RADIUS_WIDE);
	foodMap.getFieldObject().setTo(FOOD_FIELD_INIT_VALUE);
	Amount<Mass> availableFoodCenter = findAndConsumeAll(CENTER_POS,
		RADIUS_WIDE);
	assertEquals(availableFoodCenter.getEstimatedValue(),
		availableFoodUneven.getEstimatedValue(), MAX_ERROR);
    }

    private Amount<Mass> findAndConsumeAll(Double2D position,
	    Amount<Length> radius) {
	FoundFood foundFood = foodMap.findAvailableFood(position, radius,
		new Converter());
	foundFood.returnRejected(AmountUtil.zero(UnitConstants.FOOD));
	return foundFood.getAvailableFood();
    }

    private static class Converter implements DensityToMassConverter {
	@Override
	public Amount<Mass> densityToMass(Amount<AreaDensity> density) {
	    // just change unit
	    return density.times(AmountUtil.one(UnitConstants.MAP_AREA)).to(
		    UnitConstants.FOOD);
	}
    }

}
