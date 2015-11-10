package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

import sim.util.Double2D;

public class FlowFromPotentialMapTest {
    private static final int MAP_SIZE = 3;
    private static final int MAP_CENTER = (MAP_SIZE - 1) >> 1;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void obtainDirection() {
	// all possible directions
	obtainDirection(DIRECTION_NEUTRAL);
	obtainDirection(DIRECTION_EAST);
	obtainDirection(DIRECTION_SOUTH);
	obtainDirection(DIRECTION_WEST);
	obtainDirection(DIRECTION_NORTH);
	obtainDirection(DIRECTION_SOUTHEAST);
	obtainDirection(DIRECTION_SOUTHWEST);
	obtainDirection(DIRECTION_NORTHWEST);
	obtainDirection(DIRECTION_NORTHEAST);
    }

    private static void obtainDirection(Double2D direction) {
	FlowMap map = new FlowFromPotentialMap(createDirectedMap(direction));
	assertThat(map.obtainDirection(MAP_CENTER, MAP_CENTER), is(direction));
    }

    /**
     * Creates a potential map pointing to given direction.
     * 
     * @param direction
     * @return potential map
     */
    private static PotentialMap createDirectedMap(Double2D direction) {
	int x = (int) Math.round(direction.x) + 1;
	int y = (int) Math.round(direction.y) + 1;
	double[][] values = new double[3][3];
	values[x][y] = 1;
	return new SimplePotentialMap(values);
    }
}
