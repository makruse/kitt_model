package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

import sim.util.Double2D;

public class FlowFromPotentialsMapTest {
    private static final int MAP_SIZE = 3;
    private static final int MAP_CENTER = (MAP_SIZE - 1) >> 1;

    private static final PotentialMap POTENTIAL_MAP_NEUTRAL = new SimplePotentialMap(
	    new double[MAP_SIZE][MAP_SIZE]);
    /**
     * <pre>
     * 0 0 0
     * 0 0 0
     * 0 1 0
     * </pre>
     */
    private static final PotentialMap POTENTIAL_MAP_DOWN = new SimplePotentialMap(
	    new double[][] { { 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 0 } });
    /**
     * <pre>
     * 0 1 0
     * 0 0 0
     * 0 0 0
     * </pre>
     */
    private static final PotentialMap POTENTIAL_MAP_UP = new SimplePotentialMap(
	    new double[][] { { 0, 0, 0 }, { 1, 0, 0 }, { 0, 0, 0 } });

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
    public void obtainDirectionOnNeutral() {
	map.addMap(POTENTIAL_MAP_NEUTRAL);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_NEUTRAL));
    }

    @Test
    public void obtainDirectionOnSingle() {
	map.addMap(POTENTIAL_MAP_DOWN);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_DOWN));
    }

    @Test
    public void obtainDirectionOnSingleWithWeight() {
	map.addMap(POTENTIAL_MAP_DOWN, 2);
	assertThat("Weight should not alter result when there is only a single map added.",
		obtainDirectionAtMapCenter(), is(DIRECTION_DOWN));
    }

    @Test
    public void obtainDirectionOnMulti() {
	map.addMap(POTENTIAL_MAP_DOWN);
	map.addMap(POTENTIAL_MAP_DOWN);
	map.addMap(POTENTIAL_MAP_UP);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_DOWN));
    }

    @Test
    public void obtainDirectionOnMultiWithWeight() {
	map.addMap(POTENTIAL_MAP_DOWN, 2);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_DOWN));
	map.addMap(POTENTIAL_MAP_UP, 1.5);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_DOWN));

	map.setWeight(POTENTIAL_MAP_UP, 3);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_UP));
    }

    private Double2D obtainDirectionAtMapCenter() {
	return map.obtainDirection(MAP_CENTER, MAP_CENTER);
    }
}
