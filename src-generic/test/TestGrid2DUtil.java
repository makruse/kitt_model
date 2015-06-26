package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;
import de.zmt.util.*;
import de.zmt.util.Grid2DUtil.LookupMode;
import de.zmt.util.Grid2DUtil.DoubleNeighborsResult;

// TODO test toroidal
public class TestGrid2DUtil {

    private static final int GRID_WIDTH = 50;
    private static final int GRID_HEIGHT = GRID_WIDTH;
    private static final Double2D CENTER_POS = new Double2D(
	    (GRID_WIDTH + 1) / 2d, (GRID_HEIGHT + 1) / 2d);
    private static final Double2D POS_BETWEEN_SQUARES = new Double2D(1, 1.5);

    private static final double RADIUS_SMALL = 0.5;
    private static final double RADIUS_WIDE = 0.7;

    DoubleGrid2D grid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);

    @Test
    public void testSingle() {
	DoubleNeighborsResult result = Grid2DUtil.findRadialNeighbors(grid,
		CENTER_POS, RADIUS_SMALL, LookupMode.BOUNDED);
	assertEquals(1, result.values.numObjs);

    }

    @Test
    public void testBetweenSquares() {
	DoubleNeighborsResult result = Grid2DUtil.findRadialNeighbors(grid,
		POS_BETWEEN_SQUARES, RADIUS_SMALL, LookupMode.BOUNDED);
	assertEquals(2, result.values.numObjs);

    }

    @Test
    public void testMulti() {
	DoubleNeighborsResult result = Grid2DUtil.findRadialNeighbors(grid,
		CENTER_POS, RADIUS_WIDE, LookupMode.BOUNDED);
	assertEquals(5, result.values.numObjs);
    }
}
