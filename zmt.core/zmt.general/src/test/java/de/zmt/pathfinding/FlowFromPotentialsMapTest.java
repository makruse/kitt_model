package de.zmt.pathfinding;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;

public class FlowFromPotentialsMapTest {
    private static final int MAP_SIZE = 3;
    private static final int INVALID_MAP_SIZE = -MAP_SIZE;
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
    private static final Double2D DIRECTION_NEUTRAL = new Double2D();

    private static final Double2D DIRECTION_DOWN = new Double2D(0, 1);
    private static final Double2D DIRECTION_UP = DIRECTION_DOWN.negate();

    private FlowFromPotentialsMap map;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	map = new FlowFromPotentialsMap(MAP_SIZE, MAP_SIZE);
    }

    @Test
    public void addOnInvalid() {
	thrown.expect(IllegalArgumentException.class);
	map.addMap(new PotentialMap() {
	    
	    @Override
	    public int getWidth() {
		return INVALID_MAP_SIZE;
	    }
	    
	    @Override
	    public int getHeight() {
		return INVALID_MAP_SIZE;
	    }
	    
	    @Override
	    public double obtainPotential(int x, int y) {
		return 0;
	    }
	});
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

    @Test
    public void obtainDirectionOnDynamic() {
	SimpleDynamicMap dynamicMap = new SimpleDynamicMap();
	map.addMap(dynamicMap);
	assertThat(map.obtainDirection(MAP_CENTER, MAP_CENTER), is(DIRECTION_DOWN));
	dynamicMap.nextIteration();
	assertThat(map.obtainDirection(MAP_CENTER, MAP_CENTER), is(DIRECTION_UP));
    }

    @Test
    public void obtainDirectionOnAddAndRemove() {
	SimplePotentialMap potentialsMap = new SimplePotentialMap(POTENTIALS_1);
	map.addMap(potentialsMap);
	assertThat(map.obtainDirection(MAP_CENTER, MAP_CENTER), is(DIRECTION_DOWN));
	map.removeMap(potentialsMap);
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

    private static class SimpleDynamicMap extends BasicDynamicMap implements PotentialMap {
	private final Queue<double[][]> mapIterations = new ArrayDeque<>(Arrays.asList(POTENTIALS_1, POTENTIALS_2));

	@Override
	public int getWidth() {
	    return MAP_SIZE;
	}

	@Override
	public int getHeight() {
	    return MAP_SIZE;
	}

	@Override
	public double obtainPotential(int x, int y) {
	    return mapIterations.peek()[x][y];
	}
	
	/** Switch to next iteration and notify listeners. */
	public void nextIteration() {
	    mapIterations.remove();
	    for (int x = 0; x < MAP_SIZE; x++) {
		for (int y = 0; y < MAP_SIZE; y++) {
		    notifyListeners(x, y);
		}
	    }
	}
    }
}
