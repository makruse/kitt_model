package de.zmt.pathfinding;

import static de.zmt.pathfinding.DirectionConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;

public class FlowFromPotentialsMapTest {
    private static final int MAP_SIZE = 3;
    private static final int MAP_CENTER = (MAP_SIZE - 1) >> 1;

    /**
     * <pre>
     * 0 1 0
     * 0 0 0
     * 0 3 0
     * </pre>
     */
    private static final double[][] POTENTIALS_DOWN = new double[][] { { 0, 0, 0 }, { 1, 0, 3 }, { 0, 0, 0 } };
    /**
     * <pre>
     * 0 2 0
     * 0 0 0
     * 0 1 0
     * </pre>
     */
    private static final double[][] POTENTIALS_UP = new double[][] { { 0, 0, 0 }, { 2, 0, 1 }, { 0, 0, 0 } };

    private static final SimplePotentialMap POTENTIAL_MAP_NEUTRAL = new SimplePotentialMap(
	    new double[MAP_SIZE][MAP_SIZE]);
    private static final SimplePotentialMap POTENTIAL_MAP_DOWN = new SimplePotentialMap(POTENTIALS_DOWN);
    private static final SimplePotentialMap POTENTIAL_MAP_UP = new SimplePotentialMap(POTENTIALS_UP);

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
	map.addMap(POTENTIAL_MAP_UP);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_DOWN));
    }

    @Test
    public void obtainDirectionOnMultiWithWeight() {
	map.addMap(POTENTIAL_MAP_DOWN);
	map.addMap(POTENTIAL_MAP_UP, 3);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_UP));
	map.addMap(POTENTIAL_MAP_UP, -2);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_DOWN));
    }

    private Double2D obtainDirectionAtMapCenter() {
	return map.obtainDirection(MAP_CENTER, MAP_CENTER);
    }

    private static class SimplePotentialMap implements PotentialMap {
	private final DoubleGrid2D grid;

	public SimplePotentialMap(double[][] values) {
	    grid = new DoubleGrid2D(values);
	}

	@Override
	public int getWidth() {
	    return grid.getWidth();
	}

	@Override
	public int getHeight() {
	    return grid.getHeight();
	}

	@Override
	public double obtainPotential(int x, int y) {
	    return grid.get(x, y);
	}

    }
}
