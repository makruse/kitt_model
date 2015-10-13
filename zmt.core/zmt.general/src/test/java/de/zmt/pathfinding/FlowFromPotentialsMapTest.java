package de.zmt.pathfinding;

import static de.zmt.pathfinding.DirectionConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

import sim.field.grid.DoubleGrid2D;

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
    private static final double[][] POTENTIALS_1 = new double[][] { { 0, 0, 0 }, { 1, 0, 3 }, { 0, 0, 0 } };
    /**
     * <pre>
     * 0 2 0
     * 0 0 0
     * 0 1 0
     * </pre>
     */
    private static final double[][] POTENTIALS_2 = new double[][] { { 0, 0, 0 }, { 2, 0, 1 }, { 0, 0, 0 } };
    private FlowFromPotentialsMap map;

    @Before
    public void setUp() throws Exception {
	map = new FlowFromPotentialsMap(MAP_SIZE, MAP_SIZE);
    }

    @Test
    public void obtainDirectionOnEmpty() {
	assertThat(map.obtainDirection(MAP_CENTER, MAP_CENTER), is(DIRECTION_NEUTRAL));
    }

    @Test
    public void obtainDirectionOnSum() {
	map.addMap(new SimplePotentialMap(POTENTIALS_1));
	map.addMap(new SimplePotentialMap(POTENTIALS_2));
	assertThat(map.obtainDirection(MAP_CENTER, MAP_CENTER), is(DIRECTION_DOWN));
    }

    @Test
    public void obtainDirectionOnNeutral() {
	map.addMap(new SimplePotentialMap(new double[MAP_SIZE][MAP_SIZE]));
	assertThat(map.obtainDirection(MAP_CENTER, MAP_CENTER), is(DIRECTION_NEUTRAL));
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
