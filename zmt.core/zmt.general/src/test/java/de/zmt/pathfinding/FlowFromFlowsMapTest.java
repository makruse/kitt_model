package de.zmt.pathfinding;

import static de.zmt.pathfinding.DirectionConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

import sim.util.Double2D;

public class FlowFromFlowsMapTest {
    private static final ConstantFlowMap FLOW_MAP_DOWN = new ConstantFlowMap(DIRECTION_DOWN);
    private static final ConstantFlowMap FLOW_MAP_RIGHT = new ConstantFlowMap(DIRECTION_RIGHT);
    private static final ConstantFlowMap FLOW_MAP_LEFT = new ConstantFlowMap(DIRECTION_LEFT);

    private static final int MAP_SIZE = 1;

    private FlowFromFlowsMap map;

    @Before
    public void setUp() throws Exception {
	map = new FlowFromFlowsMap(MAP_SIZE, MAP_SIZE);
    }

    @Test
    public void obtainDirectionOnEmpty() {
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_NEUTRAL));
    }

    @Test
    public void obtainDirectionOnSingle() {
	map.addMap(FLOW_MAP_DOWN);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_DOWN));
    }

    @Test
    public void obtainDirectionOnSingleWithWeight() {
	map.removeMap(map);
	map.addMap(FLOW_MAP_DOWN, 2);
	assertThat("Weight should not alter result when there is only a single map added.", map.obtainDirection(0, 0),
		is(DIRECTION_DOWN));
    }

    @Test
    public void obtainDirectionOnMulti() {
	map.addMap(FLOW_MAP_DOWN);
	map.addMap(FLOW_MAP_RIGHT);
	Double2D downRight = DIRECTION_DOWN.add(DIRECTION_RIGHT).normalize();
	assertThat(map.obtainDirection(0, 0), is(downRight));
    }

    @Test
    public void obtainDirectionOnMultiWithWeight() {
	map.addMap(FLOW_MAP_RIGHT);
	map.addMap(FLOW_MAP_LEFT, 2);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_LEFT));
    }

    /**
     * Tests if add / removal with the internal wrapping with decorator class
     * works.
     */
    @Test
    public void removeMapOnDifferentWeight() {
	map.addMap(FLOW_MAP_DOWN, 2);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_DOWN));
	assertThat(map.removeMap(FLOW_MAP_DOWN), is(true));
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_NEUTRAL));
    }

    private static class ConstantFlowMap implements FlowMap {
	private final Double2D value;

	public ConstantFlowMap(Double2D value) {
	    this.value = value;
	}

	@Override
	public int getWidth() {
	    return MAP_SIZE;
	}

	@Override
	public int getHeight() {
	    return MAP_SIZE;
	}

	@Override
	public Double2D obtainDirection(int x, int y) {
	    return value;
	}

	@Override
	public String toString() {
	    return getClass().getSimpleName() + "[value=" + value + "]";
	}
    }
}
