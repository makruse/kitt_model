package de.zmt.pathfinding;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static sim.util.DirectionConstants.*;

import org.hamcrest.Double2DCloseTo;
import org.junit.Before;
import org.junit.Test;

import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;

public class FlowFromPotentialsMapTest {
    private static final int MAP_SIZE = 3;
    private static final int MAP_CENTER = (MAP_SIZE - 1) / 2;

    private static final double MAX_ERROR = 1E-15d;

    private FlowFromPotentialsMap map;

    @Before
    public void setUp() throws Exception {
	map = new FlowFromPotentialsMap(MAP_SIZE, MAP_SIZE);
    }

    @Test
    public void obtainDirectionOnEmpty() {
	assertThat(obtainDirectionAtMapCenter(), is(NEUTRAL));
    }

    @Test
    public void obtainDirectionOnSingle() {
	// all possible directions
	obtainDirectionOnSingle(NEUTRAL);
	obtainDirectionOnSingle(EAST);
	obtainDirectionOnSingle(SOUTH);
	obtainDirectionOnSingle(WEST);
	obtainDirectionOnSingle(NORTH);
	obtainDirectionOnSingle(SOUTHEAST);
	obtainDirectionOnSingle(SOUTHWEST);
	obtainDirectionOnSingle(NORTHWEST);
	obtainDirectionOnSingle(NORTHEAST);
    }

    private static void obtainDirectionOnSingle(Double2D direction) {
	FlowMap singleMap = new FlowFromPotentialsMap(createDirectedMap(direction));
	assertThat(singleMap.obtainDirection(MAP_CENTER, MAP_CENTER),
		is(Double2DCloseTo.closeTo(direction, MAX_ERROR)));
    }

    @Test
    public void obtainDirectionOnSingleWithWeight() {
	map.addMap(createDirectedMap(SOUTH), 2);
	assertThat("Weight should not alter result when there is only a single map added.",
		obtainDirectionAtMapCenter(), is(SOUTH));
    }

    @Test
    public void obtainDirectionOnCustomEdgeHint() {
	map = new FlowFromPotentialsMap(
		new SimplePotentialMap(new DoubleGrid2D(MAP_SIZE, MAP_SIZE, 0), new EdgeHandler(1)));
	assertThat(map.obtainDirection(0, 0), is(Double2DCloseTo.closeTo(NORTHWEST)));
	assertThat(map.obtainDirection(0, 1), is(Double2DCloseTo.closeTo(WEST)));
	assertThat(map.obtainDirection(0, 2), is(Double2DCloseTo.closeTo(SOUTHWEST)));
	assertThat(map.obtainDirection(1, 0), is(Double2DCloseTo.closeTo(NORTH)));
	assertThat(map.obtainDirection(1, 1), is(Double2DCloseTo.closeTo(NEUTRAL)));
	assertThat(map.obtainDirection(1, 2), is(Double2DCloseTo.closeTo(SOUTH)));
	assertThat(map.obtainDirection(2, 0), is(Double2DCloseTo.closeTo(NORTHEAST)));
	assertThat(map.obtainDirection(2, 1), is(Double2DCloseTo.closeTo(EAST)));
	assertThat(map.obtainDirection(2, 2), is(Double2DCloseTo.closeTo(SOUTHEAST)));
    }

    @Test
    public void obtainDirectionOnMulti() {
	map.addMap(createDirectedMap(SOUTH));
	map.addMap(createDirectedMap(SOUTH));
	map.addMap(createDirectedMap(NORTH));
	assertThat(obtainDirectionAtMapCenter(), is(SOUTH));
    }

    @Test
    public void obtainDirectionOnMultiWithWeight() {
	PotentialMap mapSouth = createDirectedMap(SOUTH);
	PotentialMap mapNorth = createDirectedMap(NORTH);

	map.addMap(mapSouth, 2);
	assertThat(obtainDirectionAtMapCenter(), is(SOUTH));
	map.addMap(mapNorth, 1.5);
	assertThat(obtainDirectionAtMapCenter(), is(SOUTH));

	map.setWeight(mapNorth, 3);
	assertThat(obtainDirectionAtMapCenter(), is(NORTH));
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
