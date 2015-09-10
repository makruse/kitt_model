package de.zmt.util;

import static org.junit.Assert.*;

import org.junit.Test;

import sim.util.*;
import de.zmt.util.Grid2DUtil.LocationsResult;
import de.zmt.util.Grid2DUtil.LookupMode;

// TODO test toroidal
public class Grid2DUtilTest {

    private static final int GRID_WIDTH = 50;
    private static final int GRID_HEIGHT = GRID_WIDTH;

    // LINE LOOKUP
    private static final Int2D POS_1 = new Int2D(0, 1);
    private static final Int2D POS_2 = new Int2D(3, 3);

    // RADIAL LOOKUP
    private static final Double2D CENTER_POS = new Double2D(
	    (GRID_WIDTH + 1) / 2d, (GRID_HEIGHT + 1) / 2d);
    private static final Double2D POS_BETWEEN_SQUARES = new Double2D(1, 1.5);

    private static final double RADIUS_SMALL = 0.5;
    private static final double RADIUS_WIDE = 0.7;

    @Test
    public void testFindLineLocations() {
	LocationsResult result = Grid2DUtil.findLineLocations(GRID_WIDTH,
		GRID_HEIGHT, POS_1, POS_2, LookupMode.BOUNDED);
	System.out.println("result 1to2: " + result);
	LocationsResult resultInv = Grid2DUtil.findLineLocations(GRID_WIDTH,
		GRID_HEIGHT, POS_2, POS_1, LookupMode.BOUNDED);
	System.out.println("result 2to1: " + resultInv);

	int lastIndex = result.getSize() - 1;
	// check if start and end points match
	assertTrue(result.getX(0) == resultInv.getX(lastIndex));
	assertTrue(result.getY(0) == resultInv.getY(lastIndex));

	assertTrue(result.getX(0) == POS_1.x);
	assertTrue(result.getY(0) == POS_1.y);
	assertTrue(result.getX(lastIndex) == POS_2.x);
	assertTrue(result.getY(lastIndex) == POS_2.y);

	assertTrue(resultInv.getX(0) == POS_2.x);
	assertTrue(resultInv.getY(0) == POS_2.y);
	assertTrue(resultInv.getX(lastIndex) == POS_1.x);
	assertTrue(resultInv.getY(lastIndex) == POS_1.y);
    }

    @Test
    public void testFindRadialLocations() {
	// centered single
	assertEquals(1, Grid2DUtil.findRadialLocations(GRID_WIDTH, GRID_HEIGHT,
			CENTER_POS, RADIUS_SMALL, LookupMode.BOUNDED).getSize());
	// between squares
	assertEquals(
		2,
		Grid2DUtil.findRadialLocations(GRID_WIDTH, GRID_HEIGHT,
			POS_BETWEEN_SQUARES, RADIUS_SMALL, LookupMode.BOUNDED)
			.getSize());
	// spanning over multiple squares
	assertEquals(5, Grid2DUtil.findRadialLocations(GRID_WIDTH, GRID_HEIGHT,
			CENTER_POS, RADIUS_WIDE, LookupMode.BOUNDED).getSize());
    }
}
