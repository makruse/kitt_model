package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

import sim.util.Double2D;

public class FlowFromPotentialsMapTest {
    private static final int MAP_SIZE = 3;
    private static final int MAP_CENTER = (MAP_SIZE - 1) >> 1;

    private FlowFromPotentialsMap map;

    @Before
    public void setUp() throws Exception {
	map = new FlowFromPotentialsMap(MAP_SIZE, MAP_SIZE);
    }

    @Test
    public void obtainDirectionOnEmpty() {
        assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_NEUTRAL));
    }

    @Test
    public void obtainDirectionOnSingle() {
	// all possible directions
	obtainDirectionOnSingle(DIRECTION_NEUTRAL);
	obtainDirectionOnSingle(DIRECTION_EAST);
	obtainDirectionOnSingle(DIRECTION_SOUTH);
	obtainDirectionOnSingle(DIRECTION_WEST);
	obtainDirectionOnSingle(DIRECTION_NORTH);
	obtainDirectionOnSingle(DIRECTION_SOUTHEAST);
	obtainDirectionOnSingle(DIRECTION_SOUTHWEST);
	obtainDirectionOnSingle(DIRECTION_NORTHWEST);
	obtainDirectionOnSingle(DIRECTION_NORTHEAST);
    }

    private static void obtainDirectionOnSingle(Double2D direction) {
	FlowMap singleMap = new FlowFromPotentialsMap(createDirectedMap(direction));
	assertThat(singleMap.obtainDirection(MAP_CENTER, MAP_CENTER), is(direction));
    }

    @Test
    public void obtainDirectionOnSingleWithWeight() {
	map.addMap(createDirectedMap(DIRECTION_SOUTH), 2);
	assertThat("Weight should not alter result when there is only a single map added.",
		obtainDirectionAtMapCenter(), is(DIRECTION_SOUTH));
    }

    @Test
    public void obtainDirectionOnMulti() {
	map.addMap(createDirectedMap(DIRECTION_SOUTH));
	map.addMap(createDirectedMap(DIRECTION_SOUTH));
	map.addMap(createDirectedMap(DIRECTION_NORTH));
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_SOUTH));
    }

    @Test
    public void obtainDirectionOnMultiWithWeight() {
	PotentialMap mapSouth = createDirectedMap(DIRECTION_SOUTH);
	PotentialMap mapNorth = createDirectedMap(DIRECTION_NORTH);

	map.addMap(mapSouth, 2);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_SOUTH));
	map.addMap(mapNorth, 1.5);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_SOUTH));

	map.setWeight(mapNorth, 3);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_NORTH));
    }

    private Double2D obtainDirectionAtMapCenter() {
	return map.obtainDirection(MAP_CENTER, MAP_CENTER);
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
